package com.example.rhodoswidget

import androidx.annotation.StringRes
import java.net.URI

internal data class TravelSource(
    @param:StringRes val titleRes: Int,
    @param:StringRes val descriptionRes: Int,
    val url: String
)

internal data class ExcursionIdea(
    val id: String,
    @param:StringRes val titleRes: Int,
    @param:StringRes val metaRes: Int,
    @param:StringRes val descriptionRes: Int,
    val url: String,
    val mapQuery: String
)

internal data class EmergencyContact(
    val number: String,
    @param:StringRes val titleRes: Int
)

internal data class TravelChecklistItem(
    val id: String,
    @param:StringRes val titleRes: Int
)

internal val travelSources = listOf(
    TravelSource(
        titleRes = R.string.travel_ktel_title,
        descriptionRes = R.string.travel_ktel_description,
        url = "https://www.ktelrodou.gr/schedule/"
    ),
    TravelSource(
        titleRes = R.string.travel_roda_title,
        descriptionRes = R.string.travel_roda_description,
        url = "https://www.rhodes.gr/"
    )
)

internal val excursionIdeas = listOf(
    ExcursionIdea(
        id = "lindos",
        titleRes = R.string.travel_lindos_title,
        metaRes = R.string.travel_lindos_meta,
        descriptionRes = R.string.travel_lindos_description,
        url = "https://www.hh.gr/en/destinations/lindos/",
        mapQuery = "Acropolis of Lindos, Rhodes"
    ),
    ExcursionIdea(
        id = "old_town",
        titleRes = R.string.travel_old_town_title,
        metaRes = R.string.travel_old_town_meta,
        descriptionRes = R.string.travel_old_town_description,
        url = "https://archaeologicalmuseums.gr/en/museum/5df34af3deca5e2d79e8c140/palace-of-the-grand-master-of-knights",
        mapQuery = "Palace of the Grand Master of the Knights of Rhodes"
    ),
    ExcursionIdea(
        id = "seven_springs",
        titleRes = R.string.travel_seven_springs_title,
        metaRes = R.string.travel_seven_springs_meta,
        descriptionRes = R.string.travel_seven_springs_description,
        url = "https://visit-rhodes.gr/",
        mapQuery = "Seven Springs, Rhodes"
    ),
    ExcursionIdea(
        id = "tsambika",
        titleRes = R.string.travel_tsambika_title,
        metaRes = R.string.travel_tsambika_meta,
        descriptionRes = R.string.travel_tsambika_description,
        url = "https://visit-rhodes.gr/beaches/",
        mapQuery = "Tsambika Beach, Rhodes"
    )
)

internal val ferryAndEventSources = listOf(
    TravelSource(
        titleRes = R.string.travel_sebeco_title,
        descriptionRes = R.string.travel_sebeco_description,
        url = "https://www.sebeco.gr/en/"
    ),
    TravelSource(
        titleRes = R.string.travel_seadreams_title,
        descriptionRes = R.string.travel_seadreams_description,
        url = "https://seadreams.gr/timetable/timetable-marmaris/"
    ),
    TravelSource(
        titleRes = R.string.travel_events_title,
        descriptionRes = R.string.travel_events_description,
        url = "https://www.rhodes.gr/ekdilosis/"
    )
)

internal val emergencyContacts = listOf(
    EmergencyContact("100", R.string.travel_police),
    EmergencyContact("166", R.string.travel_ambulance),
    EmergencyContact("199", R.string.travel_fire),
    EmergencyContact("108", R.string.travel_coast_guard)
)

internal val travelChecklist = listOf(
    TravelChecklistItem("documents", R.string.travel_check_documents),
    TravelChecklistItem("insurance", R.string.travel_check_insurance),
    TravelChecklistItem("booking", R.string.travel_check_booking),
    TravelChecklistItem("driving", R.string.travel_check_driving),
    TravelChecklistItem("sun", R.string.travel_check_sun),
    TravelChecklistItem("charger", R.string.travel_check_charger),
    TravelChecklistItem("medicine", R.string.travel_check_medicine)
)

internal fun isTrustedTravelUrl(url: String): Boolean {
    val uri = runCatching { URI(url) }.getOrNull() ?: return false
    val host = uri.host?.lowercase() ?: return false
    return uri.scheme == "https" && host in TRUSTED_TRAVEL_HOSTS
}

private val TRUSTED_TRAVEL_HOSTS = setOf(
    "www.ktelrodou.gr",
    "www.rhodes.gr",
    "www.hh.gr",
    "archaeologicalmuseums.gr",
    "visit-rhodes.gr",
    "www.sebeco.gr",
    "seadreams.gr"
)
