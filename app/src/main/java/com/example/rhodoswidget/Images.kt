package com.example.rhodoswidget

import android.content.Context
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Tagesrotation fuer die Widget-Hintergrundbilder.
 *
 * Die Reihenfolge wurde einmalig mit festem Seed (42) gewuerfelt und ist
 * hier hart kodiert, damit das gleiche Bild bei jedem Build am selben Tag
 * erscheint. Aenderungen an der Reihenfolge wirken sich sofort aus.
 */
object Images {

    private val resourceNames = listOf(
        "prasonisi_rhodes_023",
        "rhodes_old_town_027",
        "rhodos_1906364",
        "rhodos_02_tsambika_strand_sunset",
        "greek_alley_whitewashed_019",
        "rhodos_1364599",
        "rhodes_island_castle_fg0v9vo1hwtndkqc",
        "lindos_white_houses_bougainvillea_001",
        "rhodos_1906323",
        "palace_of_the_grand_master_rhodes_005",
        "anthony_quinn_bay_rhodes_001",
        "rhodes_old_town_004",
        "rhodes_greece_landscape_032",
        "relax_hotel_kolymbia",
        "prasonisi_rhodes_006",
        "wp13333024_rhodes_greece_wallpapers",
        "lindos_donkey_path_002",
        "rhodes_greece_landscape_027",
        "rhodos_1906335",
        "kfuhlert_rhodes_4404841_1920",
        "prasonisi_rhodes_003",
        "rhodes_old_town_015",
        "gemini_cropped_preview",
        "rhodes_greece_landscape_010",
        "rhodos_392463",
        "dimitrisvetsikas1969_castle_7462448_1920",
        "rhodes_greece_landscape_014",
        "lindos_white_houses_bougainvillea_002",
        "rhodos_52368489088_f2f1a9f33c_k",
        "rhodes_old_town_009",
        "prasonisi_rhodes_024",
        "wallpaperswide_com_lindos_village_rhodes_island_greece_wallpaper_1920x1080",
        "rhodes_old_town_019",
        "rhodes_greece_landscape_025",
        "rhodos_1906329",
        "rhodes_greece_landscape_003",
        "prasonisi_rhodes_028",
        "rhodos_1906326",
        "rhodos_1906319",
        "relax_hotel_kolymbia"
    )

    private const val PREFS_SETTINGS = "rhodos_settings"
    private const val KEY_PINNED_IMAGE = "pinned_image_name"

    val allImageNames: List<String> = resourceNames.distinct()

    fun getPinnedImage(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PINNED_IMAGE, null)
    }

    fun setPinnedImage(context: Context, name: String?) {
        val prefs = context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PINNED_IMAGE, name).apply()
    }

    fun currentImageName(context: Context): String {
        val pinned = getPinnedImage(context)
        if (pinned != null && pinned != "auto") return pinned

        val now = Calendar.getInstance()
        val localMillis = now.timeInMillis +
            now.get(Calendar.ZONE_OFFSET) + now.get(Calendar.DST_OFFSET)
        val daysSinceEpoch = TimeUnit.MILLISECONDS.toDays(localMillis)
        val index = (daysSinceEpoch % resourceNames.size).toInt()
        return resourceNames[index]
    }

    /** Drawable-Resource fuer das heutige Hintergrundbild. */
    fun resourceOfTheDay(context: Context): Int {
        val name = currentImageName(context)
        return context.resources.getIdentifier(name, "drawable", context.packageName)
    }

    fun displayNameOf(resourceName: String): String = when (resourceName) {
        "prasonisi_rhodes_023" -> "Doppelbucht Porto Timoni (Korfu)"
        "rhodes_old_town_027" -> "Mittelalterliche Gasse mit Festungsblick"
        "rhodos_1906364" -> "Akropolis-Säulen in Lindos"
        "rhodos_02_tsambika_strand_sunset" -> "Strandsonnenuntergang im Abendrot"
        "greek_alley_whitewashed_019" -> "Hafen von Chania (Kreta)"
        "rhodos_1364599" -> "Malerischer Sandstrand mit Klippe"
        "rhodes_island_castle_fg0v9vo1hwtndkqc" -> "Mandraki-Hafen bei Nacht"
        "lindos_white_houses_bougainvillea_001" -> "Weiße Gasse mit blauen Türen"
        "rhodos_1906323" -> "St. Pauls Bucht (Paulusbucht)"
        "palace_of_the_grand_master_rhodes_005" -> "Fort St. Nikolaus mit Leuchtturm"
        "anthony_quinn_bay_rhodes_001" -> "Malerische Felsbucht"
        "rhodes_old_town_004" -> "Historische Altstadt mit Meerblick"
        "rhodes_greece_landscape_032" -> "Festungsmauer der Akropolis von Lindos"
        "relax_hotel_kolymbia" -> "Unser Relax Hotel in Kolymbia"
        "prasonisi_rhodes_006" -> "Westküste Rhodos mit Paraglider"
        "wp13333024_rhodes_greece_wallpapers" -> "Hippokrates-Platz (Altstadt)"
        "lindos_donkey_path_002" -> "Eselspfad in den Bergen"
        "rhodes_greece_landscape_027" -> "Dreimaster-Segelschiff bei Sonnenuntergang"
        "rhodos_1906335" -> "Byzantinisches Kloster Tharri"
        "kfuhlert_rhodes_4404841_1920" -> "Akropolis von Lindos"
        "prasonisi_rhodes_003" -> "Fischerdorf Limeni (Mani)"
        "rhodes_old_town_015" -> "Stadtgraben mit Steinkugeln (Altstadt)"
        "gemini_cropped_preview" -> "Romantische Gasse mit Meerblick"
        "rhodes_greece_landscape_010" -> "Elli Beach (Rhodos Stadt)"
        "rhodos_392463" -> "Windmühle auf Mykonos"
        "dimitrisvetsikas1969_castle_7462448_1920" -> "Hirsch-Statuen am Mandraki-Hafen"
        "rhodes_greece_landscape_014" -> "Panorama der Bucht von Lindos"
        "lindos_white_houses_bougainvillea_002" -> "Ausblick aufs Meer mit Bougainvillea"
        "rhodos_52368489088_f2f1a9f33c_k" -> "Anthony Quinn Bucht"
        "rhodes_old_town_009" -> "Blick über das weiße Dorf Lindos"
        "prasonisi_rhodes_024" -> "Ochsenbauchbucht Voidokilia"
        "wallpaperswide_com_lindos_village_rhodes_island_greece_wallpaper_1920x1080" -> "Postkartenblick auf Lindos"
        "rhodes_old_town_019" -> "Hafenstadt Nafplio mit Festungsblick"
        "rhodes_greece_landscape_025" -> "Akropolis-Festung über Lindos"
        "rhodos_1906329" -> "Hafenpromenade am Mandraki-Hafen"
        "rhodes_greece_landscape_003" -> "Antike Ruinen über dem Meer"
        "prasonisi_rhodes_028" -> "Apella Strand (Karpathos)"
        "rhodos_1906326" -> "Lindos Dorf mit Festung"
        "rhodos_1906319" -> "Lindos Ansicht mit Akropolis"
        "arrival_day_rhodos" -> "Traumblick auf Lindos (Reisetag)"
        else -> "Rhodos Impression"
    }
}
