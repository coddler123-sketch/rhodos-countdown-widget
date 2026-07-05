import { extractArticleText } from "../src/sources.js";

const feed = await (await fetch("https://rhodos-news.coddler123.workers.dev/api/news")).json();
for (const article of feed.items.slice(0, 8)) {
  try {
    const response = await fetch(article.originalUrl);
    const html = await response.text();
    const text = extractArticleText(html);
    console.log(`${article.source}\t${article.id}\thttp=${response.status}\thtml=${html.length}\ttext=${text.length}`);
  } catch (error) {
    console.log(`${article.source}\t${article.id}\terror=${error.message}`);
  }
}
