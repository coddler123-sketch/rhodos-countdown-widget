package com.example.rhodoswidget

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class AppUpdate(
    val versionName: String,
    val apkUrl: String,
    val releaseUrl: String
)

object AppUpdateRepository {

    private const val GITHUB_OWNER = "coddler123-sketch"
    private const val GITHUB_REPO = "rhodos-countdown-widget"
    private const val APK_NAME_HINT = ".apk"

    private val latestReleaseUrl =
        "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"

    fun checkLatest(): AppUpdate? {
        val connection = (URL(latestReleaseUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 10_000
            requestMethod = "GET"
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "RhodosCountdownWidget/${BuildConfig.VERSION_NAME}")
        }

        return try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return null
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val release = JSONObject(body)
            val versionName = release.optString("tag_name")
                .removePrefix("v")
                .ifBlank { release.optString("name").removePrefix("v") }
            if (!isNewerVersion(versionName, BuildConfig.VERSION_NAME)) return null

            val assets = release.getJSONArray("assets")
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                val name = asset.optString("name")
                if (name.endsWith(APK_NAME_HINT, ignoreCase = true)) {
                    return AppUpdate(
                        versionName = versionName,
                        apkUrl = asset.getString("browser_download_url"),
                        releaseUrl = release.optString("html_url")
                    )
                }
            }
            null
        } catch (_: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }

    fun download(context: Context, update: AppUpdate): File? {
        val connection = (URL(update.apkUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 30_000
            requestMethod = "GET"
            setRequestProperty("User-Agent", "RhodosCountdownWidget/${BuildConfig.VERSION_NAME}")
        }

        return try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return null
            val dir = File(context.cacheDir, "updates").apply { mkdirs() }
            val apk = File(dir, "Rhodos-Countdown-Widget-${update.versionName}.apk")
            connection.inputStream.use { input ->
                apk.outputStream().use { output -> input.copyTo(output) }
            }
            apk
        } catch (_: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }

    fun installIntent(context: Context, apk: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apk
        )
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    private fun isNewerVersion(candidate: String, current: String): Boolean {
        val candidateParts = candidate.toVersionParts()
        val currentParts = current.toVersionParts()
        val max = maxOf(candidateParts.size, currentParts.size)
        for (i in 0 until max) {
            val next = candidateParts.getOrElse(i) { 0 }
            val now = currentParts.getOrElse(i) { 0 }
            if (next != now) return next > now
        }
        return false
    }

    private fun String.toVersionParts(): List<Int> =
        split('.', '-', '_').mapNotNull { it.toIntOrNull() }
}
