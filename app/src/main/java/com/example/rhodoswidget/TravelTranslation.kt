package com.example.rhodoswidget

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import org.json.JSONObject

data class TranslatedEventText(val title: String, val venue: String?)

object TravelTranslationRepository {
    private const val PREFS = "rhodos_event_translations"

    fun cached(context: Context, events: List<RhodesEvent>): Map<Int, TranslatedEventText> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return events.mapNotNull { event ->
            val raw = prefs.getString(event.id.toString(), null) ?: return@mapNotNull null
            runCatching {
                val json = JSONObject(raw)
                event.id to TranslatedEventText(
                    title = json.getString("title"),
                    venue = json.optString("venue").takeIf { it.isNotBlank() && it != "null" }
                )
            }.getOrNull()
        }.toMap()
    }

    fun translateMissing(context: Context, events: List<RhodesEvent>): Map<Int, TranslatedEventText> {
        val cached = cached(context, events).toMutableMap()
        val missing = events.filter { it.id !in cached && (containsGreek(it.title) || containsGreek(it.venue.orEmpty())) }
        if (missing.isEmpty()) return cached

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.GREEK)
            .setTargetLanguage(TranslateLanguage.GERMAN)
            .build()
        val translator = runCatching { Translation.getClient(options) }.getOrElse { error ->
            Log.w("RhodosTranslation", "Greek translator unavailable", error)
            return cached
        }
        return try {
            val conditions = DownloadConditions.Builder().requireWifi().build()
            Tasks.await(translator.downloadModelIfNeeded(conditions))
            missing.forEach { event ->
                val translated = TranslatedEventText(
                    title = Tasks.await(translator.translate(event.title)),
                    venue = event.venue?.takeIf(::containsGreek)?.let { Tasks.await(translator.translate(it)) }
                        ?: event.venue
                )
                cached[event.id] = translated
                save(context, event.id, translated)
            }
            cached
        } catch (error: Exception) {
            Log.w("RhodosTranslation", "Greek translation unavailable", error)
            cached
        } finally {
            translator.close()
        }
    }

    internal fun containsGreek(value: String): Boolean = value.any { it in '\u0370'..'\u03FF' || it in '\u1F00'..'\u1FFF' }

    private fun save(context: Context, id: Int, translated: TranslatedEventText) {
        val json = JSONObject().put("title", translated.title).put("venue", translated.venue)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(id.toString(), json.toString())
            .apply()
    }
}
