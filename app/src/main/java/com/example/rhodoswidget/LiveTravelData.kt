package com.example.rhodoswidget

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class TransitDocument(
    val id: String,
    val title: String,
    val operator: String,
    val pdfUrl: String,
    val sourceUrl: String,
    val fetchedAtMillis: Long
)

data class RhodesEvent(
    val id: Int,
    val title: String,
    val startDateTime: String,
    val endDateTime: String,
    val venue: String?,
    val url: String
)

object LiveTravelParser {
    fun ktelDocument(
        json: String,
        id: String,
        title: String,
        sourceUrl: String,
        fetchedAtMillis: Long
    ): TransitDocument? {
        val html = JSONObject(json).getJSONObject("content").getString("rendered")
        val pdf = PDF_URL.findAll(decodeEntities(html))
            .map { it.value.trimEnd('=') }
            .firstOrNull { URI(it).host == "www.ktelrodou.gr" && "/wp-content/uploads/" in it }
            ?: return null
        return TransitDocument(id, title, "KTEL", pdf, sourceUrl, fetchedAtMillis)
    }

    fun rodaDocuments(json: String, fetchedAtMillis: Long): List<TransitDocument> {
        val html = decodeEntities(JSONObject(json).getJSONObject("content").getString("rendered"))
        val candidates = ENCODED_PDF.findAll(html).mapNotNull { match ->
            val decoded = URLDecoder.decode(match.value, StandardCharsets.UTF_8.name())
            val url = if (decoded.startsWith("/")) "https://www.rhodes.gr$decoded" else decoded
            rodaCategory(url)?.let { category -> category to url }
        }
        return candidates.associate { it.first.id to it }.values.map { (category, url) ->
            TransitDocument(
                id = category.id,
                title = category.title,
                operator = "RODA",
                pdfUrl = url,
                sourceUrl = RODA_SOURCE,
                fetchedAtMillis = fetchedAtMillis
            )
        }.sortedBy { it.id }
    }

    fun events(json: String, nowMillis: Long): List<RhodesEvent> {
        val array = JSONObject(json).optJSONArray("events") ?: return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                val end = item.optString("end_date")
                if (parseDate(end)?.time?.let { it < nowMillis } != false) continue
                add(
                    RhodesEvent(
                        id = item.getInt("id"),
                        title = decodeEntities(item.getString("title")),
                        startDateTime = item.getString("start_date"),
                        endDateTime = end,
                        venue = item.optJSONObject("venue")?.optString("venue")?.takeIf(String::isNotBlank)
                            ?: item.optString("venue").takeIf { it.isNotBlank() && it != "null" },
                        url = item.getString("url")
                    )
                )
            }
        }.sortedBy { it.startDateTime }
    }

    fun isTrustedPdf(url: String): Boolean {
        val uri = runCatching { URI(url) }.getOrNull() ?: return false
        return uri.scheme == "https" && uri.host in setOf("www.ktelrodou.gr", "www.rhodes.gr") &&
            uri.path.lowercase(Locale.ROOT).endsWith(".pdf")
    }

    private fun rodaCategory(url: String): RodaCategory? {
        val name = URLDecoder.decode(url.substringAfterLast('/'), StandardCharsets.UTF_8.name())
            .uppercase(Locale.ROOT)
        return when {
            "DROMOLOGIA-RHODES-FALIRAKI" in name -> RodaCategory("roda_faliraki", "Rhodos ↔ Faliraki")
            "ΔΥΤΙΚΗΣ" in name && "ΔΕΥΤΕΡ" in name -> RodaCategory("roda_weekday", "Westküste · Montag–Freitag")
            "ΔΥΤΙΚΗΣ" in name && "ΣΑΒΒΑΤ" in name -> RodaCategory("roda_saturday", "Westküste · Samstag")
            "ΔΥΤΙΚΗΣ" in name && "ΚΥΡΙΑΚ" in name -> RodaCategory("roda_sunday", "Westküste · Sonntag/Feiertag")
            else -> null
        }
    }

    private fun decodeEntities(value: String): String = value
        .replace("&amp;", "&")
        .replace("&#038;", "&")
        .replace("&#8211;", "–")
        .replace("&#8216;", "‘")
        .replace("&#8217;", "’")
        .replace("&#8220;", "“")
        .replace("&#8221;", "”")

    private fun parseDate(value: String) = runCatching {
        SimpleDateFormat(DATE_PATTERN, Locale.US).parse(value)
    }.getOrNull()

    private data class RodaCategory(val id: String, val title: String)

    private val PDF_URL = Regex("https://www\\.ktelrodou\\.gr/wp-content/uploads/[^\\s\\\"'<>]+?\\.pdf=?", RegexOption.IGNORE_CASE)
    private val ENCODED_PDF = Regex("(?:https%3A%2F%2Fwww\\.rhodes\\.gr)?%2Fwp-content%2Fuploads%2F[^|\\s\\\"'<>]+?\\.pdf", RegexOption.IGNORE_CASE)
    private const val DATE_PATTERN = "yyyy-MM-dd HH:mm:ss"
    private const val RODA_SOURCE = "https://www.rhodes.gr/sygkinonies-metafores-stin-poli-ke-sto-nisi/"
}

object LiveTravelRepository {
    private const val PREFS = "rhodos_live_travel"
    private const val KEY_TRANSIT = "transit"
    private const val KEY_EVENTS = "events"
    private const val RODA_API =
        "https://www.rhodes.gr/wp-json/wp/v2/pages/2827?_fields=content,modified"

