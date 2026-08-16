package com.example.rhodoswidget

import androidx.annotation.DrawableRes

data class CompassEditorial(
    val duration: String,
    val bestTime: String,
    val fromHotel: String,
    val transport: String,
    val septemberNote: String,
    val combination: String,
    val returnTip: String,
    val facilities: String,
    val counterRecommendation: String,
    val checkedAt: String = "14.08.2026",
    val validUntil: String? = null,
    @param:DrawableRes val imageRes: Int
)

internal fun editorialFor(tip: CompassTip): CompassEditorial {
    val fallback = when (tip.category) {
        "Kolymbia" -> CompassEditorial(
            "1–2 Stunden", "Später Nachmittag", "Direkt in Kolymbia", "Zu Fuß",
            "Im September meist angenehm für einen ruhigen Ausklang.",
            "Mit einem Abendessen im Ort verbinden.", "Keine feste Rückfahrt nötig.",
            "Infrastruktur im Ferienort", "Weniger passend, wenn ihr bewusst Trubel sucht.",
            imageRes = R.drawable.relax_hotel_kolymbia
        )
        "Essen" -> CompassEditorial(
            "1–2 Stunden", "Abends", "Je nach Lokal", "Zu Fuß, Bus oder Taxi",
            "Öffnungstage und Reservierung im September kurz prüfen.",
            "Mit einem Spaziergang oder Strandabend verbinden.", "Rückweg vor dem Essen klären.",
            "Vom jeweiligen Lokal abhängig", "Nicht ideal, wenn ihr sehr knapp geplant seid.",
            imageRes = R.drawable.excursion_old_town
        )
        "Strände" -> CompassEditorial(
            "Halber Tag", "Vormittag", "Ab Kolymbia unterschiedlich", "Bus, Taxi oder Auto",
            "Im September bleibt das Meer meist warm; Wind und Rückfahrt tagesaktuell prüfen.",
            "Mit einer nahen Taverne verbinden.", "Letzte Busverbindung vorab prüfen.",
            "Liegen und Gastronomie nicht überall garantiert", "Weniger passend bei starkem Wind.",
            imageRes = R.drawable.excursion_tsambika
        )
        "Ausflüge" -> CompassEditorial(
            "Halber Tag", "Früh starten", "Ab Relax Hotel Kolymbia", "Bus oder Auto",
            "Im September können Öffnungszeiten und Verbindungen bereits reduziert sein.",
            "Mit einem passenden Strand- oder Essensstopp verbinden.", "Rückfahrt zuerst festlegen.",
            "Wasser und Sonnenschutz mitnehmen", "Weniger passend für einen reinen Ruhetag.",
            imageRes = R.drawable.excursion_lindos
        )
        "Mobilität" -> CompassEditorial(
            "10 Minuten Planung", "Am Vorabend", "Ab Relax Hotel Kolymbia", "Je nach Ziel",
            "Für eure Reise ab 20.09.2026 Fahrpläne erneut abgleichen.",
            "Direkt beim jeweiligen Ausflug anwenden.", "Hin- und Rückfahrt gemeinsam prüfen.",
            "Ticket und etwas Zeitpuffer einplanen", "Spontanität ist bei seltenen Linien riskant.",
            imageRes = R.drawable.relax_hotel_kolymbia
        )
        "Supermärkte" -> CompassEditorial(
            "30–60 Minuten", "Vormittags", "Ab Kolymbia", "Zu Fuß, Taxi oder Auto",
            "Öffnungszeiten und mögliche Sonntagsruhe vor dem Einkauf prüfen.",
            "Mit dem ersten oder letzten Weg des Tages verbinden.", "Einkäufe und Kühlung bei der Rückfahrt bedenken.",
            "Lebensmittel und Reisebedarf", "Für einzelne Getränke reicht oft ein näherer Minimarkt.",
            imageRes = R.drawable.rhodes_old_town_009
        )
        "Souvenirs" -> CompassEditorial(
            "30–60 Minuten", "Später Nachmittag", "Rund um Kolymbia", "Zu Fuß, Taxi oder Auto",
            "Saisonale Öffnungszeiten am Besuchstag prüfen.",
            "Mit einem Ortsbummel oder Abendessen verbinden.", "Zerbrechliche Einkäufe sicher transportieren.",
            "Handwerk, Geschenke und Mitbringsel", "Nicht unter Zeitdruck kaufen; Qualität und Herkunft ansehen.",
            imageRes = R.drawable.lindos_white_houses_bougainvillea_002
        )
        "Regionale Produkte" -> CompassEditorial(
            "45–90 Minuten", "Vormittags oder nachmittags", "Ab Kolymbia unterschiedlich", "Meist Taxi oder Auto",
            "Öffnungstage und Verkostungen im September vorher bestätigen.",
            "Mit Archangelos, Kolymbia oder Rhodos-Stadt verbinden.", "Gewicht und Flüssigkeitsregeln fürs Gepäck beachten.",
            "Olivenöl, Honig, Keramik und lokale Spezialitäten", "Preise vergleichen und Herkunft konkret erfragen.",
            imageRes = R.drawable.rhodos_1906335
        )
        else -> CompassEditorial(
            "Individuell", "Vor der Buchung", "Ab Kolymbia", "Je nach Unterkunft",
            "Leistungen und Saisonbetrieb im September bestätigen lassen.",
            "Mit euren Tageszielen abgleichen.", "Transfer vorab klären.",
            "Details beim Anbieter prüfen", "Nur sinnvoll, wenn Lage und Angebot zu euch passen.",
            imageRes = R.drawable.relax_hotel_kolymbia
        )
    }

    return when (tip.title) {
        "Taverne Akti" -> fallback.copy(
            duration = "1½–2 Stunden",
            bestTime = "Abends",
            combination = "Gut nach einem Strandtag.",
            counterRecommendation = "Nicht als gesicherte Empfehlung behandeln, falls die jüngsten Bewertungen deutlich abweichen.",
            imageRes = R.drawable.category_greek_food
        )
        "Stama" -> fallback.copy(
            duration = "1½ Stunden",
            bestTime = "Früher Abend",
            facilities = "Kleine Speisekarte – Auswahl vorher ansehen",
            counterRecommendation = "Weniger passend, wenn ihr eine sehr große Auswahl erwartet.",
            imageRes = R.drawable.greek_alley_whitewashed_019
        )
        "Tsambika / Tsampika" -> fallback.copy(
            fromHotel = "Etwa 15–20 Minuten per Taxi/Auto",
            transport = "Bus, Taxi oder Auto",
            septemberNote = "Auch im September beliebt; morgens meist entspannter.",
            combination = "Strandtag plus Tsambika-Kloster nur mit frühem Start.",
            facilities = "Liegen, Gastronomie, wenig natürlicher Schatten",
            imageRes = R.drawable.excursion_tsambika
        )
        "Stegna" -> fallback.copy(
            fromHotel = "Etwa 20 Minuten per Taxi/Auto",
            bestTime = "Später Vormittag bis Abend",
            combination = "Baden und anschließend Fisch oder Meze am Wasser.",
            counterRecommendation = "Weniger passend, wenn ihr einen reinen Sandstrand erwartet.",
            imageRes = R.drawable.tip_stegna_beach
        )
        "Pefkoi Plakia Beach" -> fallback.copy(
            duration = "Halber bis ganzer Tag",
            fromHotel = "Südlich von Lindos",
            transport = "Am einfachsten mit Auto",
            combination = "Mit Lindos nur verbinden, wenn ihr früh losfahrt.",
            returnTip = "Für die Rückfahrt genügend Fahrzeit einplanen.",
            imageRes = R.drawable.rhodos_52368489088_f2f1a9f33c_k
        )
        "Elli Beach" -> fallback.copy(
            fromHotel = "Rhodos-Stadt, längere Busfahrt",
            bestTime = "Vormittag oder später Nachmittag",
            combination = "Mit Mandraki und Altstadt verbinden.",
            facilities = "Städtische Infrastruktur in Laufnähe",
            imageRes = R.drawable.rhodes_greece_landscape_010
        )
        "Mandraki vor Sonnenaufgang" -> fallback.copy(
            duration = "1–2 Stunden",
            bestTime = "Sehr früh",
            fromHotel = "Rhodos-Stadt",
            transport = "Für Sonnenaufgang eher Auto oder Taxi",
            septemberNote = "Sonnenaufgangszeit kurz vor dem Ausflug prüfen.",
            combination = "Danach Frühstück und Altstadt.",
            returnTip = "Für den späteren Rückweg Busfahrplan prüfen.",
            imageRes = R.drawable.rhodos_1906329
        )
        "Mietwagenbedingungen prüfen" -> fallback.copy(
            bestTime = "Vor der Reservierung",
            duration = "15–20 Minuten",
            facilities = "Kaution, Selbstbeteiligung, Tankregel und Shuttle schriftlich sichern",
            counterRecommendation = "Nicht buchen, wenn zentrale Bedingungen nur mündlich zugesagt werden.",
            imageRes = R.drawable.arrival_day_rhodos
        )
        "Elysium bei Faliraki" -> fallback.copy(
            imageRes = R.drawable.rhodes_greece_landscape_014
        )
        "Kresten Palace" -> fallback.copy(
            imageRes = R.drawable.rhodes_greece_landscape_003
        )
        "Lydia Maris Resort" -> fallback.copy(
            imageRes = R.drawable.relax_hotel_kolymbia
        )
        "Esperides Beach Family" -> fallback.copy(
            imageRes = R.drawable.prasonisi_rhodes_006
        )
        "Blue Sea Beach" -> fallback.copy(
            imageRes = R.drawable.rhodos_02_tsambika_strand_sunset
        )
        "Melekouni probieren" -> fallback.copy(
            duration = "Kurzer Genussstopp",
            bestTime = "Zwischendurch",
            transport = "In Bäckereien und Läden suchen",
            combination = "Als Proviant oder kleines Mitbringsel.",
            returnTip = "Kein besonderer Rückweg nötig.",
            imageRes = R.drawable.rhodes_old_town_004
        )
        "Lakani auf der Karte suchen" -> fallback.copy(
            bestTime = "Abends",
            facilities = "Traditionelles Schmorgericht, nicht überall täglich verfügbar",
            combination = "Mit weiteren regionalen Vorspeisen teilen.",
            imageRes = R.drawable.rhodes_old_town_027
        )
        "Dodekanes-Gebäck testen" -> fallback.copy(
            duration = "30–60 Minuten",
            bestTime = "Zum Kaffee",
            combination = "Mit einem Orts- oder Altstadtbummel.",
            returnTip = "Kein besonderer Rückweg nötig.",
            imageRes = R.drawable.gemini_cropped_preview
        )
        "Sieben Quellen im Schatten" -> fallback.copy(
            duration = "2–3 Stunden",
            bestTime = "Vormittag oder heiße Mittagszeit",
            fromHotel = "Nahe Kolymbia",
            transport = "Saisonbus, Taxi oder Auto",
            combination = "Mit einem ruhigen Nachmittag in Kolymbia.",
            facilities = "Waldwege, Wasser, schmaler dunkler Tunnel",
            counterRecommendation = "Nicht ideal bei eingeschränkter Trittsicherheit oder Platzangst.",
            imageRes = R.drawable.excursion_seven_springs
        )
        "Kallithea: Architektur und Badestopp" -> fallback.copy(
            duration = "3–4 Stunden",
            bestTime = "Vormittag",
            fromHotel = "Richtung Rhodos-Stadt",
            combination = "Mit Rhodos-Stadt verbinden, wenn genug Zeit bleibt.",
            facilities = "Eintritt, Architektur, kleine Badebucht",
            imageRes = R.drawable.wp13333024_rhodes_greece_wallpapers
        )
        "Lindos gleich morgens" -> fallback.copy(
            duration = "Halber Tag",
            bestTime = "Zum Öffnungsbeginn",
            fromHotel = "Etwa 30–40 Minuten Richtung Süden",
            transport = "Bus oder Auto",
            combination = "Danach Baden in einer Bucht bei Lindos.",
            facilities = "Viele Stufen, wenig Schatten, feste Schuhe",
            counterRecommendation = "Nicht mittags beginnen, wenn Hitze schlecht vertragen wird.",
            imageRes = R.drawable.excursion_lindos
        )
        "Anthony-Quinn-Bucht" -> fallback.copy(
            duration = "3–4 Stunden",
            bestTime = "Früh am Vormittag",
            fromHotel = "Nördlich von Kolymbia",
            transport = "Bus, Taxi oder Auto",
            combination = "Mit Ladiko oder Faliraki verbinden.",
            facilities = "Kompakte Felsbucht, Badeschuhe sinnvoll",
            counterRecommendation = "Weniger passend, wenn ihr viel Sand und Platz braucht.",
            imageRes = R.drawable.anthony_quinn_bay_rhodes_001
        )
        "Prasonisi als ganzer Reisetag" -> fallback.copy(
            duration = "Ganzer Tag",
            bestTime = "Früh losfahren",
            fromHotel = "Weit im Süden",
            transport = "Am sinnvollsten mit Auto",
            combination = "Mit Dörfern im Süden statt weiteren großen Zielen.",
            facilities = "Wind, Sand, lange Fahrzeit",
            counterRecommendation = "Für einen kurzen oder entspannten Urlaubstag wahrscheinlich zu weit.",
            imageRes = R.drawable.prasonisi_rhodes_003
        )
        "September-Fahrplan neu prüfen" -> fallback.copy(
            duration = "5 Minuten",
            bestTime = "Ab 15.–19.09. und täglich vor Abfahrt",
            returnTip = "Screenshot oder Offline-Kopie der Rückfahrt speichern.",
            counterRecommendation = "Alte Sommerzeiten niemals ungeprüft übernehmen.",
            validUntil = "Vor jeder Fahrt neu prüfen",
            imageRes = R.drawable.category_rhodes_bus
        )
        "Rückfahrt zuerst planen" -> fallback.copy(
            duration = "5 Minuten",
            bestTime = "Vor jedem Busausflug",
            combination = "Danach erst Eintritt, Essen und Badestopp planen.",
            returnTip = "Letzte sichere Verbindung plus Alternative notieren.",
            imageRes = R.drawable.rhodes_greece_landscape_032
        )
        "Eukalyptusallee am Abend" -> fallback.copy(
            duration = "45–90 Minuten",
            bestTime = "Zur goldenen Stunde",
            combination = "Mit Abendessen oder Getränk in Kolymbia.",
            facilities = "Bequeme Schuhe und bei Dunkelheit Sichtbarkeit",
            imageRes = R.drawable.tip_kolymbia_eucalyptus
        )
        "Entspannter Strandtag vor Ort" -> fallback.copy(
            duration = "Halber oder ganzer Tag",
            bestTime = "Vormittag",
            fromHotel = "Kolymbia Beach",
            transport = "Je nach Hotellage zu Fuß",
            combination = "Mit einer kurzen Abendrunde statt weiter Fahrt.",
            facilities = "Liegen und Gastronomie saisonabhängig",
            imageRes = R.drawable.tip_kolymbia_beach
        )
        "Carrusel: griechische Klassiker" -> fallback.copy(
            duration = "1½–2 Stunden",
            bestTime = "Abends",
            fromHotel = "Zentral in Kolymbia",
            transport = "Je nach Hotel zu Fuß",
            septemberNote = "Tisch und Öffnungszeit für den gewünschten Abend kurz prüfen.",
            combination = "Mit der Eukalyptusallee am Abend verbinden.",
            returnTip = "Innerhalb Kolymbias meist zu Fuß möglich.",
            facilities = "Breite Karte, Grill, vegetarische Optionen, Kindermenü",
            counterRecommendation = "Weniger passend, wenn ihr eine kleine Feinschmeckerkarte sucht.",
            checkedAt = "16.08.2026",
            imageRes = R.drawable.tip_food_carrusel
        )
        "Meze bei Ouzaki teilen" -> fallback.copy(
            duration = "1½–2 Stunden",
            bestTime = "Früher Abend",
            fromHotel = "Zentral in Kolymbia",
            transport = "Je nach Hotel zu Fuß",
            septemberNote = "Öffnungstag und Reservierung kurz vor dem Besuch prüfen.",
            combination = "Ideal nach einem kurzen Orts- oder Strandspaziergang.",
            returnTip = "Innerhalb Kolymbias meist zu Fuß möglich.",
            facilities = "Griechische Meze und traditionelle Hauptgerichte",
            counterRecommendation = "Weniger passend, wenn ihr keine Teller teilen möchtet.",
            checkedAt = "16.08.2026",
            imageRes = R.drawable.tip_food_ouzaki
        )
        "To Palio Nisaki: Fisch am Meer" -> fallback.copy(
            duration = "1½–2 Stunden",
            bestTime = "Mittags oder früher Abend",
            fromHotel = "Direkt am Kolymbia Beach",
            transport = "Zu Fuß oder kurzes Taxi",
            septemberNote = "Laut eigener Website dienstags geschlossen; am Besuchstag nochmals prüfen.",
            combination = "Mit einem Strandtag oder Hafenbummel verbinden.",
            returnTip = "Für einen späten Besuch den Rückweg zum Hotel vorher klären.",
            facilities = "Tagesfang, Fisch-Meze, Meeresfrüchte und Parkplatz",
            counterRecommendation = "Bei Tagesfang vorab Gewicht und Preis bestätigen lassen.",
            checkedAt = "16.08.2026",
            validUntil = "Öffnungstag vor dem Besuch prüfen",
            imageRes = R.drawable.tip_food_palio_nisaki
        )
        "Rosso di Sera: Pizza-Abend" -> fallback.copy(
            duration = "1½ Stunden",
            bestTime = "Abends",
            fromHotel = "Athinon Street, Kolymbia",
            transport = "Je nach Hotel zu Fuß",
            septemberNote = "Abendbetrieb und Öffnungszeit am Besuchstag prüfen.",
            combination = "Als unkomplizierter Abend ohne längere Anfahrt.",
            returnTip = "Innerhalb Kolymbias meist zu Fuß möglich.",
            facilities = "Pizza, Pasta sowie vegetarische und vegane Optionen",
            counterRecommendation = "Nicht die erste Wahl, wenn ihr gezielt rhodische Küche probieren möchtet.",
            checkedAt = "16.08.2026",
            imageRes = R.drawable.tip_food_rosso_di_sera
        )
        "Anthoula: Taverne im Grünen" -> fallback.copy(
            duration = "1½–2 Stunden",
            bestTime = "Mittags oder abends",
            fromHotel = "Etwas außerhalb von Kolymbia",
            transport = "Taxi oder Auto",
            septemberNote = "Öffnungszeit und Rückfahrt am Ausflugstag bestätigen.",
            combination = "Sehr gut vor oder nach den Sieben Quellen.",
            returnTip = "Taxi-Rückfahrt vor dem Essen vereinbaren.",
            facilities = "Lokale Gerichte, Meze und Holzkohlegrill",
            counterRecommendation = "Ohne Auto weniger spontan als die Lokale im Ortszentrum.",
            checkedAt = "16.08.2026",
            imageRes = R.drawable.tip_food_anthoula
        )
        "Taverna Michel: familiär und vom Grill" -> fallback.copy(
            fromHotel = "Zentral in Kolymbia",
            transport = "Je nach Hotel zu Fuß",
            combination = "Mit einem Abendspaziergang in Kolymbia.",
            facilities = "Grillgerichte, Meze, Pizza und Gyros",
            counterRecommendation = "Die jüngsten Rezensionen sind gemischt; nicht nur nach der Gesamtwertung entscheiden.",
            checkedAt = "16.08.2026",
            imageRes = R.drawable.rhodos_1906335
        )
        "Taverna Tsambikos: klassische Küche" -> fallback.copy(
            fromHotel = "Etwas außerhalb von Kolymbia",
            transport = "Taxi oder Auto",
            combination = "Mit Sieben Quellen oder einem ruhigen Abend.",
            returnTip = "Taxi-Rückfahrt vorab klären.",
            facilities = "Grill, griechische Hausmannskost und vegetarische Optionen",
            checkedAt = "16.08.2026",
            imageRes = R.drawable.lindos_white_houses_bougainvillea_001
        )
        "Food Box: schnell und unkompliziert" -> fallback.copy(
            duration = "30–60 Minuten",
            bestTime = "Mittags oder früher Abend",
            fromHotel = "Eukalyptusstraße, Kolymbia",
            transport = "Je nach Hotel zu Fuß",
            combination = "Als schneller Stopp vor oder nach dem Strand.",
            facilities = "Gyros, Burger, Grill, Salate und Take-away",
            counterRecommendation = "Weniger passend für einen langen, ruhigen Restaurantabend.",
            checkedAt = "16.08.2026",
            imageRes = R.drawable.rhodes_old_town_019
        )
        "Limanaki: Meeresblick und Seafood" -> fallback.copy(
            duration = "1½–2 Stunden",
            bestTime = "Vor Sonnenuntergang",
            fromHotel = "An der Küste von Kolymbia",
            transport = "Zu Fuß, Taxi oder Auto je nach Hotel",
            combination = "Mit einem Strandnachmittag verbinden.",
            facilities = "Fisch, Meeresfrüchte, Pasta, Cocktails und Aussicht",
            counterRecommendation = "Die Aussicht wird konstanter gelobt als Essen und Preis-Leistung.",
            checkedAt = "16.08.2026",
            imageRes = R.drawable.rhodes_greece_landscape_027
        )
        "Mylos: besonderer Abend am Meer" -> fallback.copy(
            duration = "2 Stunden",
            bestTime = "Abends mit Reservierung",
            fromHotel = "Atlantica Imperial Resort",
            transport = "Zu Fuß oder kurzes Taxi je nach Hotel",
            combination = "Als eigener besonderer Abend.",
            facilities = "Gehobene À-la-carte-Küche und Meerblick",
            counterRecommendation = "Zugang für externe Gäste und Preis unbedingt vorab bestätigen.",
            checkedAt = "16.08.2026",
            imageRes = R.drawable.rhodos_392463
        )
        "Sklavenitis Kolymbia: großer Einkauf" -> fallback.copy(
            fromHotel = "An der Rhodos–Lindos-Straße",
            transport = "Auto oder Taxi",
            combination = "Gut direkt nach Ankunft oder vor einem Ausflug mit Auto.",
            facilities = "Frischetheken, Backwaren, Fertiggerichte und Parkplatz",
            counterRecommendation = "Für einen kleinen Einkauf ist der Weg vom Hotel möglicherweise unnötig.",
            checkedAt = "16.08.2026",
            validUntil = "Öffnungszeit vorab prüfen",
            imageRes = R.drawable.rhodes_old_town_009
        )
        "Zeus: zentraler Ferienmarkt" -> fallback.copy(
            fromHotel = "Zentral in Kolymbia",
            transport = "Je nach Hotel zu Fuß",
            combination = "Mit Eukalyptusallee, Strand oder Abendessen.",
            facilities = "Getränke, Snacks, Strandbedarf und Souvenirs",
            counterRecommendation = "Bei einem großen Warenkorb mit einem Vollsortimenter vergleichen.",
            checkedAt = "16.08.2026",
            imageRes = R.drawable.rhodes_greece_landscape_025
        )
        "Secret Rhodes: handgemachte Andenken" -> fallback.copy(
            fromHotel = "Eukalyptusstraße, Kolymbia",
            transport = "Je nach Hotel zu Fuß",
            combination = "Mit einem Abendbummel auf der Eukalyptusstraße.",
            facilities = "Keramik, Schmuck, Naturkosmetik und Olivenölseife",
            counterRecommendation = "Kleine Bewertungsbasis; Angebot und Öffnungszeiten können sich ändern.",
            checkedAt = "16.08.2026",
            imageRes = R.drawable.lindos_white_houses_bougainvillea_002
        )
        "Michail: Keramik aus Familienhand" -> fallback.copy(
            fromHotel = "Bei Kolymbia",
            transport = "Taxi oder Auto",
            combination = "Mit Artistic Village oder einer Fahrt Richtung Afandou.",
            facilities = "Handgefertigte Keramik und Kunsthandwerk",
            counterRecommendation = "Für einen spontanen Fußweg aus dem Ferienzentrum weniger geeignet.",
            checkedAt = "16.08.2026",
            imageRes = R.drawable.kfuhlert_rhodes_4404841_1920
        )
        "Artistic Village: Keramik und Galerie" -> fallback.copy(
            duration = "1–1½ Stunden",
            fromHotel = "Zwischen Afandou und Kolymbia",
            transport = "Auto oder Taxi",
            combination = "Mit Afandou, Tsambika oder einem weiteren Keramikstopp.",
            facilities = "Werkstatt, Galerie, Museum und Verkauf",
            checkedAt = "16.08.2026",
            imageRes = R.drawable.rhodos_1906364
        )
        "Olive Oil Factory: Öl mit Einblick" -> fallback.copy(
            duration = "45–90 Minuten",
            fromHotel = "Archangelos",
            transport = "Auto oder Taxi",
            combination = "Mit Stegna oder einem Stopp in Archangelos.",
            facilities = "Ölherstellung, Verkostung und Verkauf",
            counterRecommendation = "Einzelne Rezensionen kritisieren hohe Verkaufspreise; vorher vergleichen.",
            checkedAt = "16.08.2026",
            imageRes = R.drawable.rhodes_old_town_015
        )
        "Traditional Greek Kiosk: probieren und mitnehmen" -> fallback.copy(
            duration = "30–60 Minuten",
            fromHotel = "Symi-Platz, Rhodos-Stadt",
            transport = "Bus oder Auto nach Rhodos-Stadt",
            combination = "Mit Mandraki und Altstadt verbinden.",
            returnTip = "Rückfahrt nach Kolymbia vorher festlegen.",
            facilities = "Olivenöl, Honig, Kräuter, Gewürze und Liköre",
            checkedAt = "16.08.2026",
            imageRes = R.drawable.dimitrisvetsikas1969_castle_7462448_1920
        )
        else -> fallback
    }
}

internal fun featuredCompassTip(tips: List<CompassTip>, dayOfYear: Int): CompassTip =
    tips[Math.floorMod(dayOfYear - 1, tips.size)]

internal fun buildCompassDayPlan(tips: List<CompassTip>, savedIds: Set<String>): List<CompassTip> {
    val timeOrder = listOf("Sehr früh", "Zum Öffnungsbeginn", "Früh am Vormittag", "Vormittag", "Abends")
    return tips
        .filter { it.id in savedIds }
        .sortedBy { tip ->
            val bestTime = editorialFor(tip).bestTime
            timeOrder.indexOfFirst { bestTime.contains(it, ignoreCase = true) }
                .takeIf { it >= 0 } ?: timeOrder.size
        }
        .take(3)
}
