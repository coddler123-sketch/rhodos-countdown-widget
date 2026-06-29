# Rhodos Countdown Widget

Eine persoenliche Android-App mit Homescreen-Widget fuer die Vorfreude auf eine
fest geplante Rhodos-Reise am 20. September 2026.

## Funktionen

- Countdown bis zum Abflug um 14:30 Uhr
- eigene Darstellung fuer Abflugtag und Ankunft auf Rhodos
- bildschirmfuellende Main-App mit Sekundenanzeige und Fortschrittsbalken
- taegliche Rhodos-Sprueche, Fakten und Ausflugstipps
- Wetter und Vorhersage fuer Kolymbia ueber Open-Meteo
- lokale Galerie mit automatischem Tagesbild oder fest gepinntem Favoriten
- echtes, skalierbares 4x2-Homescreen-Widget mit Countdown, Wetter und Tagesbild
- manueller Widget- und Wetter-Refresh
- integrierte Updatepruefung ueber GitHub Releases
- Teilen des aktuellen Countdowns ueber Androids Share-Sheet

## Nutzung

1. App installieren und einmal oeffnen.
2. Auf dem Homescreen die Widgetauswahl des Launchers oeffnen.
3. Das Rhodos-Countdown-Widget hinzufuegen.
4. Ueber das Zahnrad in der App Hintergrundbild, Update und Teilen verwalten.

Ein Tipp auf den Widgettitel oder das Wetter startet eine sofortige
Aktualisierung. Ein Tipp auf die restliche Widgetflaeche oeffnet die App.

## Daten und Netzwerk

- Reise-, Bild- und Wetterdaten werden lokal gespeichert.
- Es gibt kein Benutzerkonto, keinen eigenen Server und kein Tracking.
- Wetterdaten kommen von `api.open-meteo.com`.
- Updateinformationen und APK-Dateien kommen aus den GitHub Releases dieses
  Repositorys.
- Die App benoetigt Internetzugriff und fuer die GitHub-Verteilung die
  Berechtigung zur Installation von APK-Updates.

## Technik

- Kotlin und Jetpack Compose fuer die Main-App
- klassische `RemoteViews` fuer das Android-Homescreen-Widget
- WorkManager fuer den 15-Minuten-Widgetzyklus
- SharedPreferences fuer Wettercache und Bildauswahl
- Minimum SDK 24, Target SDK 36
- R8-Optimierung im Release-Build

Der Worker ruft Wetter im Hintergrund nur zwischen 8 und 22 Uhr und hoechstens
etwa einmal pro Stunde ab. Nach dem Entfernen des letzten Widgets werden alle
Widget-WorkManager-Auftraege beendet.

## Lokaler Build

Voraussetzungen:

- Android Studio mit JDK 21
- Android SDK passend zu `compileSdk`

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug assembleRelease
```

Die APKs liegen anschliessend unter:

- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release.apk`

Instrumentierungstests mit laufendem Emulator:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

## Qualitaetssicherung

GitHub Actions prueft bei Pushes und Pull Requests:

- Unit-Tests
- Android Lint
- Debug- und optimierten Release-Build
- Compose- und Widget-Smoke-Tests auf einem Android-Emulator

Die aktuelle funktionale Spezifikation steht in [SPEC.md](SPEC.md).
