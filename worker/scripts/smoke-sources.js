import { parseRodiakiArticleLinks, parseRodiakiJsonLd, parseWordPressRss } from "../src/sources.js";

const sources = [
  ["Dimokratiki", "https://www.dimokratiki.gr/feed/", parseWordPressRss],
  ["Rodiaki", "https://www.rodiaki.gr/", async (html) => {
    const urls = parseRodiakiArticleLinks(html, 3);
    return (await Promise.all(urls.map(async (url) => parseRodiakiJsonLd(await (await fetch(url)).text())))).flat();
  }]
];

for (const [name, url, parse] of sources) {
  try {
    const response = await fetch(url);
    const articles = await parse(await response.text());
    console.log(`${name}: HTTP ${response.status}, parsed=${articles.length}`);
  } catch (error) {
    console.log(`${name}: ERROR ${error.message}`);
  }
}
