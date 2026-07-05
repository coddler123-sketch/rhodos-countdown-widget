# News-Sektion

## Produktentscheidung

Empfohlen ist die umgesetzte Kombination: ein ruhiger Ticker mit höchstens zehn Meldungen auf der Startseite und eine eigene News-Seite mit Karten und Filtern. Ein Accordion allein versteckt aktuelle Inselinfos zu stark; ein dauerhafter großer Feed würde dagegen den Countdown verdrängen.

## Datenfluss

```text
Rodiaki HTML adapter ─┐
                     ├─> scheduled backend -> deduplicate -> translate/summarize -> JSON/cache/CDN
Dimokratiki RSS/API ──┘                                                        |
                                                                                v
Android app <- HTTPS JSON <- local SharedPreferences cache <- Compose UI
```

Die App scrapt keine Website und enthält keinen Übersetzungsschlüssel. `NewsController` lädt den fertigen deutschen Feed, validiert HTTPS-Links und ISO-8601-Daten und hält den letzten erfolgreichen Payload offline vor. Der Endpoint wird beim Build gesetzt:

```properties
NEWS_API_URL=https://example.net/api/news
```

Das JSON-Schema steht in `news-api-example.json`. Kategorien sind `RHODOS`, `DODECANESE`, `TRAVEL`, `WEATHER` und `EVENTS`.

## Backend-Empfehlung

Eine kleine Cloudflare Worker-, Firebase Function- oder Cloud-Run-Function genügt:

1. Alle drei bis sechs Stunden per Scheduler starten.
2. Pro Quelle einen Adapter hinter `NewsSource.fetch()` kapseln. Dimokratiki bevorzugt über `/feed/` beziehungsweise WordPress REST laden. Für Rodiaki wurde am 5. Juli 2026 kein öffentlicher RSS-/JSON-Endpunkt gefunden; deshalb nur serverseitig, robots.txt/AGB-konform und mit stabilen Selektoren erfassen oder eine Nutzungserlaubnis/API beim Verlag anfragen.
3. Artikel anhand kanonischer URL und Titel-Hash deduplizieren; nur lokale Rhodos-/Dodekanes-Meldungen übernehmen.
4. Ausschließlich Titel und einen Teaser von maximal etwa 300 Zeichen natürlich ins Deutsche übertragen. Bei Übersetzungsfehlern den Artikel auslassen oder mit `translationStatus: "failed"` intern protokollieren – niemals unübersetzten Text als deutsche Übersetzung ausgeben.
5. Bilder standardmäßig weglassen. Nur explizit freigegebene Hotlinks oder eigene lizenzierte Bilder ausliefern.
6. Das fertige JSON mit `Cache-Control: public, max-age=900, stale-while-revalidate=21600`, ETag und CORS nur für nötige Clients bereitstellen. Letzten erfolgreichen Feed behalten, falls eine Quelle ausfällt.
7. Keine Nutzerkennungen, Cookies oder Tracker erfassen. Logs nach kurzer Frist löschen und API-/Übersetzungsschlüssel ausschließlich als Server-Secrets speichern.

## UI- und Fehlerzustände

- Frische Daten: Karten mit deutschem Titel, kurzem Teaser, Quelle, Zeit und „Original lesen“.
- Cache: Hinweis „Offline verfügbar · zuletzt geladener Stand“.
- Refresh fehlgeschlagen: Cache bleibt sichtbar, ergänzt um eine unaufdringliche Warnung.
- Kein Cache/kein Netz: „Keine Verbindung. Bitte später erneut versuchen.“ plus Retry.
- Leerer Feed: „Zurzeit gibt es keine aktuellen Meldungen.“
- Backend nicht konfiguriert: eindeutiger Setup-Hinweis statt erfundener Demo-News.

Pull-to-refresh kann später ergänzt werden; der vorhandene Button ist absichtlich die dependenciesparende Erstversion. Remote-Bilder sind im Datenmodell vorbereitet, werden aber bis zur Rechteklärung nicht gerendert.
