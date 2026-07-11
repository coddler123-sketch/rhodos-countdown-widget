import test from "node:test";
import assert from "node:assert/strict";
import { classify, extractArticleText, parseRodiakiArticleLinks, parseRodiakiJsonLd, parseWordPressRss } from "../src/sources.js";
import { buildStatus, deduplicateArticles, truncateSummary } from "../src/index.js";

test("parses a WordPress RSS item", () => {
  const xml = `<rss><channel><item><title><![CDATA[Νέα πτήση για τη Ρόδο]]></title><link>https://www.dimokratiki.gr/a</link><pubDate>Sun, 05 Jul 2026 08:00:00 GMT</pubDate><description><![CDATA[<p>Περίληψη</p>]]></description><category>Τουρισμός</category></item></channel></rss>`;
  const [article] = parseWordPressRss(xml);
  assert.equal(article.originalTitle, "Νέα πτήση για τη Ρόδο");
  assert.equal(article.teaser, "Περίληψη");
  assert.equal(article.category, "TRAVEL");
});

test("parses Rodiaki JSON-LD", () => {
  const html = `<script type="application/ld+json">{"@type":"NewsArticle","headline":"Συναυλία στη Ρόδο","description":"Μουσική","url":"https://www.rodiaki.gr/a","datePublished":"2026-07-05T08:00:00Z"}</script>`;
  const [article] = parseRodiakiJsonLd(html);
  assert.equal(article.source, "Rodiaki");
  assert.equal(article.category, "EVENTS");
});

test("extracts unique Rodiaki article links", () => {
  const html = `<a href="https://www.rodiaki.gr/article/1/a">A</a><a href="https://www.rodiaki.gr/article/1/a">A</a><a href="https://www.rodiaki.gr/article/2/b">B</a>`;
  assert.deepEqual(parseRodiakiArticleLinks(html), [
    "https://www.rodiaki.gr/article/1/a",
    "https://www.rodiaki.gr/article/2/b"
  ]);
});

test("classifies weather warnings", () => {
  assert.equal(classify("Πυρκαγιά στη Ρόδο"), "WEATHER");
});

test("does not confuse a summit with travel", () => {
  assert.equal(classify("Σύνοδος του ΝΑΤΟ με οπλισμένα F-16"), "RHODOS");
});

test("classifies English festival and sports titles as events", () => {
  assert.equal(classify("Rhodes Street Food Festival"), "EVENTS");
  assert.equal(classify("Rhodes Street Basketball 3x3"), "EVENTS");
});

test("classifies road closures as travel", () => {
  assert.equal(classify("Προσωρινή διακοπή κυκλοφορίας στην οδό προς Ιαλυσό"), "TRAVEL");
});

test("extracts articleBody from JSON-LD without markup", () => {
  const body = "Α".repeat(350);
  const html = `<script type="application/ld+json">${JSON.stringify({ "@type": "NewsArticle", articleBody: body })}</script>`;
  assert.equal(extractArticleText(html), body);
});

test("falls back to semantic article content", () => {
  const paragraph = "Τοπική είδηση από τη Ρόδο. ".repeat(20);
  const html = `<article><nav>Navigation</nav><p>${paragraph}</p><script>tracking()</script></article>`;
  const result = extractArticleText(html);
  assert.match(result, /Τοπική είδηση/);
  assert.doesNotMatch(result, /Navigation|tracking/);
});

test("truncates a long summary at a complete sentence", () => {
  const result = truncateSummary(`${"Ein vollständiger Satz. ".repeat(90)}Unvollständig`, 300);
  assert.ok(result.length <= 300);
  assert.ok(result.endsWith("."));
});

test("deduplicates similar headlines from different sources", () => {
  const base = {
    teaser: "Teaser",
    category: "RHODOS"
  };
  const articles = [
    {
      ...base,
      id: "a",
      source: "Rodiaki",
      originalUrl: "https://example.com/a",
      originalTitle: "Μεγάλη πυρκαγιά σήμερα κοντά στην πόλη της Ρόδου",
      publishedAt: "2026-07-06T10:00:00Z"
    },
    {
      ...base,
      id: "b",
      source: "Dimokratiki",
      originalUrl: "https://example.com/b",
      originalTitle: "Μεγάλη πυρκαγιά κοντά στην πόλη της Ρόδου σήμερα",
      publishedAt: "2026-07-06T09:30:00Z"
    }
  ];

  assert.deepEqual(deduplicateArticles(articles).map((article) => article.id), ["a"]);
});

test("keeps similar headlines when they are published on different days", () => {
  const articles = [
    { id: "new", originalTitle: "Νέα πτήση από Αθήνα προς Ρόδο το καλοκαίρι", publishedAt: "2026-07-06T10:00:00Z" },
    { id: "old", originalTitle: "Νέα πτήση από Αθήνα προς Ρόδο το καλοκαίρι", publishedAt: "2026-07-04T10:00:00Z" }
  ];

  assert.equal(deduplicateArticles(articles).length, 2);
});

test("builds public status from cached news", async () => {
  const status = await buildStatus({
    NEWS_CACHE: {
      get: async () => JSON.stringify({
        generatedAt: "2026-07-07T08:00:00.000Z",
        sources: [{ name: "Rodiaki", status: "ok", count: 3 }],
        items: [{ id: "a" }, { id: "b" }]
      })
    }
  }, new Date("2026-07-07T09:00:00.000Z"));

  assert.equal(status.status, "ok");
  assert.equal(status.cache.itemCount, 2);
  assert.equal(status.cache.ageMinutes, 60);
  assert.deepEqual(status.sources, [{ name: "Rodiaki", status: "ok", count: 3, lastCheckedAt: null }]);
});

test("marks status as degraded when a source is unavailable", async () => {
  const status = await buildStatus({
    NEWS_CACHE: {
      get: async () => JSON.stringify({
        generatedAt: "2026-07-07T08:00:00.000Z",
        sources: [{ name: "RodosReport", status: "unavailable", count: 0 }],
        items: [{ id: "a" }]
      })
    }
  }, new Date("2026-07-07T09:00:00.000Z"));

  assert.equal(status.status, "degraded");
});
