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

    /** Drawable-Resource fuer das heutige Hintergrundbild. */
    fun resourceOfTheDay(context: Context): Int {
        val now = Calendar.getInstance()
        val localMillis = now.timeInMillis +
            now.get(Calendar.ZONE_OFFSET) + now.get(Calendar.DST_OFFSET)
        val daysSinceEpoch = TimeUnit.MILLISECONDS.toDays(localMillis)
        val index = (daysSinceEpoch % resourceNames.size).toInt()
        val name = resourceNames[index]
        return context.resources.getIdentifier(name, "drawable", context.packageName)
    }
}
