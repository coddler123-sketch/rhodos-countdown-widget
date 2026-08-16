import java.net.URI
import java.util.Properties
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

abstract class GenerateGoogleMapsLinksTask : DefaultTask() {
    @get:InputFile
    abstract val csvFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputSourceDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val inputFile = csvFile.get().asFile
        if (!inputFile.isFile) {
            throw GradleException("Google-Maps-Links.csv fehlt im Projektordner.")
        }

        val lines = inputFile.readLines(Charsets.UTF_8)
        if (lines.isEmpty() || lines.first().removePrefix("\uFEFF") != "id;titel;google_maps_url") {
            throw GradleException(
                "Google-Maps-Links.csv braucht als erste Zeile: id;titel;google_maps_url"
            )
        }

        val seenIds = mutableSetOf<String>()
        val rows = lines.drop(1).mapIndexedNotNull { index, line ->
            if (line.isBlank()) return@mapIndexedNotNull null

            val lineNumber = index + 2
            val columns = line.split(';')
            if (columns.size != 3) {
                throw GradleException(
                    "Google-Maps-Links.csv, Zeile $lineNumber: Erwartet werden genau 3 Spalten."
                )
            }

            val id = columns[0].trim()
            val title = columns[1].trim()
            val url = columns[2].trim()
            if (id.isEmpty() || title.isEmpty()) {
                throw GradleException(
                    "Google-Maps-Links.csv, Zeile $lineNumber: ID und Titel duerfen nicht leer sein."
                )
            }
            if (!seenIds.add(id)) {
                throw GradleException(
                    "Google-Maps-Links.csv, Zeile $lineNumber: Die ID '$id' ist doppelt vorhanden."
                )
            }

            if (url.isNotEmpty()) {
                val uri = try {
                    URI(url)
                } catch (_: Exception) {
                    throw GradleException(
                        "Google-Maps-Links.csv, Zeile $lineNumber: '$url' ist keine gueltige URL."
                    )
                }
                val host = uri.host?.lowercase()
                val path = uri.path.orEmpty()
                val supportedGoogleMapsLink = uri.scheme == "https" && when (host) {
                    "maps.app.goo.gl" -> path.length > 1
                    "goo.gl" -> path.startsWith("/maps")
                    "google.com", "www.google.com" -> path.startsWith("/maps")
                    "maps.google.com" -> true
                    else -> false
                }
                if (!supportedGoogleMapsLink) {
                    throw GradleException(
                        "Google-Maps-Links.csv, Zeile $lineNumber: '$url' ist kein unterstuetzter Google-Maps-Link."
                    )
                }
            }

            Triple(id, title, url)
        }

        val generatedSource = buildString {
            appendLine("package com.example.rhodoswidget")
            appendLine()
            appendLine("// Automatisch aus Google-Maps-Links.csv erzeugt. Nicht von Hand bearbeiten.")
            appendLine("internal val generatedCompassTipIds: Set<String> = setOf(")
            rows.forEach { (id, _, _) -> appendLine("    ${kotlinString(id)},") }
            appendLine(")")
            appendLine()
            appendLine("internal val generatedCompassTipMapsUrls: Map<String, String> = mapOf(")
            rows.filter { it.third.isNotEmpty() }.forEach { (id, _, url) ->
                appendLine("    ${kotlinString(id)} to ${kotlinString(url)},")
            }
            appendLine(")")
        }

        val outputFile = outputSourceDir.file(
            "com/example/rhodoswidget/CompassMapsGenerated.kt"
        ).get().asFile
        outputFile.parentFile.mkdirs()
        outputFile.writeText(generatedSource, Charsets.UTF_8)
    }

    private fun kotlinString(value: String): String = buildString {
        append('"')
        value.forEach { character ->
            when (character) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '$' -> {
                    append('\\')
                    append('$')
                }
                else -> append(character)
            }
        }
        append('"')
    }
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val newsApiUrl = providers.gradleProperty("NEWS_API_URL").orElse("")
val googleMapsLinksCsv = rootProject.layout.projectDirectory.file("Google-Maps-Links.csv")
val generatedGoogleMapsSourceDir = layout.buildDirectory.dir("generated/source/googleMaps/main")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        load(keystorePropertiesFile.inputStream())
    }
}

android {
    namespace = "com.example.rhodoswidget"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.rhodoswidget"
        minSdk = 24
        targetSdk = 36
        versionCode = 34
        versionName = "1.2.14"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "NEWS_API_URL", "\"${newsApiUrl.get()}\"")
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = rootProject.file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            optimization {
                enable = true
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

val generateGoogleMapsLinks by tasks.registering(GenerateGoogleMapsLinksTask::class) {
    group = "build setup"
    description = "Prueft Google-Maps-Links.csv und erzeugt die Links fuer die App."
    csvFile.set(googleMapsLinksCsv)
    outputSourceDir.set(generatedGoogleMapsSourceDir)
}

androidComponents {
    onVariants(selector().all()) { variant ->
        variant.sources.kotlin?.addGeneratedSourceDirectory(
            generateGoogleMapsLinks,
            GenerateGoogleMapsLinksTask::outputSourceDir
        )
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.google.mlkit.translate)
    testImplementation(libs.junit)
    testImplementation(libs.json)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
