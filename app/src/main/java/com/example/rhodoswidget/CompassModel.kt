package com.example.rhodoswidget

data class CompassTip(
    val category: String,
    val title: String,
    val description: String,
    val note: String,
    val kind: CompassTipKind = CompassTipKind.RECOMMENDATION,
    val location: String = "",
    val journey: String = "",
    val tags: List<String> = emptyList(),
    val reviewSummary: String? = null,
    val source: CompassTipSource = CompassTipSource.COMMUNITY,
    val sourceUrl: String? = null,
    val mapsUrl: String? = null,
    val id: String = ""
)

enum class CompassTipSource(val label: String) {
    COMMUNITY("COMMUNITY"),
    RESEARCHED("RECHERCHIERT")
}

enum class CompassTipKind(val label: String) {
    RECOMMENDATION("EMPFOHLEN"),
    NOTE("HINWEIS"),
    CAUTION("BEACHTEN")
}

internal val compassTipIds = listOf(
    "food-taverne-akti",
    "food-stama",
    "beach-tsambika",
    "beach-stegna",
    "beach-pefkoi-plakia",
    "beach-elli",
    "trip-mandraki-sunrise",
    "mobility-rental-terms",
    "hotel-elysium",
    "hotel-kresten-palace",
    "hotel-lydia-maris",
    "hotel-esperides-beach",
    "hotel-blue-sea-beach",
    "food-melekouni",
    "food-lakani",
    "food-dodecanese-pastry",
    "trip-seven-springs",
    "trip-kallithea",
    "trip-lindos-morning",
    "trip-anthony-quinn-bay",
    "trip-prasonisi",
    "mobility-september-schedule",
    "mobility-plan-return-first",
    "kolymbia-eucalyptus-evening",
    "kolymbia-local-beach-day",
    "food-kolymbia-carrusel",
    "food-kolymbia-ouzaki",
    "food-kolymbia-palio-nisaki",
    "food-kolymbia-rosso-di-sera",
    "food-kolymbia-anthoula",
    "food-kolymbia-michel",
    "food-kolymbia-tsambikos",
    "food-kolymbia-food-box",
    "food-kolymbia-limanaki",
    "food-kolymbia-mylos",
    "shopping-supermarket-sklavenitis-kolymbia",
    "shopping-supermarket-zeus-kolymbia",
    "shopping-supermarket-edem-kolymbia",
    "shopping-supermarket-michalis-kiosk",
    "shopping-fashion-hashtag-kolymbia",
    "shopping-souvenir-oslo-street",
    "shopping-souvenir-secret-rhodes",
    "shopping-souvenir-michail-artistic",
    "shopping-local-artistic-village",
    "shopping-local-olive-oil-factory",
    "shopping-local-traditional-greek-kiosk"
)

internal fun attachCompassTipIds(tips: List<CompassTip>): List<CompassTip> {
    require(tips.size == compassTipIds.size) { "Every compass tip needs a stable ID" }
    require(generatedCompassTipIds == compassTipIds.toSet()) {
        "Google-Maps-Links.csv must contain every compass tip ID exactly once"
    }
    return tips.mapIndexed { index, tip ->
        val id = compassTipIds[index]
        tip.copy(id = id, mapsUrl = generatedCompassTipMapsUrls[id])
    }
}

internal fun filterCompassTips(
    tips: List<CompassTip>,
    query: String,
    category: String? = null
): List<CompassTip> {
    val normalizedQuery = query.trim().lowercase()
    return tips.filter { tip ->
        val matchesCategory = category == null || tip.category == category
        val searchable = listOf(
            tip.category,
            tip.title,
            tip.description,
            tip.note,
            tip.location,
            tip.journey,
            tip.reviewSummary.orEmpty(),
            tip.tags.joinToString(" ")
        ).joinToString(" ").lowercase()
        matchesCategory && (normalizedQuery.isEmpty() || normalizedQuery in searchable)
    }
}

internal fun matchesCompassQuickFilter(tip: CompassTip, filter: String): Boolean = when (filter) {
    "Ohne Auto" -> tip.tags.any { it.contains("ohne Auto", ignoreCase = true) } ||
        tip.journey.contains("zu Fuß", ignoreCase = true) ||
        tip.journey.contains("Bus", ignoreCase = true)
    "Halber Tag" -> tip.tags.any { it.contains("Halbtag", ignoreCase = true) } ||
        editorialFor(tip).duration.contains("Halber", ignoreCase = true) ||
        editorialFor(tip).duration.contains("2–3", ignoreCase = true) ||
        editorialFor(tip).duration.contains("3–4", ignoreCase = true)
    "Ruhig" -> tip.tags.any { it.contains("ruhig", ignoreCase = true) } ||
        tip.description.contains("ruhig", ignoreCase = true) ||
        tip.note.contains("entspannt", ignoreCase = true)
    "Essen" -> tip.category == "Essen" || tip.tags.any { it.contains("Taverne", ignoreCase = true) }
    else -> true
}
