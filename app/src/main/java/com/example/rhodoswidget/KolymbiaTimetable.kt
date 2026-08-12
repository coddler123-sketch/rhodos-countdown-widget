package com.example.rhodoswidget

internal data class KolymbiaConnection(
    val place: String,
    val outbound: LindosTimetableRoute,
    val returnTrip: LindosTimetableRoute
)

internal object KolymbiaTimetable {
    const val REVIEWED_PDF_URL =
        "https://www.ktelrodou.gr/wp-content/uploads/2026/08/KOLYMBIABEACH5.pdf"
    const val VALIDITY = "06.07.2026–10.09.2026"

    val fromKolymbia = listOf(
        route("Rhodos-Stadt", "ΡΟΔΟΣ", "4,00 €", "7:50 8:50 9:50 10:35 11:35 12:30 13:35 14:10 15:05 16:05 16:35 17:45 18:15 20:20"),
        route("Lindos", "ΛΙΝΔΟΣ", "4,30 €", "8:50 9:20 9:50 10:50 12:50 14:50"),
        route("Tsambika-Strand", "ΤΣΑΜΠΙΚΑ ΠΑΡΑΛΙΑ", "2,30 €", "9:50 12:50 15:50"),
        route("Sieben Quellen", "ΕΠΤΑ ΠΗΓΕΣ", "2,30 €", "11:30"),
        route("Kallithea-Thermen", "ΠΗΓΕΣ ΚΑΛΛΙΘΕΑΣ", "4,00 €", "8:50 9:50 10:35 14:10 16:35 17:45"),
        route("Prasonisi", "ΠΡΑΣΟΝΗΣΙ", "9,30 €", "9:50")
    )

    val toKolymbia = listOf(
        route("Tsambika-Strand", "ΤΣΑΜΠΙΚΑ ΠΑΡΑΛΙΑ", "2,30 €", "16:20 18:00"),
        route("Lindos", "ΛΙΝΔΟΣ", "4,30 €", "12:00 13:00 14:30 15:30 16:00 17:15"),
        route("Rhodos-Stadt", "ΡΟΔΟΣ", "4,00 €", "9:00 10:30 12:00 14:00 14:30 15:00 15:30 16:00 17:00 17:30 18:15 19:00 19:30 21:30 22:15 23:00"),
        route("Sieben Quellen", "ΕΠΤΑ ΠΗΓΕΣ", "2,30 €", "14:00"),
        route("Kallithea-Thermen", "ΠΗΓΕΣ ΚΑΛΛΙΘΕΑΣ", "4,00 €", "9:10 10:40 15:40 17:10 18:25 19:10"),
        route("Prasonisi", "ΠΡΑΣΟΝΗΣΙ", "9,30 €", "13:00 16:00")
    )

    val connections = fromKolymbia.mapNotNull { outbound ->
        toKolymbia.firstOrNull { it.place == outbound.place }?.let { returnTrip ->
            KolymbiaConnection(outbound.place, outbound, returnTrip)
        }
    }

    val isValidForTrip: Boolean get() = isValidOn(
        year = CountdownCalculator.DEPARTURE_YEAR,
        monthOneBased = CountdownCalculator.DEPARTURE_MONTH + 1,
        day = CountdownCalculator.DEPARTURE_DAY
    )

    fun isValidOn(year: Int, monthOneBased: Int, day: Int): Boolean {
        val date = year * 10_000 + monthOneBased * 100 + day
        return date in VALID_FROM..VALID_UNTIL
    }

    fun hasUnreviewedUpdate(officialPdfUrl: String): Boolean =
        officialPdfUrl.isNotBlank() && officialPdfUrl != REVIEWED_PDF_URL

    fun nextDeparture(departureTimes: List<String>, hour: Int, minute: Int): String? {
        val currentMinute = hour * 60 + minute
        return departureTimes.firstOrNull { time ->
            val (departureHour, departureMinute) = time.split(':').map(String::toInt)
            departureHour * 60 + departureMinute >= currentMinute
        }
    }

    fun searchConnections(query: String): List<KolymbiaConnection> {
        val term = query.trim()
        if (term.isEmpty()) return connections
        return connections.filter { connection ->
            connection.place.contains(term, ignoreCase = true) ||
                connection.outbound.greekName?.contains(term, ignoreCase = true) == true
        }
    }

    private fun route(place: String, greekName: String, price: String, times: String) =
        LindosTimetableRoute(place, greekName, price, times.split(' '))

    private const val VALID_FROM = 20260706
    private const val VALID_UNTIL = 20260910
}
