import { extractArticleText, parseRodiakiArticleLinks, parseRodiakiJsonLd, parseWordPressRss } from "./sources.js";

const CACHE_KEY = "latest-news-v1";
const SOURCES = [
  {
    name: "Rodiaki",
    url: "https://www.rodiaki.gr/",
    load: loadRodiaki
  },
  {
    name: "Dimokratiki",
    url: "https://www.dimokratiki.gr/feed/",
    load: async (body) => parseWordPressRss(body, "Dimokratiki")
  },
  {
    name: "RodosReport",
    url: "https://rodosreport.gr/category/nea-rodou/feed/",
    load: async (body) => parseWordPressRss(body, "RodosReport")
  },
  {
    name: "Stadt Rhodos",
    url: "https://www.rhodes.gr/feed/",
    load: async (body) => parseWordPressRss(body, "Stadt Rhodos")
  }
];

export default {
  async fetch(request, env, ctx) {
    const url = new URL(request.url);
    if (request.method === "OPTIONS") return response(null, 204);
    if (request.method === "GET" && url.pathname === "/api/news") {
      const cached = await env.NEWS_CACHE.get(CACHE_KEY);
      if (cached) return response(cached);
      try {
        const payload = await refreshNews(env);
        return response(JSON.stringify(payload));
      } catch (error) {
        console.error("initial refresh failed", error);
        return response(JSON.stringify({ error: "News sind momentan nicht verfügbar." }), 503);
      }
    }
    if (request.method === "GET" && url.pathname === "/api/status") {
      return response(JSON.stringify(await buildStatus(env)));
    }
    const detailMatch = request.method === "GET" && url.pathname.match(/^\/api\/news\/([a-z0-9-]+)$/i);
    if (detailMatch) {
      return articleDetail(decodeURIComponent(detailMatch[1]), env);
    }
    if (request.method === "POST" && url.pathname === "/api/admin/refresh") {
      if (!env.REFRESH_TOKEN || request.headers.get("Authorization") !== `Bearer ${env.REFRESH_TOKEN}`) {
        return response(JSON.stringify({ error: "Unauthorized" }), 401);
      }
      try {
        const payload = await refreshNews(env);
        return response(JSON.stringify(payload));
      } catch (error) {
        console.error("manual refresh failed", error);
        return response(JSON.stringify({ error: error instanceof Error ? error.message : "Refresh failed" }), 500);
      }
    }
    return response(JSON.stringify({ error: "Not found" }), 404);
  },

  async scheduled(_controller, env, ctx) {
    ctx.waitUntil(refreshNews(env).catch((error) => console.error("scheduled refresh failed", error)));
  }
};

async function articleDetail(id, env) {
  const detailKey = `news-detail-v2:${id}`;
  const cached = await env.NEWS_CACHE.get(detailKey);
  if (cached) return response(cached);

  const feedRaw = await env.NEWS_CACHE.get(CACHE_KEY);
  const currentArticle = feedRaw && JSON.parse(feedRaw).items?.find((item) => item.id === id);
  const article = currentArticle ?? await loadArchivedArticle(id, env);
  if (!article) return response(JSON.stringify({ error: "Meldung nicht gefunden." }), 404);

  try {
    const sourceResponse = await fetch(article.originalUrl, {
      headers: { "User-Agent": "RhodosCountdownNews/1.0 (+news summarizer; contact app owner)" },
      signal: AbortSignal.timeout(12_000)
    });
    if (!sourceResponse.ok) throw new Error(`Source HTTP ${sourceResponse.status}`);
    const contentType = sourceResponse.headers.get("content-type") ?? "";
    if (!contentType.includes("text/html")) throw new Error("Source is not HTML");
    const articleText = extractArticleText(await sourceResponse.text());
    if (!articleText) throw new Error("No usable article text");

    const detail = await summarizeArticle(article, articleText, env);
    const payload = JSON.stringify({
      schemaVersion: 1,
      id: article.id,
      germanTitle: article.germanTitle,
      germanDetail: detail.germanDetail,
      keyPoints: detail.keyPoints,
      source: article.source,
      publishedAt: article.publishedAt,
      originalUrl: article.originalUrl,
      generatedAt: new Date().toISOString()
    });
    await env.NEWS_CACHE.put(detailKey, payload, { expirationTtl: 604_800 });
    return response(payload);
  } catch (error) {
    console.error("article detail failed", id, error);
    return response(JSON.stringify({ error: "Die deutsche Leseansicht ist für diesen Artikel nicht verfügbar." }), 502);
  }
}

