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

    private data class ImageAsset(val name: String, val resId: Int)

    private val resources = listOf(
        ImageAsset("prasonisi_rhodes_023", R.drawable.prasonisi_rhodes_023),
        ImageAsset("rhodes_old_town_027", R.drawable.rhodes_old_town_027),
        ImageAsset("rhodos_1906364", R.drawable.rhodos_1906364),
        ImageAsset("rhodos_02_tsambika_strand_sunset", R.drawable.rhodos_02_tsambika_strand_sunset),
        ImageAsset("greek_alley_whitewashed_019", R.drawable.greek_alley_whitewashed_019),
        ImageAsset("rhodos_1364599", R.drawable.rhodos_1364599),
        ImageAsset("rhodes_island_castle_fg0v9vo1hwtndkqc", R.drawable.rhodes_island_castle_fg0v9vo1hwtndkqc),
        ImageAsset("lindos_white_houses_bougainvillea_001", R.drawable.lindos_white_houses_bougainvillea_001),
        ImageAsset("rhodos_1906323", R.drawable.rhodos_1906323),
        ImageAsset("palace_of_the_grand_master_rhodes_005", R.drawable.palace_of_the_grand_master_rhodes_005),
        ImageAsset("anthony_quinn_bay_rhodes_001", R.drawable.anthony_quinn_bay_rhodes_001),
        ImageAsset("rhodes_old_town_004", R.drawable.rhodes_old_town_004),
        ImageAsset("rhodes_greece_landscape_032", R.drawable.rhodes_greece_landscape_032),
        ImageAsset("relax_hotel_kolymbia", R.drawable.relax_hotel_kolymbia),
        ImageAsset("prasonisi_rhodes_006", R.drawable.prasonisi_rhodes_006),
        ImageAsset("wp13333024_rhodes_greece_wallpapers", R.drawable.wp13333024_rhodes_greece_wallpapers),
        ImageAsset("lindos_donkey_path_002", R.drawable.lindos_donkey_path_002),
        ImageAsset("rhodes_greece_landscape_027", R.drawable.rhodes_greece_landscape_027),
        ImageAsset("rhodos_1906335", R.drawable.rhodos_1906335),
        ImageAsset("kfuhlert_rhodes_4404841_1920", R.drawable.kfuhlert_rhodes_4404841_1920),
        ImageAsset("prasonisi_rhodes_003", R.drawable.prasonisi_rhodes_003),
        ImageAsset("rhodes_old_town_015", R.drawable.rhodes_old_town_015),
        ImageAsset("gemini_cropped_preview", R.drawable.gemini_cropped_preview),
        ImageAsset("rhodes_greece_landscape_010", R.drawable.rhodes_greece_landscape_010),
        ImageAsset("rhodos_392463", R.drawable.rhodos_392463),
        ImageAsset("dimitrisvetsikas1969_castle_7462448_1920", R.drawable.dimitrisvetsikas1969_castle_7462448_1920),
        ImageAsset("rhodes_greece_landscape_014", R.drawable.rhodes_greece_landscape_014),
        ImageAsset("lindos_white_houses_bougainvillea_002", R.drawable.lindos_white_houses_bougainvillea_002),
        ImageAsset("rhodos_52368489088_f2f1a9f33c_k", R.drawable.rhodos_52368489088_f2f1a9f33c_k),
        ImageAsset("rhodes_old_town_009", R.drawable.rhodes_old_town_009),
        ImageAsset("prasonisi_rhodes_024", R.drawable.prasonisi_rhodes_024),
        ImageAsset(
            "wallpaperswide_com_lindos_village_rhodes_island_greece_wallpaper_1920x1080",
            R.drawable.wallpaperswide_com_lindos_village_rhodes_island_greece_wallpaper_1920x1080
        ),
        ImageAsset("rhodes_old_town_019", R.drawable.rhodes_old_town_019),
        ImageAsset("rhodes_greece_landscape_025", R.drawable.rhodes_greece_landscape_025),
        ImageAsset("rhodos_1906329", R.drawable.rhodos_1906329),
        ImageAsset("rhodes_greece_landscape_003", R.drawable.rhodes_greece_landscape_003),
        ImageAsset("prasonisi_rhodes_028", R.drawable.prasonisi_rhodes_028),
        ImageAsset("rhodos_1906326", R.drawable.rhodos_1906326),
        ImageAsset("rhodos_1906319", R.drawable.rhodos_1906319),
        ImageAsset("relax_hotel_kolymbia", R.drawable.relax_hotel_kolymbia)
    )

    private val resourceNames = resources.map { it.name }
    private val resourcesByName = resources.associateBy { it.name }

    private const val PREFS_SETTINGS = "rhodos_settings"
    private const val KEY_PINNED_IMAGE = "pinned_image_name"
    private const val KEY_BACKGROUND_DIM = "background_dim"
    private const val DEFAULT_BACKGROUND_DIM = 0.65f

    val allImageNames: List<String> = resourceNames.distinct()

    fun getPinnedImage(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PINNED_IMAGE, null)
    }

    fun setPinnedImage(context: Context, name: String?) {
        val prefs = context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PINNED_IMAGE, name).apply()
    }

    fun getBackgroundDim(context: Context): Float =
        context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE)
            .getFloat(KEY_BACKGROUND_DIM, DEFAULT_BACKGROUND_DIM)
            .coerceIn(0.4f, 0.9f)

    fun setBackgroundDim(context: Context, value: Float) {
        context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_BACKGROUND_DIM, value.coerceIn(0.4f, 0.9f))
            .apply()
    }

    fun currentImageName(context: Context): String {
        val pinned = getPinnedImage(context)
        if (pinned != null && pinned != "auto" && pinned in allImageNames) return pinned

        return rotationImageName()
    }

    fun rotationImageName(): String {
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
        return resourceOf(name)
    }

    fun resourceOf(resourceName: String): Int =
        resourcesByName[resourceName]?.resId ?: resources.first().resId

    fun displayNameOf(resourceName: String): String = when (resourceName) {
        "prasonisi_rhodes_023" -> "Doppelbucht Porto Timoni (Korfu)"
        "rhodes_old_town_027" -> "Dubrovnik mit Festungsblick (Kroatien)"
        "rhodos_1906364" -> "Dorisches Propylon auf der Akropolis von Lindos"
        "rhodos_02_tsambika_strand_sunset" -> "Sonnenuntergang am Tsambika Beach"
        "greek_alley_whitewashed_019" -> "Hafen von Chania (Kreta)"
        "rhodos_1364599" -> "Malerischer Kieselstrand mit Felsklippe"
        "rhodes_island_castle_fg0v9vo1hwtndkqc" -> "Großmeisterpalast und Hafen bei Nacht"
        "lindos_white_houses_bougainvillea_001" -> "Kykladische Gasse mit rotem Fahrrad"
        "rhodos_1906323" -> "St. Pauls Bucht (Paulusbucht)"
        "palace_of_the_grand_master_rhodes_005" -> "Fort St. Nikolaus mit Leuchtturm"
        "anthony_quinn_bay_rhodes_001" -> "Anthony-Quinn-Bucht bei Dämmerung"
        "rhodes_old_town_004" -> "Historische Altstadt von Korfu"
        "rhodes_greece_landscape_032" -> "Festungsmauer der Akropolis von Lindos"
        "relax_hotel_kolymbia" -> "Unser Relax Hotel in Kolymbia"
        "prasonisi_rhodes_006" -> "Westküste Rhodos mit Paraglider"
        "wp13333024_rhodes_greece_wallpapers" -> "Hippokrates-Platz (Altstadt)"
        "lindos_donkey_path_002" -> "Esel-Karawane im Atlasgebirge (Marokko)"
        "rhodes_greece_landscape_027" -> "Segelschiff im goldenen Sonnenuntergang"
        "rhodos_1906335" -> "Byzantinisches Kloster Tharri"
        "kfuhlert_rhodes_4404841_1920" -> "Akropolis von Lindos"
        "prasonisi_rhodes_003" -> "Fischerdorf Limeni (Mani)"
        "rhodes_old_town_015" -> "Stadtgraben mit Steinkugeln (Altstadt)"
        "gemini_cropped_preview" -> "Romantische Gasse mit Meerblick"
        "rhodes_greece_landscape_010" -> "Elli Beach (Rhodos Stadt)"
        "rhodos_392463" -> "Windmühle auf Mykonos"
        "dimitrisvetsikas1969_castle_7462448_1920" -> "Hirsch-Statuen am Mandraki-Hafen"
        "rhodes_greece_landscape_014" -> "Panorama der Bucht von Lindos"
        "lindos_white_houses_bougainvillea_002" -> "Meerblick gerahmt von Bougainvillea"
        "rhodos_52368489088_f2f1a9f33c_k" -> "Morgenlicht in der Anthony-Quinn-Bucht"
        "rhodes_old_town_009" -> "Blick über das weiße Dorf Lindos"
        "prasonisi_rhodes_024" -> "Ochsenbauchbucht Voidokilia"
        "wallpaperswide_com_lindos_village_rhodes_island_greece_wallpaper_1920x1080" -> "Postkartenblick auf Lindos"
        "rhodes_old_town_019" -> "Hafenstadt Nafplio mit Festungsblick"
        "rhodes_greece_landscape_025" -> "Akropolis-Festung über Lindos"
        "rhodos_1906329" -> "Parkbank am Mandraki-Hafen bei Sonnenaufgang"
        "rhodes_greece_landscape_003" -> "Akropolis-Festungsmauer und Säulen in Lindos"
        "prasonisi_rhodes_028" -> "Apella Strand (Karpathos)"
        "rhodos_1906326" -> "Lindos Dorf mit Festung"
        "rhodos_1906319" -> "Lindos Ansicht mit Akropolis"
        "arrival_day_rhodos" -> "Traumblick auf Lindos (Reisetag)"
        else -> "Rhodos Impression"
    }
}
