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
  const article = feedRaw && JSON.parse(feedRaw).items?.find((item) => item.id === id);
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

  const unique = [...new Map(raw.map((article) => [normalizeTitle(article.originalTitle), article])).values()]
    .sort((a, b) => Date.parse(b.publishedAt) - Date.parse(a.publishedAt))
    .slice(0, Number(env.MAX_ARTICLES || 10));
  if (!unique.length) throw new Error("No source returned usable articles");

  const items = [];
  for (const article of unique) {
    try {
      items.push({ ...article, ...await translate(article, env), imageUrl: null });
    } catch (error) {
      console.error("translation failed", article.id, error);
    }
  }
  if (!items.length) throw new Error("No article could be translated");

  const sourceStatus = settled.map((result, index) => ({
    name: SOURCES[index].name,
    status: result.status === "fulfilled" ? "ok" : "unavailable",
    count: result.status === "fulfilled" ? result.value.articles.length : 0
  }));
  const payload = { schemaVersion: 1, generatedAt: new Date().toISOString(), sources: sourceStatus, items };
  await env.NEWS_CACHE.put(CACHE_KEY, JSON.stringify(payload));
  return payload;
}

function normalizeTitle(title) {
  return title.toLocaleLowerCase("el").normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/[^\p{L}\p{N}]+/gu, " ")
    .trim();
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
