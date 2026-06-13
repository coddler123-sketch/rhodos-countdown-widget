# Rhodos Countdown Widget

Ein echtes Android Homescreen-Widget als persoenlicher Countdown bis zum Rhodos-Urlaub.

## Ziel

Das Widget soll jeden Tag Vorfreude erzeugen: ein schoenes Rhodos-Foto als Hintergrund,
ein klar lesbarer Countdown und ein persoenlicher Titel fuer die Reise.

## MVP

- Android App in Kotlin
- echtes Android Homescreen-Widget
- Titel: "Unser Rhodos-Abenteuer"
- Countdown bis zu einem frei gesetzten Datum mit Uhrzeit
- Anzeige: Tage, Stunden, Minuten
- keine Sekunden
- lokale Rhodos-Bilder als taeglich wechselnder Widget-Hintergrund
- Speicherung lokal auf dem Geraet

## Erste Widget-Groessen

- 2x2: Hauptwidget mit Foto, Countdown, Datum und kurzem Spruch
- 4x1: Panorama-Widget fuer breite Homescreen-Bereiche

Kompakte Varianten wie 1x1 und 2x1 koennen spaeter folgen.

## Technische Richtung

- Kotlin
- klassische Android App Widgets mit RemoteViews
- DataStore fuer Einstellungen
- WorkManager fuer taegliche Bildwechsel und regelmaessige Aktualisierung
- lokale Bildsammlung in der App

## Designrichtung

Warm, hochwertig und persoenlich, aber nicht kitschig.

Die Widgets nutzen grosse Rhodos-Fotos, dezente dunkle Verlaeufe fuer Lesbarkeit,
weisse Typografie und abgerundete Android-Widget-Karten.

