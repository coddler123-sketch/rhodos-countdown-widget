package com.example.rhodoswidget

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

private fun shortWeekday(dateIso: String): String = dateIso.toShortGermanWeekday()

data class HomeState(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val isReached: Boolean,
    val isOnVacation: Boolean,
    val phrase: String,
    val weather: WeatherSnapshot?,
    val weatherStatus: String,
    val backgroundRes: Int,
    val departureDate: String,
    val departureTime: String,
    val progress: Float,
    val sunsetLabel: String?,
    val sunriseLabel: String?,
    val factOfTheDay: String
) {
    companion object {
        private val YEAR = CountdownCalculator.DEPARTURE_YEAR
        private val MONTH = CountdownCalculator.DEPARTURE_MONTH
        private val DAY = CountdownCalculator.DEPARTURE_DAY
        private val HOUR = CountdownCalculator.DEPARTURE_HOUR
        private val MINUTE = CountdownCalculator.DEPARTURE_MINUTE

        fun load(context: Context): HomeState {
            val remaining = CountdownCalculator.calculate()

            val quotes = context.resources.getStringArray(R.array.widget_phrases)
            val phraseIndex = (quotes.size - 1 - CountdownCalculator.daysUntilDeparture())
                .coerceIn(0, quotes.size - 1)

            val weather = WeatherRepository.cached(context)?.let { w ->
                WeatherSnapshot(
                    temperatureLabel = w.temperatureLabel,
                    apparentTemperatureLabel = w.apparentTemperatureLabel,
                    humidityLabel = w.humidityLabel,
                    precipitationLabel = w.precipitationLabel,
                    windSpeedLabel = w.windSpeedLabel,
                    iconRes = WeatherRepository.iconFor(w),
                    forecastDays = w.forecast.take(7).map { day ->
                        ForecastEntry(
                            weekday = shortWeekday(day.dateIso),
                            iconRes = WeatherRepository.iconForCode(day.weatherCode),
                            minTemp = day.minTemperatureCelsius,
                            maxTemp = day.maxTemperatureCelsius
                        )
                    },
                    spokenReport = WeatherReportFormatter.spokenReport(w)
                )
            }
            val weatherStatus = weatherStatus(context, weather)
            val backgroundRes = Images.resourceOfTheDay(context)
            val df = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)
            val tf = SimpleDateFormat("HH:mm", Locale.GERMAN)
            val target = Calendar.getInstance().apply {
                set(Calendar.YEAR, YEAR); set(Calendar.MONTH, MONTH)
                set(Calendar.DAY_OF_MONTH, DAY); set(Calendar.HOUR_OF_DAY, HOUR)
                set(Calendar.MINUTE, MINUTE); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val rawWeather = WeatherRepository.cached(context)
            val sunsetLabel = rawWeather?.sunsetIso?.let { parseSolarEventLabel(it) }
            val sunriseLabel = rawWeather?.sunriseIso?.let { parseSolarEventLabel(it) }
            return HomeState(
                days = remaining.days, hours = remaining.hours,
                minutes = remaining.minutes, seconds = remaining.seconds,
                isReached = remaining.isReached,
                isOnVacation = remaining.isOnVacation,
                phrase = quotes[phraseIndex],
                weather = weather,
                weatherStatus = weatherStatus,
                backgroundRes = backgroundRes,
                departureDate = df.format(target.time),
                departureTime = tf.format(target.time),
                progress = CountdownCalculator.progressFraction(),
                sunsetLabel = sunsetLabel,
                sunriseLabel = sunriseLabel,
                factOfTheDay = rhodosFactOfTheDay()
            )
        }

        private fun parseSolarEventLabel(sunsetIso: String): String? {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US).apply {
                    timeZone = java.util.TimeZone.getTimeZone("Europe/Athens")
                }
                val sunsetTime = sdf.parse(sunsetIso) ?: return null
                val now = System.currentTimeMillis()
                val timeLabel = SimpleDateFormat("HH:mm", Locale.GERMAN).apply {
                    timeZone = java.util.TimeZone.getTimeZone("Europe/Athens")
                }.format(sunsetTime)
                val diffMillis = sunsetTime.time - now
                if (diffMillis > 0) {
                    val h = TimeUnit.MILLISECONDS.toHours(diffMillis)
                    val m = TimeUnit.MILLISECONDS.toMinutes(diffMillis) % 60
                    if (h > 0) "$timeLabel Uhr · noch ${h} Std. ${m} Min."
                    else "$timeLabel Uhr · noch ${m} Min."
                } else {
                    "$timeLabel Uhr"
                }
            } catch (e: Exception) { null }
        }

        private fun weatherStatus(context: android.content.Context, weather: WeatherSnapshot?): String {
            if (weather == null) return "Noch nicht geladen"
            val lastFetch = WeatherRepository.lastFetchMillis(context)
            if (lastFetch <= 0L) return "Gespeichert"

            val ageMillis = (System.currentTimeMillis() - lastFetch).coerceAtLeast(0L)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(ageMillis)
            if (minutes < 1) return "Gerade aktualisiert"
            if (minutes < 60) return "Vor $minutes Min. aktualisiert"

            val hours = TimeUnit.MILLISECONDS.toHours(ageMillis)
            return "Vor $hours Std. aktualisiert"
        }
    }
}