    internal val ktelSources = listOf(
        KtelSource("ktel_kolymbia", "Busse ab Kolymbia Beach", "kolymbia-beach2-qr"),
        KtelSource("ktel_lindos", "Rhodos ↔ Lindos", "lindos-qr"),
        KtelSource("ktel_tsambika", "Rhodos ↔ Tsambika", "tsambika-qr"),
        KtelSource("ktel_seven_springs", "Rhodos ↔ Sieben Quellen", "seven-springs-qr")
    )

    internal fun placeholderTransitDocument(id: String): TransitDocument? =
        ktelSources.firstOrNull { it.id == id }?.let { source ->
            TransitDocument(
                id = source.id,
                title = source.title,
                operator = "KTEL",
                pdfUrl = "",
                sourceUrl = source.pageUrl,
                fetchedAtMillis = 0L
            )
        }

    fun fetchTransit(nowMillis: Long = System.currentTimeMillis()): List<TransitDocument>? {
        val ktel = ktelSources.mapNotNull { source ->
            val api = "https://www.ktelrodou.gr/wp-json/wp/v2/pages?slug=${source.slug}&_fields=content"
            httpText(api)?.let { body ->
                val page = JSONArray(body).optJSONObject(0)?.toString() ?: return@let null
                LiveTravelParser.ktelDocument(page, source.id, source.title, source.pageUrl, nowMillis)
            }
        }
        val roda = httpText(RODA_API)?.let { LiveTravelParser.rodaDocuments(it, nowMillis) }.orEmpty()
        return (ktel + roda).takeIf { it.isNotEmpty() }
    }

    fun fetchEvents(nowMillis: Long = System.currentTimeMillis()): List<RhodesEvent>? {
        return httpText(eventEndpoint(nowMillis))?.let { LiveTravelParser.events(it, nowMillis) }
    }

    internal fun eventEndpoint(nowMillis: Long): String {
        val start = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            add(Calendar.MONTH, -1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val end = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            add(Calendar.DAY_OF_YEAR, 90)
        }
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val encodedStart = URLEncoder.encode(formatter.format(start.time), StandardCharsets.UTF_8.name())
            .replace("+", "%20")
        val encodedEnd = URLEncoder.encode(formatter.format(end.time), StandardCharsets.UTF_8.name())
            .replace("+", "%20")
        return "https://www.rhodes.gr/wp-json/tribe/events/v1/events" +
            "?start_date=$encodedStart&end_date=$encodedEnd&per_page=20"
    }

    fun saveTransit(context: Context, documents: List<TransitDocument>) {
        val array = JSONArray()
        documents.forEach { document ->
            array.put(JSONObject().put("id", document.id).put("title", document.title)
                .put("operator", document.operator).put("pdf", document.pdfUrl)
                .put("source", document.sourceUrl).put("fetched", document.fetchedAtMillis))
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_TRANSIT, array.toString()).apply()
    }

    fun cachedTransit(context: Context): List<TransitDocument> = runCatching {
        val array = JSONArray(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_TRANSIT, "[]"))
        List(array.length()) { index ->
            array.getJSONObject(index).run {
                TransitDocument(getString("id"), getString("title"), getString("operator"),
                    getString("pdf"), getString("source"), getLong("fetched"))
            }
        }.filter { LiveTravelParser.isTrustedPdf(it.pdfUrl) }
    }.getOrDefault(emptyList())

    fun saveEvents(context: Context, events: List<RhodesEvent>) {
        val array = JSONArray()
        events.forEach { event ->
            array.put(JSONObject().put("id", event.id).put("title", event.title)
                .put("start", event.startDateTime).put("end", event.endDateTime)
                .put("venue", event.venue).put("url", event.url))
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_EVENTS, array.toString()).apply()
    }

    fun cachedEvents(context: Context, nowMillis: Long = System.currentTimeMillis()): List<RhodesEvent> = runCatching {
        val array = JSONArray(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_EVENTS, "[]"))
        val root = JSONObject().put("events", array)
        LiveTravelParser.events(root.toString(), nowMillis)
    }.getOrDefault(emptyList())

    fun downloadPdf(context: Context, document: TransitDocument): File? {
        if (!LiveTravelParser.isTrustedPdf(document.pdfUrl)) return null
        val directory = File(context.cacheDir, "transit_schedules").apply { mkdirs() }
        val target = File(directory, "${document.id}.pdf")
        val temporary = File(directory, "${document.id}.tmp")
        val connection = (URL(document.pdfUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 12_000
            readTimeout = 20_000
            requestMethod = "GET"
        }
        return try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return target.takeIf(File::exists)
            connection.inputStream.use { input -> temporary.outputStream().use { input.copyTo(it) } }
            if (!temporary.isPdf()) return target.takeIf(File::exists)
            if (target.exists()) target.delete()
            if (!temporary.renameTo(target)) return null
            target
        } catch (error: Exception) {
            Log.w("RhodosTransit", "Timetable download failed", error)
            target.takeIf(File::exists)
        } finally {
            temporary.delete()
            connection.disconnect()
        }
    }

    private fun httpText(url: String): String? {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 12_000
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
        }
        return try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return null
            connection.inputStream.bufferedReader().use { it.readText() }
        } catch (error: Exception) {
            Log.w("RhodosTravel", "Live travel fetch failed", error)
            null
        } finally {
            connection.disconnect()
        }
    }

    private fun File.isPdf(): Boolean = runCatching {
        inputStream().use { input ->
            val header = ByteArray(4)
            input.read(header) == 4 && header.contentEquals("%PDF".toByteArray())
        }
    }.getOrDefault(false)

    internal data class KtelSource(val id: String, val title: String, val slug: String) {
        val pageUrl: String get() = "https://www.ktelrodou.gr/$slug/"
    }
}
