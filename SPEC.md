# Rhodos Countdown Widget - Produktspezifikation

## Zweck

Die App ist ein persoenlicher Countdown fuer eine fest geplante Rhodos-Reise.
Sie soll bis zur Reise taeglich Vorfreude erzeugen und waehrend des Reisetags
einen passenden Ankunftszustand anzeigen.

Diese Spezifikation beschreibt den aktuellen Produktstand. Frei konfigurierbare
Reiseziele, Reisedaten oder mehrere Countdowns sind nicht Bestandteil der App.

## Reisedaten

- Abflug: 20. September 2026, 14:30 Uhr in lokaler Geraetezeit
- angenommene Ankunft: 20. September 2026, 19:00 Uhr
- Fortschrittszeitraum: 20. September 2025 bis zum Abflug

Vor dem Abflug zeigt die App Tage, Stunden, Minuten und Sekunden. Das Widget
zeigt Tage, Stunden und Minuten. Ab dem Reisetag und ab der angenommenen
Ankunft werden eigene Texte und Bilder verwendet.

## Main-App

Die Main-App zeigt:

- ein lokales Rhodos-Foto als bildschirmfuellenden Hintergrund
- Reisedatum, Countdown und Fortschritt
- einen Tagesspruch
- einen Rhodos-Fakt und einen Ausflugstipp des Tages
- aktuelles Wetter, Sonnenzeiten und eine Vorhersage fuer Kolymbia
- Status und manuelle Aktualisierung der Wetterdaten

Ueber das Einstellungs-Sheet kann der Nutzer:

- nach App-Updates suchen und sie installieren
- den aktuellen Countdown teilen
- die Hintergrundbild-Galerie oeffnen

## Bilder

Alle Reisebilder sind lokale App-Ressourcen. Es werden keine Bilder aus dem
Internet geladen.

Im Automatikmodus wird das Tagesbild deterministisch aus Kalenderjahr und
Tag-im-Jahr gewaehlt. Eine manuell ausgewaehlte Galerieposition wird in
SharedPreferences gespeichert und von Main-App und Widget gemeinsam verwendet.

## Wetter

Die Wetterquelle ist Open-Meteo fuer die festen Koordinaten von Kolymbia.
Abgerufen werden unter anderem:

- Temperatur und gefuehlte Temperatur
- Luftfeuchtigkeit, Niederschlag und Wind
- Wettercode und Tag-/Nachtstatus
- Sonnenaufgang und Sonnenuntergang
- Vorhersage fuer die folgenden Tage

Erfolgreiche Antworten werden lokal zwischengespeichert. Bei fehlendem Netz
oder einem API-Fehler bleibt der letzte Cache sichtbar.

Der Widget-Worker prueft Wetter regulaer nur zwischen 8 und 22 Uhr und
hoechstens etwa einmal pro Stunde. Ein manueller Refresh darf diese Begrenzung
ueberschreiben.

## Homescreen-Widget

Die App stellt ein skalierbares 4x2-Widget mit `RemoteViews` bereit. Es zeigt:

- Rhodos-Titel und Tagesbild
- Countdown oder Reise-/Ankunftszustand
- Fortschrittsbalken
- Wettertemperatur und Wettericon, sofern Cache vorhanden ist
- Tagesspruch

WorkManager rendert platzierte Widgets in einem eindeutigen periodischen
15-Minuten-Auftrag neu. Sofortige Aktualisierungen verwenden einen separaten,
ebenfalls eindeutigen Auftrag. Beim Entfernen des letzten Widgets werden beide
Auftraege abgebrochen.

## App-Updates

Beim Start prueft die App die neueste GitHub-Releaseversion. Akzeptiert werden
nur neuere, gueltig formatierte Versionen und HTTPS-APK-Links aus dem erwarteten
Releasepfad dieses Repositorys.

Die APK wird in den App-Cache geladen und ueber einen nicht exportierten
FileProvider an den Android-Paketinstaller uebergeben. Android entscheidet ueber
die eigentliche Installation und Signaturpruefung.

## Persistenz und Datenschutz

Lokal gespeichert werden:

- ausgewaehltes Hintergrundbild
- letzter erfolgreicher Wetterbericht und Abrufzeitpunkt

Es gibt keine Anmeldung, keine Cloud-Synchronisation, keine Analytics und keine
personenbezogene Serverdatenbank. Android-Backup ist fuer die App aktiviert.

## Architektur

- `MainActivity` und Compose-Dateien rendern die Main-App.
- `AppUpdateController` besitzt Updatezustand und Updateaktionen.
- `CountdownCalculator` kapselt Reisezeit und Statusberechnung.
- `WeatherRepository` kapselt Open-Meteo und den lokalen Cache.
- `Images` kapselt Bildrotation und Favoritenpersistenz.
- `RhodosCountdownLargeWidgetProvider` rendert das Widget.
- `RhodosWidgetWorker` plant und beendet Hintergrundarbeit.

## Qualitaetskriterien

Ein Stand ist releasefaehig, wenn:

- Unit-Tests erfolgreich sind
- Instrumentierungstests auf dem Emulator erfolgreich sind
- Android Lint keine Fehler meldet
- Debug- und optimierter Release-Build erfolgreich sind
- Main-App und Widget den korrekten Countdownzustand anzeigen
- Bildauswahl einen Activity-Neustart uebersteht
- das Entfernen des letzten Widgets den periodischen Worker beendet

## Nicht enthalten

- frei waehlbares Reiseziel oder Reisedatum
- mehrere Countdowns oder mehrere Widgetkonfigurationen
- Benutzerkonten oder Cloud-Synchronisation
- Online-Bilddownload
- Kalenderimport
- iOS-Version
