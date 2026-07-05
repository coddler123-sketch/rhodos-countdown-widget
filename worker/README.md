# Rhodos News Worker

Cloudflare Worker für den deutschen News-Feed der Android-App.

Aktive Quellenadapter: Rodiaki, Dimokratiki, RodosReport und Stadt Rhodos. Nicht erreichbare Quellen werden übersprungen und im JSON-Feld `sources` als `unavailable` ausgewiesen; der letzte erfolgreiche Gesamtfeed bleibt dadurch verfügbar.

`GET /api/news/:id` liefert eine serverseitig erzeugte deutsche Leseansicht. Der Worker akzeptiert nur IDs aus dem aktuellen Feed, speichert keinen Originalartikel und cached ausschließlich Zusammenfassung und Stichpunkte für sieben Tage.

## Lokal prüfen

```powershell
npm install
npm run check
npx wrangler dev
```

Der lokale Cron kann über `http://localhost:8787/cdn-cgi/handler/scheduled` ausgelöst werden. Workers AI und echte Quellabrufe benötigen gegebenenfalls `wrangler dev --remote`.

## Deployment

```powershell
npx wrangler login
npx wrangler secret put REFRESH_TOKEN
npx wrangler deploy
```

Wrangler legt den in `wrangler.toml` definierten KV-Namespace beim Deployment automatisch an. Danach die ausgegebene URL mit `/api/news` als `NEWS_API_URL` in der Android-Buildkonfiguration setzen.

Manueller Refresh:

```powershell
Invoke-RestMethod -Method Post -Uri "https://<worker>/api/admin/refresh" -Headers @{ Authorization = "Bearer <REFRESH_TOKEN>" }
```

Vor produktiver Nutzung müssen die Nutzungsbedingungen und robots.txt der Quellen geprüft und idealerweise die Zustimmung von Rodiaki für die serverseitige Auswertung eingeholt werden.