export async function refreshNews(env) {
  const checkedAt = new Date().toISOString();
  const settled = await Promise.allSettled(SOURCES.map(async (source) => {
    const result = await fetch(source.url, {
      headers: { "User-Agent": "RhodosCountdownNews/1.0 (+news aggregator; contact app owner)" }
    });
    if (!result.ok) throw new Error(`${source.name}: HTTP ${result.status}`);
    return { source: source.name, articles: await source.load(await result.text()) };
  }));
  const raw = settled.flatMap((result) => result.status === "fulfilled" ? result.value.articles : []);
  settled.filter((result) => result.status === "rejected")
    .forEach((result) => console.error("source failed", result.reason));

  const unique = deduplicateArticles(raw)
    .slice(0, Number(env.MAX_ARTICLES || 10));
  if (!unique.length) throw new Error("No source returned usable articles");

  const items = [];
  for (const article of unique) {
    try {
      const contentHash = await hashArticle(article);
      const stored = await loadStoredTranslation(article.id, env);
      const translated = stored?.content_hash === contentHash
        ? { germanTitle: stored.german_title, germanSummary: stored.german_summary }
        : await translate(article, env);
      items.push({ ...article, ...translated, imageUrl: null, contentHash });
    } catch (error) {
      console.error("translation failed", article.id, error);
    }
  }
  if (!items.length) throw new Error("No article could be translated");

  const sourceStatus = settled.map((result, index) => ({
    name: SOURCES[index].name,
    status: result.status === "fulfilled" ? "ok" : "unavailable",
    count: result.status === "fulfilled" ? result.value.articles.length : 0,
    error: result.status === "rejected" ? errorMessage(result.reason) : null
  }));
  try {
    await persistAggregation(items, sourceStatus, checkedAt, env);
  } catch (error) {
    console.error("aggregation persistence failed", error);
  }
  const publicItems = items.map(({ contentHash: _contentHash, ...article }) => article);
  const publicSources = sourceStatus.map(({ error: _error, ...source }) => source);
  const payload = { schemaVersion: 1, generatedAt: new Date().toISOString(), sources: publicSources, items: publicItems };
  await env.NEWS_CACHE.put(CACHE_KEY, JSON.stringify(payload));
  return payload;
}

export async function buildStatus(env, now = new Date()) {
  const cacheRaw = await env.NEWS_CACHE.get(CACHE_KEY);
  const cached = cacheRaw ? JSON.parse(cacheRaw) : null;
  const generatedAt = cached?.generatedAt ?? null;
  const ageMinutes = generatedAt
    ? Math.max(0, Math.round((now.getTime() - Date.parse(generatedAt)) / 60_000))
    : null;
  const archive = await loadArchiveStatus(env);
  const sources = archive.sources.length ? archive.sources : (cached?.sources ?? []);
  const itemCount = cached?.items?.length ?? 0;
  const unavailableSources = sources.filter((source) => source.status !== "ok").length;
  const status = !itemCount
    ? "empty"
    : ageMinutes !== null && ageMinutes > 12 * 60
      ? "stale"
      : unavailableSources
        ? "degraded"
        : "ok";

  return {
    schemaVersion: 1,
    status,
    checkedAt: now.toISOString(),
    lastRefresh: archive.lastCheckedAt ?? generatedAt,
    cache: {
      available: Boolean(cached),
      generatedAt,
      ageMinutes,
      itemCount
    },
    archive: {
      available: archive.available,
      itemCount: archive.itemCount,
      lastSeenAt: archive.lastSeenAt
    },
    sources: sources.map((source) => ({
      name: source.name ?? source.source,
      status: source.status,
      count: source.count ?? source.article_count ?? 0,
      lastCheckedAt: source.lastCheckedAt ?? source.last_checked_at ?? null
    }))
  };
}