private fun rhodosFactOfTheDay(): String {
    val facts = listOf(
        // Geografie & Natur
        "Rhodos ist mit 1.401 km² die viertgrößte Insel Griechenlands – nach Kreta, Euböa und Lesbos.",
        "Der höchste Berg der Insel ist der Attavyros mit 1.215 m – bei klarem Wetter sieht man von dort die türkische Küste.",
        "Rhodos liegt nur 18 km von der türkischen Küste entfernt – näher als Mallorca an Spanien.",
        "Die Insel hat rund 300 Sonnentage im Jahr – damit gehört sie zu den sonnigsten Orten Europas.",
        "Rhodos gehört zur Dodekanes-Gruppe, dem Zwölf-Inseln-Archipel in der südlichen Ägäis.",
        "Im September hat das Meer rund um Rhodos noch angenehme 25–27 °C Wassertemperatur.",
        "Auf der Insel wachsen über 50 endemische Pflanzenarten, die nirgendwo sonst auf der Welt vorkommen.",
        "Das Schmetterlingstal (Petaloudes) ist im Sommer Heimat Tausender Jerseyspinner-Falter.",
        "Auf Rhodos sind Damhirsche heimisch – die berühmten Bronzehirsche im Hafen von Mandraki erinnern daran.",
        "Die Insel ist ein wichtiges Brutgebiet der Meeresschildkröte Caretta caretta.",
        "Die Küstenlinie von Rhodos ist rund 220 km lang – mit über 40 verschiedenen Stränden.",
        "Prasonissi im Süden ist ein weltbekannter Windsurfspot: dort treffen Mittelmeer und Ägäis aufeinander.",
        "Der rhodische Meltemi-Wind weht im Sommer aus Nordwest und sorgt für angenehme Kühle.",
        "Im September ist Rhodos besonders schön – weniger Touristen, noch voll Sonne, erstes Herbstlicht.",
        "Rhodos produziert eigenen Honig mit einzigartigem Aroma – dank des lokalen wilden Thymians.",
        // Kolymbia
        "Kolymbia liegt an der Ostküste, etwa 26 km südlich der Inselhauptstadt.",
        "Die Eukalyptusbäume entlang der Hauptstraße in Kolymbia wurden in den 1950er Jahren zur Trockenlegung von Sümpfen gepflanzt.",
        "Kolymbia ist bekannt für seinen ruhigen, breiten Sandstrand – ideal zum Entspannen.",
        "Der Stegna-Strand nördlich von Kolymbia ist besonders bei Einheimischen beliebt.",
        "Agathi-Strand, wenige Kilometer südlich, gilt als einer der schönsten Strände der Ostküste.",
        "Das Tsambika-Kloster thront auf einem 300 m hohen Felsen bei Kolymbia – der Ausblick ist atemberaubend.",
        "Frauen, die im Tsambika-Kloster beten, nennen ihr Kind traditionell Tsambikos oder Tsambika.",
        "In der Nähe von Kolymbia liegt das Dorf Archangelos – bekannt für handgefertigte Lederstiefel.",
        "An der Ostküste bei Kolymbia weht im Sommer oft eine kühle Seebrise – erfrischender als an der Westküste.",
        // Antike Geschichte
        "Die Insel war in der Antike berühmt für ihre Rosen – daher der Name (griech. rhodon = Rose).",
        "Die Städte Kamiros, Ialyssos und Lindos gründeten 408 v. Chr. gemeinsam die neue Hauptstadt Rhodos.",
        "Der neue Stadtplan von Rhodos folgte dem Rastersystem des Hippodamos von Milet – ein Vorläufer moderner Stadtplanung.",
        "Rhodos war im 3. Jahrhundert v. Chr. eines der bedeutendsten Handelszentren des gesamten Mittelmeers.",
        "Die Rhodier entwickelten das älteste bekannte Seehandelsrecht der Welt – das rhodische Seerecht.",
        "Das rhodische Seerecht beeinflusst bis heute das internationale Schifffahrtsrecht.",
        "Cicero und Julius Cäsar studierten auf Rhodos Rhetorik – die rhodische Schule war berühmt.",
        "Julius Cäsar wurde auf dem Weg nach Rhodos von Piraten entführt und auf einer kleinen Insel festgehalten.",
        "Die rhodische Bildhauerschule schuf Meisterwerke der Antike – darunter vermutlich die Laokoon-Gruppe.",
        "Die Laokoon-Gruppe, heute in den Vatikanischen Museen, wurde wahrscheinlich von Rhodiern erschaffen.",
        "Diagoras von Rhodos war einer der berühmtesten Sportler der Antike und wurde von Pindar in einer Ode gefeiert.",
        "Auf dem Gipfel des Attavyros stand einst ein Zeus-Tempel – von dem noch Ruinen sichtbar sind.",
        "Das antike Kamiros ist eine der besterhaltenen antiken Städte Griechenlands – ohne spätere Überbauung.",
        "In der Antike schickten Städte aus ganz Griechenland Weihgeschenke zum Tempel auf Rhodos.",
        // Koloss von Rhodos
        "Der Koloss von Rhodos war eine riesige Bronzestatue des Sonnengottes Helios – über 30 m hoch.",
        "Der Koloss wurde um 280 v. Chr. fertiggestellt und galt als eines der Sieben Weltwunder der Antike.",
        "Ein Erdbeben 226 v. Chr. warf den Koloss um – er lag über 800 Jahre als Trümmerhaufen.",
        "Der genaue Standort des Kolosses ist bis heute unbekannt – viele vermuten ihn am Eingang des Hafens.",
        "Die Araber transportierten die Trümmer des Kolosses im 7. Jahrhundert angeblich auf 900 Kamelen ab.",
        "Moderne Pläne, eine Nachbildung des Kolosses im Hafen zu errichten, werden seit Jahren diskutiert.",
        // Mittelalter & Ritter
        "Der Johanniterorden eroberte Rhodos 1309 und machte die Insel zu seinem Hauptsitz.",
        "Die Ritter bauten die Befestigungsanlagen von Rhodos zu einer der stärksten Festungen Europas aus.",
        "Die Stadtmauer der Altstadt ist bis zu 4 km lang und an manchen Stellen 12 m dick.",
        "Die Straße der Ritter (Ippoton) gilt als besterhaltene mittelalterliche Straße Europas.",
        "Der Palast des Großmeisters wurde nach einem Pulvermagazin-Explosion 1856 schwer beschädigt und später restauriert.",
        "Die Ritter teilten die Altstadt in sogenannte Zungen auf – Landsmannschaften aus verschiedenen europäischen Ländern.",
        "Die Osmanen unter Süleyman dem Prächtigen eroberten Rhodos 1522 nach sechsmonatiger Belagerung.",
        "Die Johanniterritter zogen nach der Niederlage nach Malta weiter – und wurden dort zu den Maltesern.",
        "Die osmanische Süleymaniye-Moschee in der Altstadt wurde direkt nach der Eroberung 1522 erbaut.",
        "Das D'Amboise-Tor ist das eindrucksvollste der sieben Stadttore der mittelalterlichen Altstadt.",
        // UNESCO & Kulturerbe
        "Die Altstadt von Rhodos ist seit 1988 UNESCO-Weltkulturerbe – wegen ihrer einzigartigen mittelalterlichen Substanz.",
        "In der Altstadt leben noch heute etwa 6.000 Menschen – sie ist keine reine Touristenkulisse.",
        "Die Altstadt vereint griechische, osmanische, jüdische und westeuropäische Architektur auf engstem Raum.",
        "Das jüdische Viertel (La Juderia) in der Altstadt ist eines der ältesten jüdischen Quartiere der Welt.",
        "Die jüdische Gemeinde auf Rhodos wurde 1944 deportiert – von ursprünglich 1.700 Menschen überlebten nur wenige.",
        "Das Jüdische Museum in der Altstadt ist eines der wenigen Museen seiner Art in Griechenland.",
        // Moderne Geschichte
        "Die Dodekanes-Inseln waren von 1912 bis 1943 unter italienischer Verwaltung.",
        "Die Italiener hinterließen auf Rhodos bemerkenswerte Architektur aus den 1930er Jahren – teils im Faschismus-Stil.",
        "Das Aquarium im Norden der Insel wurde 1935 von den Italienern erbaut und ist heute noch in Betrieb.",
        "Die Dodekanes wurden erst 1947 offiziell Teil Griechenlands.",
        "Im Zweiten Weltkrieg war Rhodos ab 1943 von deutschen Truppen besetzt.",
        "Anthony Quinn kaufte nach dem Dreh von 'Die Kanonen von Navarone' (1961) Land auf Rhodos.",
        "Die Bucht, in der Anthony Quinn lebte und schwamm, trägt heute seinen Namen: Anthony Quinn Bay.",
        // Kulinarik & Kultur
        "\"Pitaroudia\" sind frittierte Kichererbsenpuffer – eine rhodische Spezialität, die man unbedingt probieren sollte.",
        "\"Melekouni\" ist ein rhodisches Süßgebäck aus Honig und Sesam – traditionell bei Hochzeiten gereicht.",
        "Der rhodische Muskatwein (Muscat of Rhodes) hat ein EU-Herkunftsschutzsiegel (PDO).",
        "Rhodos ist seit der Antike ein Weinanbaugebiet – die Reben wachsen auf vulkanischem Boden.",
        "Rhodische Keramik ist bunt und für ihre Granatapfelmotive bekannt – ein Symbol für Glück.",
        "Der Granatapfel gilt auf Rhodos als Glücksbringer und hängt in vielen Häusern über der Tür.",
        "In Lindos dürfen keine Autos fahren – die Altstadt ist nur zu Fuß oder per Esel erreichbar.",
        "Die Akropolis von Lindos thront auf einem 116 m hohen Felsen direkt über dem strahlend blauen Meer.",
        "Lindos war in der Antike eine bedeutende Handelsstadt mit eigenem Hafen und weitreichenden Verbindungen.",
        // Natur & Tiere
        "Auf Rhodos wurden über 150 Vogelarten beobachtet – die Insel liegt auf wichtigen Zugvogelrouten.",
        "Im Frühjahr blüht Rhodos in einem Meer aus Wildblumen – Mohn, Asphodel und Orchideen.",
        "Die Insel hat mehrere natürliche Quellen, deren Wasser seit der Antike als heilkräftig gilt.",
        "Das Wasser rund um Rhodos ist so klar, dass man bei ruhiger See bis auf 30–40 m Tiefe sehen kann.",
        "Vor der Küste von Rhodos liegen mehrere antike Schiffswracks – ein Paradies für Taucher.",
        "Im September sind die Feigenbäume auf Rhodos voll mit reifen Früchten – ein unvergleichlicher Genuss.",
        // Zahlen & Fakten
        "Rhodos hat rund 115.000 Einwohner – die meisten davon in der Inselhauptstadt.",
        "Über 2 Millionen Touristen besuchen Rhodos jährlich – fast 20 Mal mehr als Einwohner.",
        "Der Flughafen \"Diagoras\" liegt 14 km südwestlich der Stadt und ist einer der verkehrsreichsten Griechenlands.",
        "Rhodos hat eine eigene Tageszeitung: \"Rhodiaki\" – sie erscheint seit Jahrzehnten.",
        "Auf der Insel gibt es über 300 Kirchen und Kapellen – viele davon winzig und versteckt.",
        "Die Insel hat 11 Gemeinden und über 40 bewohnte Ortschaften.",
        // Besonderes & Kurioses
        "Der Platane im Hafen von Kos soll unter Hippokrates gestanden haben – Rhodos und Kos teilten viel medizinisches Wissen.",
        "Rhodos galt in der Antike als so reich, dass man sagte: \"Selbst die Götter möchten auf Rhodos leben.\"",
        "Die rhodische Marine war in der Antike für ihre Schnelligkeit berühmt – ihre Galeeren galten als unbesiegbar.",
        "Im Mittelalter war Rhodos ein wichtiger Zwischenstopp für Pilger auf dem Weg ins Heilige Land.",
        "Die Windmühlen im Hafen von Mandraki wurden im 15. Jahrhundert gebaut – drei stehen noch heute.",
        "Rhodos ist die Geburtsstadt des griechischen Schriftstellers und Nobelpreisträgers Odysseas Elytis – nein, er stammte aus Kreta, aber Rhodos inspirierte viele seiner Verse.",
        "Das Meer um Rhodos wechselt je nach Tageszeit die Farbe – von tiefem Blau bis smaragdgrün.",
        "Auf Rhodos gibt es eine besondere Lichtstimmung im Spätsommer: golden, warm, fast unwirklich schön.",
        "Im September kühlen die Nächte auf Rhodos leicht ab – perfekt zum Schlafen mit offenen Fenstern.",
        "Ein Urlaub auf Rhodos fühlt sich länger an als er ist – weil jeder Tag vollgepackt mit Eindrücken ist."
    )
    val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
    return facts[dayOfYear % facts.size]
}
