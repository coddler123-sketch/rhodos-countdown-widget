package com.example.rhodoswidget.ui.travel
import com.example.rhodoswidget.*

import com.example.rhodoswidget.R
import com.example.rhodoswidget.MainActivity
import com.example.rhodoswidget.ui.home.*
import com.example.rhodoswidget.ui.compass.*
import com.example.rhodoswidget.ui.travel.*
import com.example.rhodoswidget.ui.news.*
import com.example.rhodoswidget.ui.weather.*
import com.example.rhodoswidget.ui.settings.*
import com.example.rhodoswidget.ui.theme.*
import com.example.rhodoswidget.widget.*

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import java.net.URI

internal data class TravelSource(
    @param:StringRes val titleRes: Int,
    @param:StringRes val descriptionRes: Int,
    val url: String
)

internal data class ExcursionIdea(
    val id: String,
    @param:DrawableRes val imageRes: Int,
    val imageCredit: String,
    val imageUrl: String,
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
        imageRes = R.drawable.excursion_lindos,
        imageCredit = "Foto: Ввласенко · CC BY-SA 3.0",
        imageUrl = "https://commons.wikimedia.org/wiki/File:Lindos_View_of_the_Acropolis_and_town_from_the_north-east._Rhodes,_Greece.jpg",
        titleRes = R.string.travel_lindos_title,
        metaRes = R.string.travel_lindos_meta,
        descriptionRes = R.string.travel_lindos_description,
        url = "https://www.hh.gr/en/destinations/lindos/",
        mapQuery = "Acropolis of Lindos, Rhodes"
    ),
    ExcursionIdea(
        id = "old_town",
        imageRes = R.drawable.excursion_old_town,
        imageCredit = "Foto: LunaLinda · CC BY-SA 4.0",
        imageUrl = "https://commons.wikimedia.org/wiki/File:Rhodes%27_old_town.jpg",
        titleRes = R.string.travel_old_town_title,
        metaRes = R.string.travel_old_town_meta,
        descriptionRes = R.string.travel_old_town_description,
        url = "https://archaeologicalmuseums.gr/en/museum/5df34af3deca5e2d79e8c140/palace-of-the-grand-master-of-knights",
        mapQuery = "Palace of the Grand Master of the Knights of Rhodes"
    ),
    ExcursionIdea(
        id = "seven_springs",
        imageRes = R.drawable.excursion_seven_springs,
        imageCredit = "Foto: dronepicr · CC BY 2.0",
        imageUrl = "https://commons.wikimedia.org/wiki/File:Second_spring_in_Epta_Piges,_Rhodes,_Greece_(51698550031).jpg",
        titleRes = R.string.travel_seven_springs_title,
        metaRes = R.string.travel_seven_springs_meta,
        descriptionRes = R.string.travel_seven_springs_description,
        url = "https://visit-rhodes.gr/",
        mapQuery = "Seven Springs, Rhodes"
    ),
    ExcursionIdea(
        id = "tsambika",
        imageRes = R.drawable.excursion_tsambika,
        imageCredit = "Foto: dronepicr · CC BY 2.0",
        imageUrl = "https://commons.wikimedia.org/wiki/File:Aerial_view_of_Tsambika_Beach,_Rhodes,_Greece_(51698551526).jpg",
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

internal data class GreekPhrase(
    val category: String,
    val greek: String,
    val phonetic: String,
    val german: String,
    val ttsText: String = greek
)

internal val greekPhrases = listOf(
    GreekPhrase("Taverne & Bestellung", "Ton logariasmó, parakaló", "Ton lo-ga-rjas-mó, pa-ra-ka-ló", "Die Rechnung bitte", "Τον λογαριασμό, παρακαλώ"),
    GreekPhrase("Taverne & Bestellung", "Stin ygía mas! / Yamas!", "Stin i-jí-a mas / Ja-mas", "Zum Wohl / Prost!", "Στην υγεία μας! Γειά μας!"),
    GreekPhrase("Taverne & Bestellung", "Éna neró, parakaló", "É-na ne-ró, pa-ra-ka-ló", "Ein Wasser bitte", "Ένα νερό, παρακαλώ"),
    GreekPhrase("Taverne & Bestellung", "Polý nóstimo!", "Po-lí nós-ti-mo", "Sehr lecker!", "Πολύ νόστιμο!"),
    GreekPhrase("Taverne & Bestellung", "Dyo biras, parakaló", "Di-o bí-ras, pa-ra-ka-ló", "Zwei Bier bitte", "Δύο μπίρες, παρακαλώ"),
    GreekPhrase("Taverne & Bestellung", "Éna oúzo, parakaló", "É-na u-zo, pa-ra-ka-ló", "Einen Ouzo bitte", "Ένα ούζο, παρακαλώ"),
    GreekPhrase("Taverne & Bestellung", "To fayitó ítan katapliktikó!", "To fa-ji-tó í-tan ka-ta-plik-ti-kó", "Das Essen war fantastisch!", "Το φαγητό ήταν καταπληκτικό!"),
    GreekPhrase("Begrüßung & Danke", "Kaliméra", "Ka-li-mé-ra", "Guten Morgen / Guten Tag", "Καλημέρα"),
    GreekPhrase("Begrüßung & Danke", "Kalispera", "Ka-lis-pé-ra", "Guten Abend", "Καλησπέρα"),
    GreekPhrase("Begrüßung & Danke", "Efcharistó polý", "Ef-cha-ris-tó po-lí", "Vielen Dank", "Ευχαριστώ πολύ"),
    GreekPhrase("Begrüßung & Danke", "Parakaló", "Pa-ra-ka-ló", "Bitte / Gern geschehen", "Παρακαλώ"),
    GreekPhrase("Begrüßung & Danke", "Yássas / Yássou", "Já-sas / Já-su", "Hallo / Tschüss (Formell / Informell)", "Γειά σας! Γειά σου!"),
    GreekPhrase("Begrüßung & Danke", "Miláte germaniká?", "Mi-lá-te ger-ma-ni-ká", "Sprechen Sie Deutsch?", "Μιλάτε γερμανικά;"),
    GreekPhrase("Unterwegs & Fragen", "Poú eínai i paralía?", "Pu í-ne i pa-ra-lí-a?", "Wo ist der Strand?", "Πού είναι η παραλία;"),
    GreekPhrase("Unterwegs & Fragen", "Poú eínai to stási?", "Pu í-ne to stá-si", "Wo ist die Bushaltestelle?", "Πού είναι η στάση;"),
    GreekPhrase("Unterwegs & Fragen", "Nai / Óchi", "Nä / Ó-chi", "Ja / Nein (Vorsicht: 'Nai' heißt Ja!)", "Ναι, Όχι"),
    GreekPhrase("Unterwegs & Fragen", "Gatáki", "Ga-tá-ki", "Kätzchen (für die süßen Tavernenkatzen)", "Γατάκι"),
    GreekPhrase("Unterwegs & Fragen", "Voítheia!", "Vo-í-thi-a", "Hilfe! (Notfall)", "Βοήθεια!")
)
