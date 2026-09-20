package com.example.rhodoswidget.ui.travel

import com.example.rhodoswidget.R

internal data class PhotoSpot(
    val id: String,
    val title: String,
    val location: String,
    val bestTime: String,
    val timeCategory: String, // "Sonnenaufgang", "Vormittag", "Golden Hour", "Blaue Stunde", "Mittagslicht"
    val tip: String,
    val mapQuery: String,
    val iconEmoji: String
)

internal val photoSpots = listOf(
    PhotoSpot(
        id = "eucalyptus_alley",
        title = "Eukalyptus-Allee",
        location = "Kolymbia Center",
        bestTime = "Frühmorgens (07:00–08:30 Uhr)",
        timeCategory = "Sonnenaufgang",
        tip = "Fotografiert flach von der Fahrbahnmitte nach oben. Das morgendliche Gegenlicht durch die 2 km lange Baumkrone erzeugt spektakuläre Fluchtlinien.",
        mapQuery = "Eucalyptus Street, Kolymbia, Rhodes",
        iconEmoji = "🌿"
    ),
    PhotoSpot(
        id = "tsambika_viewpoint",
        title = "Tsambika Gipfel & Kapelle",
        location = "Tsambika Berg",
        bestTime = "Golden Hour & Spätnachmittag (17:30–19:00 Uhr)",
        timeCategory = "Golden Hour",
        tip = "Nach den 300 Stufen belohnt euch die Aussichtsplattform mit der wehenden Griechenland-Flagge vor der golden schimmernden Bucht.",
        mapQuery = "Monastery Tsambika, Rhodes",
        iconEmoji = "⛪"
    ),
    PhotoSpot(
        id = "anthony_quinn_bay",
        title = "Anthony Quinn Bucht",
        location = "Kallithea / Ladiko",
        bestTime = "Vormittag (10:00–12:00 Uhr)",
        timeCategory = "Vormittag",
        tip = "Der beste Winkel ist von der Aussichtsterrasse oberhalb der Taverne. Die Sonne steht perfekt, um das smaragdgrüne Wasser leuchten zu lassen.",
        mapQuery = "Anthony Quinn Bay, Rhodes",
        iconEmoji = "🌊"
    ),
    PhotoSpot(
        id = "haraki_feraklos",
        title = "Burg Feraklos & Haraki Bucht",
        location = "Haraki Fischerdorf",
        bestTime = "Sonnenuntergang & Dämmerung (18:00–19:30 Uhr)",
        timeCategory = "Golden Hour",
        tip = "Setzt die erleuchtete Ruine der Johanniterburg als Silhouette im Hintergrund der malerischen Boote an der Fischerpromenade in Szene.",
        mapQuery = "Feraklos Castle, Haraki, Rhodes",
        iconEmoji = "🏰"
    ),
    PhotoSpot(
        id = "afandou_beach_sunrise",
        title = "Afandou Beach & Kieselstrand",
        location = "Südlich von Kolymbia",
        bestTime = "Sonnenaufgang (06:15–07:00 Uhr)",
        timeCategory = "Sonnenaufgang",
        tip = "Direkter unverbauter Blick auf das Aufgehen der Sonne über der Ägäis. Wunderschöne Spiegelungen im sanften Brandungssaum.",
        mapQuery = "Afandou Beach, Rhodes",
        iconEmoji = "🌅"
    ),
    PhotoSpot(
        id = "epta_piges_tunnel",
        title = "Sieben Quellen Wassertunnel",
        location = "Epta Piges Inland",
        bestTime = "Mittagslicht (11:30–14:00 Uhr)",
        timeCategory = "Mittagslicht",
        tip = "Im schattigen Wald brechen Mittagsstrahlen durch das Blätterdach. Haltet am Tunnelausgang Ausschau nach den stolzierenden Pfauen!",
        mapQuery = "Seven Springs, Rhodes",
        iconEmoji = "🦚"
    ),
    PhotoSpot(
        id = "lindos_acropolis_view",
        title = "Lindos Panorama-Aussichtspunkt",
        location = "Küstenstraße oberhalb Lindos",
        bestTime = "Vormittag (09:00–10:30 Uhr)",
        timeCategory = "Vormittag",
        tip = "Haltebucht an der Hauptstraße nutzen: Perfekter Kontrast zwischen schneeweißen Häusern, der antiken Akropolis und dem tiefblauen Meer.",
        mapQuery = "Acropolis of Lindos, Rhodes",
        iconEmoji = "🏛️"
    ),
    PhotoSpot(
        id = "prasonisi_spit",
        title = "Prasonisi Sandbank",
        location = "Rhodos Südspitze",
        bestTime = "Nachmittag (15:00–17:30 Uhr)",
        timeCategory = "Vormittag",
        tip = "Klettert auf den Hügel der Halbinsel: Von oben sieht man die gigantische Sandbank, die zwei Meere trennt – im Vordergrund Hunderte Kitesurfer.",
        mapQuery = "Prasonisi Beach, Rhodes",
        iconEmoji = "🏄‍♂️"
    ),
    PhotoSpot(
        id = "kallithea_springs",
        title = "Kallithea Thermen Rotunde",
        location = "Kallithea Coast",
        bestTime = "Vormittag (09:30–11:30 Uhr)",
        timeCategory = "Vormittag",
        tip = "Nutzt die italienischen Bögen und das Kieselstein-Mosaik als natürlichen Rahmen für Portraits mit Blick aufs glasklare Meer.",
        mapQuery = "Kallithea Springs, Rhodes",
        iconEmoji = "🏛️"
    ),
    PhotoSpot(
        id = "agios_nikolaos_port",
        title = "Hafenkapelle Agios Nikolaos",
        location = "Kolymbia Harbour",
        bestTime = "Blaue Stunde (19:30–20:30 Uhr)",
        timeCategory = "Blaue Stunde",
        tip = "Die kleine weiß-blaue Kapelle direkt am Strand von Kolymbia leuchtet sanft im Abendlicht, während die Fischerboote ruhig im Hafen liegen.",
        mapQuery = "Kolymbia Beach Harbour, Rhodes",
        iconEmoji = "⛵"
    )
)
