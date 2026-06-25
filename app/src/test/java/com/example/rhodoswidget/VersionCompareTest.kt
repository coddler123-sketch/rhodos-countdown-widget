package com.example.rhodoswidget

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionCompareTest {

    @Test fun `higher patch is newer`() =
        assertTrue(AppUpdateRepository.isNewerVersion("1.0.8", "1.0.7"))

    @Test fun `same version is not newer`() =
        assertFalse(AppUpdateRepository.isNewerVersion("1.0.7", "1.0.7"))

    @Test fun `lower patch is not newer`() =
        assertFalse(AppUpdateRepository.isNewerVersion("1.0.6", "1.0.7"))

    @Test fun `higher minor is newer`() =
        assertTrue(AppUpdateRepository.isNewerVersion("1.1.0", "1.0.9"))

    @Test fun `higher major is newer`() =
        assertTrue(AppUpdateRepository.isNewerVersion("2.0.0", "1.9.9"))

    @Test fun `shorter version handled`() =
        assertTrue(AppUpdateRepository.isNewerVersion("2.0", "1.9.9"))

    @Test fun `v-prefix stripped`() =
        assertTrue(AppUpdateRepository.isNewerVersion("1.0.8".removePrefix("v"), "1.0.7"))

    @Test fun `valid release version names are accepted`() {
        assertTrue(AppUpdateRepository.isValidVersionName("1.1.4"))
        assertTrue(AppUpdateRepository.isValidVersionName("2026-06-25"))
    }

    @Test fun `unsafe release version names are rejected`() {
        assertFalse(AppUpdateRepository.isValidVersionName("../1.1.5"))
        assertFalse(AppUpdateRepository.isValidVersionName("1.1.5 beta"))
        assertFalse(AppUpdateRepository.isValidVersionName(""))
    }

    @Test fun `trusted apk download URL is accepted`() {
        val url = "https://github.com/coddler123-sketch/rhodos-countdown-widget/releases/download/v1.1.4/Rhodos-Countdown-Widget-share.apk"

        assertTrue(AppUpdateRepository.isTrustedApkDownloadUrl(url))
    }

    @Test fun `untrusted apk download URLs are rejected`() {
        assertFalse(AppUpdateRepository.isTrustedApkDownloadUrl("http://github.com/coddler123-sketch/rhodos-countdown-widget/releases/download/v1.1.4/app.apk"))
        assertFalse(AppUpdateRepository.isTrustedApkDownloadUrl("https://example.com/app.apk"))
        assertFalse(AppUpdateRepository.isTrustedApkDownloadUrl("https://github.com/coddler123-sketch/other/releases/download/v1.1.4/app.apk"))
        assertFalse(AppUpdateRepository.isTrustedApkDownloadUrl("https://github.com/coddler123-sketch/rhodos-countdown-widget/releases/download/v1.1.4/app.zip"))
    }

    @Test fun `apk content types used by GitHub downloads are accepted`() {
        assertTrue(AppUpdateRepository.isAllowedApkContentType("application/vnd.android.package-archive"))
        assertTrue(AppUpdateRepository.isAllowedApkContentType("application/octet-stream"))
        assertTrue(AppUpdateRepository.isAllowedApkContentType("application/octet-stream; charset=utf-8"))
    }

    @Test fun `non apk content types are rejected`() {
        assertFalse(AppUpdateRepository.isAllowedApkContentType("text/html"))
        assertFalse(AppUpdateRepository.isAllowedApkContentType(null))
    }
}
