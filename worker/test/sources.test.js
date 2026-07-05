import test from "node:test";
import assert from "node:assert/strict";
import { classify, parseRodiakiArticleLinks, parseRodiakiJsonLd, parseWordPressRss } from "../src/sources.js";

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
