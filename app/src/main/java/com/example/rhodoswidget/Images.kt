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
        "prasonisi_rhodes_023" -> "Prasonisi Halbinsel"
        "rhodes_old_town_027" -> "Rhodos Altstadt Gasse"
        "rhodos_1906364" -> "Küstenstraße bei Lindos"
        "rhodos_02_tsambika_strand_sunset" -> "Tsambika Strand Sonnenuntergang"
        "greek_alley_whitewashed_019" -> "Griechische Gasse"
        "rhodos_1364599" -> "Blick auf Lindos"
        "rhodes_island_castle_fg0v9vo1hwtndkqc" -> "Festung von Monolithos"
        "lindos_white_houses_bougainvillea_001" -> "Lindos mit Bougainvillea"
        "rhodos_1906323" -> "Akropolis von Lindos"
        "palace_of_the_grand_master_rhodes_005" -> "Großmeisterpalast"
        "anthony_quinn_bay_rhodes_001" -> "Anthony Quinn Bucht"
        "rhodes_old_town_004" -> "Mittelalterliche Stadtmauer"
        "rhodes_greece_landscape_032" -> "Bucht von Haraki"
        "relax_hotel_kolymbia" -> "Unser Relax Hotel"
        "prasonisi_rhodes_006" -> "Wellen in Prasonisi"
        "wp13333024_rhodes_greece_wallpapers" -> "Hafen von Mandraki"
        "lindos_donkey_path_002" -> "Lindos Eselspfad"
        "rhodes_greece_landscape_027" -> "Küstenlinie Rhodos"
        "rhodos_1906335" -> "Ostküste Ausblick"
        "kfuhlert_rhodes_4404841_1920" -> "St. Pauls Bucht"
        "prasonisi_rhodes_003" -> "Surfer-Strand Prasonisi"
        "rhodes_old_town_015" -> "Großmeisterpalast Eingang"
        "gemini_cropped_preview" -> "Lindos Bucht Panorama"
        "rhodes_greece_landscape_010" -> "Strand von Tsambika"
        "rhodos_392463" -> "Taverne am Meer"
        "dimitrisvetsikas1969_castle_7462448_1920" -> "Kritinia Burgruine"
        "rhodes_greece_landscape_014" -> "Bucht von Ladiko"
        "lindos_white_houses_bougainvillea_002" -> "Weiße Gasse in Lindos"
        "rhodos_52368489088_f2f1a9f33c_k" -> "Mandraki Hirsche"
        "rhodes_old_town_009" -> "Ritterstraße in der Altstadt"
        "prasonisi_rhodes_024" -> "Kitesurf-Paradies Prasonisi"
        "wallpaperswide_com_lindos_village_rhodes_island_greece_wallpaper_1920x1080" -> "Lindos bei Nacht"
        "rhodes_old_town_019" -> "Tor zur Altstadt"
        "rhodes_greece_landscape_025" -> "Felsküste im Westen"
        "rhodos_1906329" -> "Lindos Bucht Ausblick"
        "rhodes_greece_landscape_003" -> "Sieben Quellen (Epta Piges)"
        "prasonisi_rhodes_028" -> "Prasonisi Landzunge"
        "rhodos_1906326" -> "Lindos Häuser"
        "rhodos_1906319" -> "Akropolis Säulen Lindos"
        else -> "Rhodos Impression"
    }
}
