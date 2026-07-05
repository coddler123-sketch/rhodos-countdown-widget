const decodeEntities = (value) => value
  .replace(/<!\[CDATA\[([\s\S]*?)\]\]>/g, "$1")
  .replace(/<[^>]+>/g, " ")
  .replace(/&amp;/g, "&")
  .replace(/&quot;/g, '"')
  .replace(/&#039;|&apos;/g, "'")
  .replace(/&lt;/g, "<")
  .replace(/&gt;/g, ">")
  .replace(/\s+/g, " ")
  .trim();

const first = (text, expression) => decodeEntities(expression.exec(text)?.[1] ?? "");

export function parseWordPressRss(xml, source = "Dimokratiki") {
  return [...xml.matchAll(/<item\b[^>]*>([\s\S]*?)<\/item>/gi)].map((match) => {
    const item = match[1];
    const originalUrl = first(item, /<link>([\s\S]*?)<\/link>/i);
    const originalTitle = first(item, /<title>([\s\S]*?)<\/title>/i);
    const teaser = first(item, /<(?:description|content:encoded)>([\s\S]*?)<\/(?:description|content:encoded)>/i);
    const publishedAt = new Date(first(item, /<pubDate>([\s\S]*?)<\/pubDate>/i)).toISOString();
    const categoryText = first(item, /<category>([\s\S]*?)<\/category>/i);
    return {
      id: stableId(source, originalUrl), originalTitle, teaser, originalUrl, publishedAt, source,
      category: classify(`${originalTitle} ${categoryText}`)
    };
  }).filter(isUsable);
}

export function parseRodiakiJsonLd(html) {
  const blocks = [...html.matchAll(/<script[^>]+type=["']application\/ld\+json["'][^>]*>([\s\S]*?)<\/script>/gi)];
  const nodes = blocks.flatMap((match) => {
    try {
      const value = JSON.parse(match[1]);
      return Array.isArray(value) ? value : value["@graph"] ?? [value];
    } catch {
      return [];
    }
  });
  return nodes.filter((node) => ["NewsArticle", "Article"].includes(node?.["@type"])).map((node) => {
    const originalUrl = node.url ?? node.mainEntityOfPage?.["@id"] ?? "";
    const originalTitle = decodeEntities(node.headline ?? "");
    return {
      id: stableId("Rodiaki", originalUrl),
      originalTitle,
      teaser: decodeEntities(node.description ?? ""),
      originalUrl,
      publishedAt: toIsoDate(node.datePublished),
      source: "Rodiaki",
      category: classify(`${originalTitle} ${node.articleSection ?? ""}`)
    };
  }).filter(isUsable);
}

function toIsoDate(value) {
  const timestamp = Date.parse(value ?? "");
  return Number.isNaN(timestamp) ? "" : new Date(timestamp).toISOString();
}

export function parseRodiakiArticleLinks(html, limit = 10) {
  const links = [...html.matchAll(/href=["'](https:\/\/www\.rodiaki\.gr\/article\/[^"'#?]+)["']/gi)]
    .map((match) => match[1]);
  return [...new Set(links)].slice(0, limit);
}

export function classify(text) {
  const value = text.toLocaleLowerCase("el");
  if (/καιρ|πυρκαγ|φωτιά|βροχ|καύσω|σεισμ/.test(value)) return "WEATHER";
  if (/πτήσ|αεροδρ|λιμάν|πλοί|κυκλοφορ|οδ|τουρισ/.test(value)) return "TRAVEL";
  if (/εκδήλω|φεστιβάλ|συναυλ|θέατρ|πολιτισ/.test(value)) return "EVENTS";
  if (/δωδεκάνησ|κω|κάλυμν|λέρο|κάρπαθ|πάτμο/.test(value)) return "DODECANESE";
  return "RHODOS";
}

function isUsable(article) {
  return article.originalTitle.length > 3
    && article.originalUrl.startsWith("https://")
    && !Number.isNaN(Date.parse(article.publishedAt));
}

function stableId(source, url) {
  let hash = 2166136261;
  for (const character of `${source}:${url}`) {
    hash ^= character.charCodeAt(0);
    hash = Math.imul(hash, 16777619);
  }
  return `${source.toLowerCase()}-${(hash >>> 0).toString(16)}`;
}