async function loadArchiveStatus(env) {
  if (!env.NEWS_DB) {
    return { available: false, itemCount: null, lastSeenAt: null, lastCheckedAt: null, sources: [] };
  }
  try {
    const [articleStats, health] = await Promise.all([
      env.NEWS_DB.prepare("SELECT COUNT(*) AS count, MAX(last_seen_at) AS last_seen_at FROM articles").first(),
      env.NEWS_DB.prepare(
        "SELECT source, status, article_count, last_checked_at FROM source_health ORDER BY source"
      ).all()
    ]);
    const sources = health.results ?? [];
    return {
      available: true,
      itemCount: articleStats?.count ?? 0,
      lastSeenAt: articleStats?.last_seen_at ?? null,
      lastCheckedAt: sources.map((source) => source.last_checked_at).filter(Boolean).sort().at(-1) ?? null,
      sources
    };
  } catch (error) {
    console.error("status lookup failed", error);
    return { available: false, itemCount: null, lastSeenAt: null, lastCheckedAt: null, sources: [] };
  }
}

function normalizeTitle(title) {
  return title.toLocaleLowerCase("el").normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/[^\p{L}\p{N}]+/gu, " ")
    .trim();
}

export function deduplicateArticles(articles) {
  const sorted = [...articles].sort((a, b) => Date.parse(b.publishedAt) - Date.parse(a.publishedAt));
  const unique = [];
  for (const article of sorted) {
    const duplicate = unique.some((candidate) => {
      const age = Math.abs(Date.parse(candidate.publishedAt) - Date.parse(article.publishedAt));
      return age <= 36 * 60 * 60 * 1_000 && titleSimilarity(candidate.originalTitle, article.originalTitle) >= 0.72;
    });
    if (!duplicate) unique.push(article);
  }
  return unique;
}

function titleSimilarity(left, right) {
  const leftNormalized = normalizeTitle(left);
  const rightNormalized = normalizeTitle(right);
  if (leftNormalized === rightNormalized) return 1;
  const leftTokens = new Set(leftNormalized.split(" ").filter((token) => token.length > 2));
  const rightTokens = new Set(rightNormalized.split(" ").filter((token) => token.length > 2));
  if (leftTokens.size < 4 || rightTokens.size < 4) return 0;
  const intersection = [...leftTokens].filter((token) => rightTokens.has(token)).length;
  return intersection / new Set([...leftTokens, ...rightTokens]).size;
}

async function hashArticle(article) {
  const data = new TextEncoder().encode(`${normalizeTitle(article.originalTitle)}\n${article.teaser.trim()}`);
  const digest = await crypto.subtle.digest("SHA-256", data);
  return [...new Uint8Array(digest)].map((byte) => byte.toString(16).padStart(2, "0")).join("");
}

async function loadStoredTranslation(id, env) {
  if (!env.NEWS_DB) return null;
  try {
    return await env.NEWS_DB.prepare(
      "SELECT content_hash, german_title, german_summary FROM articles WHERE id = ?"
    ).bind(id).first();
  } catch (error) {
    console.error("translation cache lookup failed", id, error);
    return null;
  }
}

