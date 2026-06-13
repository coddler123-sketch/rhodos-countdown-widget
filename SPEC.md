# Unser Rhodos-Abenteuer - MVP-Spezifikation

## Zweck

Eine Android-App mit echtem Homescreen-Widget fuer einen persoenlichen Rhodos-Urlaubs-Countdown.
Das Widget ist als Geschenk gedacht und soll jeden Tag durch ein wechselndes Rhodos-Bild Vorfreude machen.

## Nutzererlebnis

Die App dient nur zur Einrichtung:

- Titel bearbeiten
- Ziel-Datum und Ziel-Uhrzeit setzen
- Vorschau des Widgets sehen
- Widget-Aktualisierung anstossen

Das Homescreen-Widget ist die Hauptflaeche:

- zeigt ein Rhodos-Foto als Hintergrund
- zeigt "Unser Rhodos-Abenteuer"
- zeigt verbleibende Tage, Stunden und Minuten
- zeigt in groesseren Varianten das Ziel-Datum
- wechselt das Bild einmal pro Tag

## Nicht im MVP

- Sekundenanzeige
- Wetterdaten
- Online-Bilddownload
- Google-Kalender-Import
- mehrere Countdowns
- mehrere Reiseziele
- Cloud-Sync

## Widget-Varianten

### 2x2

Hauptvariante mit:

- grossem Hintergrundbild
- Titel oben links
- Countdown mittig oder unten
- Datum-Chip unten links
- kurzer Spruch unten oder im Chip-Bereich

### 4x1

Panorama-Variante mit:

- breitem Rhodos-Bild
- Titel links
- Countdown mittig
- kleiner Hinweis auf taegliches Rhodos-Bild rechts

## Datenmodell

```kotlin
data class CountdownSettings(
    val title: String,
    val targetEpochMillis: Long,
    val imageRotationSeed: Int
)
```

Voreinstellung:

- title: "Unser Rhodos-Abenteuer"
- targetEpochMillis: vom Nutzer gesetzt
- imageRotationSeed: 0

## Persistenz

DataStore speichert die lokalen Einstellungen.
Es gibt keinen Server und keine Anmeldung.

## Bildrotation

Die App liefert eine kleine lokale Rhodos-Bildsammlung mit.
Das Widget waehlt das Tagesbild deterministisch anhand des lokalen Datums.

Beispiel:

```text
imageIndex = daysSinceEpoch % imageCount
```

So bleibt das Bild fuer einen Tag stabil und wechselt am naechsten Tag automatisch.

## Aktualisierung

WorkManager plant eine taegliche Aktualisierung fuer das Bild.
Zusaetzlich aktualisiert das Widget den Countdown in sinnvollen Intervallen.

Da Android Widgets nicht jede Sekunde live aktualisiert, zeigt das MVP bewusst nur Tage, Stunden und Minuten.

## Technik

- Android Studio
- Kotlin
- Minimum SDK: wird beim Projektsetup festgelegt
- RemoteViews fuer robuste Homescreen-Widgets mit Bildhintergruenden
- DataStore fuer lokale Einstellungen
- WorkManager fuer Hintergrundaktualisierungen

## Erfolgskriterien

- Die App laesst Titel und Ziel-Datum setzen.
- Das Widget kann auf dem Homescreen hinzugefuegt werden.
- Das Widget zeigt den korrekten Countdown in Tagen, Stunden und Minuten.
- Das Widget zeigt ein Rhodos-Hintergrundbild.
- Das Hintergrundbild wechselt taeglich.
- Nach einem Neustart bleiben Einstellungen erhalten.

