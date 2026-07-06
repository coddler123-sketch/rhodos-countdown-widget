CREATE TABLE articles (
  id TEXT PRIMARY KEY,
  original_title TEXT NOT NULL,
  german_title TEXT NOT NULL,
  german_summary TEXT NOT NULL,
  teaser TEXT NOT NULL,
  original_url TEXT NOT NULL UNIQUE,
  published_at TEXT NOT NULL,
  source TEXT NOT NULL,
  category TEXT NOT NULL,
  image_url TEXT,
  content_hash TEXT NOT NULL,
  first_seen_at TEXT NOT NULL,
  last_seen_at TEXT NOT NULL
);

CREATE INDEX articles_published_at_idx ON articles(published_at DESC);

CREATE TABLE source_health (
  source TEXT PRIMARY KEY,
  status TEXT NOT NULL,
  article_count INTEGER NOT NULL,
  error TEXT,
  last_checked_at TEXT NOT NULL
);