async function loadArchivedArticle(id, env) {
  if (!env.NEWS_DB) return null;
  try {
    const row = await env.NEWS_DB.prepare(
      "SELECT id, original_title, german_title, german_summary, original_url, published_at, source, category, image_url FROM articles WHERE id = ?"
    ).bind(id).first();
    return row && {
      id: row.id,
      originalTitle: row.original_title,
      germanTitle: row.german_title,
      germanSummary: row.german_summary,
      originalUrl: row.original_url,
      publishedAt: row.published_at,
      source: row.source,
      category: row.category,
      imageUrl: row.image_url
    };
  } catch (error) {
    console.error("archive lookup failed", id, error);
    return null;
  }
}

async function persistAggregation(items, sourceStatus, checkedAt, env) {
  if (!env.NEWS_DB) return;
  const articleStatements = items.map((article) => env.NEWS_DB.prepare(`
    INSERT INTO articles (
      id, original_title, german_title, german_summary, teaser, original_url,
      published_at, source, category, image_url, content_hash, first_seen_at, last_seen_at
    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    ON CONFLICT(id) DO UPDATE SET
      original_title = excluded.original_title,
      german_title = excluded.german_title,
      german_summary = excluded.german_summary,
      teaser = excluded.teaser,
      original_url = excluded.original_url,
      published_at = excluded.published_at,
      source = excluded.source,
      category = excluded.category,
      image_url = excluded.image_url,
      content_hash = excluded.content_hash,
      last_seen_at = excluded.last_seen_at
  `).bind(
    article.id, article.originalTitle, article.germanTitle, article.germanSummary, article.teaser,
    article.originalUrl, article.publishedAt, article.source, article.category, article.imageUrl,
    article.contentHash, checkedAt, checkedAt
  ));
  const healthStatements = sourceStatus.map((source) => env.NEWS_DB.prepare(`
    INSERT INTO source_health (source, status, article_count, error, last_checked_at)
    VALUES (?, ?, ?, ?, ?)
    ON CONFLICT(source) DO UPDATE SET
      status = excluded.status,
      article_count = excluded.article_count,
      error = excluded.error,
      last_checked_at = excluded.last_checked_at
  `).bind(source.name, source.status, source.count, source.error, checkedAt));
  await env.NEWS_DB.batch([...articleStatements, ...healthStatements]);
}

function errorMessage(error) {
  return String(error instanceof Error ? error.message : error).slice(0, 300);
}

async function loadRodiaki(homepage) {
  const pages = await Promise.allSettled(parseRodiakiArticleLinks(homepage).map(async (url) => {
    const response = await fetch(url, {
      headers: { "User-Agent": "RhodosCountdownNews/1.0 (+news aggregator; contact app owner)" }
    });
    if (!response.ok) throw new Error(`Rodiaki article: HTTP ${response.status}`);
    return parseRodiakiJsonLd(await response.text());
  }));
  return pages.flatMap((result) => result.status === "fulfilled" ? result.value : []);
}

async function translate(article, env) {
  const prompt = `Du bist Redakteur eines deutschsprachigen Rhodos-Reisebegleiters. Übersetze den griechischen Titel natürlich ins Deutsche und schreibe aus dem Teaser eine sachliche Zusammenfassung mit höchstens 280 Zeichen. Erfinde nichts. Antworte ausschließlich als JSON mit den Schlüsseln germanTitle und germanSummary.\n\nTitel: ${article.originalTitle}\nTeaser: ${article.teaser}`;
  const result = await env.AI.run(env.AI_MODEL, {
    messages: [{ role: "system", content: "Antworte ausschließlich mit gültigem JSON." }, { role: "user", content: prompt }],
    response_format: {
      type: "json_schema",
      json_schema: {
        type: "object",
        properties: {
          germanTitle: { type: "string" },
          germanSummary: { type: "string" }
        },
        required: ["germanTitle", "germanSummary"]
      }
    },
    max_tokens: 280,
    temperature: 0.2
  });
  const output = typeof result === "string" ? result : result.response;
  const json = typeof output === "object" && output !== null
    ? output
    : JSON.parse(String(output).match(/\{[\s\S]*\}/)?.[0] ?? "");
  if (!json.germanTitle || !json.germanSummary) throw new Error("Invalid translation response");
  return {
    germanTitle: String(json.germanTitle).trim(),
    germanSummary: String(json.germanSummary).trim().slice(0, 300)
  };
}

