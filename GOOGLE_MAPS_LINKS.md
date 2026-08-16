# Google-Maps-Links selbst pflegen

Die Datei `Google-Maps-Links.csv` ist die zentrale Liste für die Maps-Schaltflächen der vorhandenen Tipps. Sie lässt sich direkt mit Excel bearbeiten.

## Einen Link ändern oder ergänzen

1. `Google-Maps-Links.csv` mit Excel öffnen.
2. Den gewünschten Ort in Google Maps öffnen.
3. **Teilen** und anschließend **Link kopieren** wählen.
4. Den Link in derselben Zeile in die Spalte `google_maps_url` einfügen.
5. Als UTF-8-CSV mit Semikolon als Trennzeichen speichern.

Die Spalte `id` nicht ändern und keine Zeilen umsortieren oder löschen. Die Spalte `titel` dient nur als Orientierung. Ein leeres Feld in `google_maps_url` blendet die Maps-Schaltfläche für diesen Tipp aus.

Unterstützt werden normale Google-Maps-Links sowie die beim Teilen erzeugten Kurzlinks wie `https://maps.app.goo.gl/...`.

## Prüfen und Release bauen

`Google-Maps-Links-pruefen-und-Release-bauen.cmd` doppelt anklicken. Dabei werden die CSV geprüft, die Unit-Tests und Android-Lint ausgeführt und anschließend die Release-APK gebaut.

Bei einer ungültigen URL, einer doppelten ID oder einer beschädigten CSV stoppt der Build mit einer Meldung samt Zeilennummer. Die fertige APK liegt danach unter `app\build\outputs\apk\release\app-release.apk`.

Die CSV ordnet Links den bereits vorhandenen Tipps zu. Für einen vollständig neuen Tipp müssen zusätzlich Titel, Text und Bild in der App ergänzt werden. Änderungen aus der CSV erscheinen nach einem neuen Build und einer erneuten Installation der App.