async function summarizeArticle(article, articleText, env) {
  const prompt = `Erstelle für einen deutschsprachigen Rhodos-Reisebegleiter eine eigenständig formulierte, sachliche Zusammenfassung dieses griechischen Nachrichtenartikels. Verwende 3 bis 5 kurze Absätze. Keine wörtlichen Zitate, keine Spekulationen und keine Informationen außerhalb des Textes. germanDetail darf höchstens 1800 Zeichen lang sein. Schreibe außerdem genau 3 bis 5 kurze Stichpunkte in keyPointsText, je einer pro Zeile und ohne Nummerierung. Antworte nur als JSON mit den beiden Textfeldern germanDetail und keyPointsText.\n\nTitel: ${article.originalTitle}\n\nArtikeltext:\n${articleText}`;
  const request = {
    messages: [
      { role: "system", content: "Du fasst griechische Lokalnachrichten natürlich auf Deutsch zusammen und antwortest nur mit gültigem JSON." },
      { role: "user", content: prompt }
    ],
    response_format: {
      type: "json_schema",
      json_schema: {
        type: "object",
        properties: {
          germanDetail: { type: "string" },
          keyPointsText: { type: "string" }
        },
        required: ["germanDetail", "keyPointsText"]
      }
    },
    max_tokens: 900,
    temperature: 0.2
  };

  let lastError;
  for (const structured of [true, false]) {
    try {
      const currentRequest = structured ? request : { ...request, response_format: undefined };
      const result = await env.AI.run(env.AI_MODEL, currentRequest);
      const output = typeof result === "string" ? result : result.response;
      const json = typeof output === "object" && output !== null
        ? output
        : JSON.parse(String(output).match(/\{[\s\S]*\}/)?.[0] ?? "");
      const germanDetail = truncateSummary(String(json.germanDetail ?? "").trim());
      let keyPoints = String(json.keyPointsText ?? "")
        .split(/\r?\n|•/)
        .map((point) => point.replace(/^\s*(?:[-*]|\d+[.)])\s*/, "").trim())
        .filter(Boolean)
        .slice(0, 5);
      if (keyPoints.length < 3) keyPoints = pointsFromSummary(germanDetail);
      if (!germanDetail || keyPoints.length < 3) throw new Error("Invalid detail response");
      return { germanDetail, keyPoints };
    } catch (error) {
      lastError = error;
      console.error("detail model attempt failed", structured ? "structured" : "fallback", error);
    }
  }
  throw lastError ?? new Error("Detail model failed");
}

function pointsFromSummary(value) {
  return (value.match(/[^.!?]+[.!?]+/g) ?? [])
    .map((sentence) => sentence.trim())
    .filter((sentence) => sentence.length >= 25)
    .slice(0, 5);
}

export function truncateSummary(value, maxLength = 1_800) {
  if (value.length <= maxLength) return value;
  const candidate = value.slice(0, maxLength);
  const sentenceEnd = Math.max(candidate.lastIndexOf("."), candidate.lastIndexOf("!"), candidate.lastIndexOf("?"));
  if (sentenceEnd >= Math.floor(maxLength * 0.65)) return candidate.slice(0, sentenceEnd + 1);
  const wordEnd = candidate.lastIndexOf(" ");
  return `${candidate.slice(0, wordEnd > 0 ? wordEnd : maxLength).trim()}…`;
}

function response(body, status = 200) {
  return new Response(body, {
    status,
    headers: {
      "Content-Type": "application/json; charset=utf-8",
      "Cache-Control": status === 200 ? "public, max-age=900, stale-while-revalidate=21600" : "no-store",
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
      "Access-Control-Allow-Headers": "Authorization",
      "X-Content-Type-Options": "nosniff"
    }
  });
}
