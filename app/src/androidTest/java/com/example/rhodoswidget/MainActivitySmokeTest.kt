package com.example.rhodoswidget

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivitySmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun clearPinnedImageBeforeTest() {
        clearPinnedImage()
    }

    @After
    fun clearPinnedImageAfterTest() {
        clearPinnedImage()
    }

    @Test
    fun mainScreenShowsCoreCountdownContent() {
        composeRule.onNodeWithText("RHODOS").assertIsDisplayed()
        composeRule.onNodeWithText("Bis zu unserem Rhodos-Urlaub").assertIsDisplayed()
        composeRule.onNodeWithText("TAGE").assertIsDisplayed()
        composeRule.onNodeWithText("STD.").assertIsDisplayed()
        composeRule.onNodeWithText("MIN.").assertIsDisplayed()
        composeRule.onNodeWithText("SEK.").assertIsDisplayed()
        composeRule.onNodeWithTag("home-day-plan").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("TIPP FÜR EUREN URLAUB").assertIsDisplayed()
    }

    @Test
    fun settingsSheetOpensGallery() {
        composeRule.onNodeWithTag("settings-button").performClick()
        composeRule.onNodeWithText("Optionen").assertIsDisplayed()

        composeRule.onNodeWithTag("settings-open-gallery").performClick()

        composeRule.onNodeWithText("Hintergrund gestalten").assertIsDisplayed()
        composeRule.onNodeWithTag("gallery-auto-image").assertIsDisplayed()
    }

    @Test
    fun galleryImageSelectionPersistsAfterActivityRecreation() {
        composeRule.onNodeWithTag("settings-button").performClick()
        composeRule.onNodeWithTag("settings-open-gallery").performClick()

        composeRule.onNodeWithTag("gallery-image-prasonisi_rhodes_023").performClick()
        composeRule.onNodeWithTag("gallery-apply").performClick()

        composeRule.onNodeWithText("RHODOS").assertIsDisplayed()
        composeRule.onNodeWithText("Bis zu unserem Rhodos-Urlaub").assertIsDisplayed()

        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("prasonisi_rhodes_023", Images.getPinnedImage(context))

        composeRule.activityRule.scenario.recreate()
        composeRule.onNodeWithTag("settings-button").performClick()
        composeRule.onNodeWithTag("settings-open-gallery").performClick()
        composeRule.onNodeWithTag("gallery-image-prasonisi_rhodes_023").assertIsSelected()
    }

    @Test
    fun compassOpensFromMainNavigationAndReturns() {
        composeRule.onNodeWithTag("main-nav-compass").performClick()
        composeRule.onNodeWithText("Rhodos Tipps").assertIsDisplayed()
        composeRule.onNodeWithTag("compass-screen").assertIsDisplayed()
        composeRule.onNodeWithText("13 AUSGEWÄHLTE TIPPS").assertIsDisplayed()
        composeRule.onNodeWithTag("compass-category-Essen").assertIsDisplayed()
        composeRule.onNodeWithTag("compass-screen").performScrollToIndex(3)
        composeRule.onNodeWithTag("community-link").assertIsDisplayed()

        composeRule.onNodeWithTag("compass-category-Unterkünfte").performClick()
        composeRule.onNodeWithTag("compass-category-screen").assertIsDisplayed()
        composeRule.onNodeWithText("5 persönliche Tipps").assertIsDisplayed()

        pressBack()
        composeRule.onNodeWithTag("compass-category-Unterkünfte").assertIsDisplayed()

        pressBack()

        composeRule.onNodeWithText("RHODOS").assertIsDisplayed()
    }

    @Test
    fun bottomNavigationSwitchesBetweenMainDestinations() {
        composeRule.onNodeWithTag("main-nav-home").assertIsSelected()

        composeRule.onNodeWithTag("main-nav-travel").performClick().assertIsSelected()
        composeRule.onNodeWithTag("main-nav-news").performClick().assertIsSelected()
        composeRule.onNodeWithTag("main-nav-compass").performClick().assertIsSelected()
        composeRule.onNodeWithTag("compass-screen").assertIsDisplayed()

        composeRule.onNodeWithTag("main-nav-home").performClick().assertIsSelected()
        composeRule.onNodeWithText("RHODOS").assertIsDisplayed()
    }

    @Test
    fun tappingActiveTabReturnsToTop() {
        composeRule.onNodeWithTag("main-nav-compass").performClick()
        composeRule.onNodeWithTag("compass-screen").performScrollToIndex(3)

        composeRule.onNodeWithTag("main-nav-compass").performClick()

        composeRule.onNodeWithText("Rhodos Tipps").assertIsDisplayed()
    }

    @Test
    fun travelOverviewOpensMobilityAreaAndReturns() {
        composeRule.onNodeWithTag("main-nav-travel").performClick()
        composeRule.onNodeWithTag("travel-area-today").assertIsDisplayed()
        composeRule.onNodeWithTag("travel-area-mobility").assertIsDisplayed()
        composeRule.onNodeWithTag("travel-area-explore").assertIsDisplayed()
        composeRule.onNodeWithTag("travel-area-help").assertIsDisplayed()

        composeRule.onNodeWithTag("travel-area-mobility").performClick()
        composeRule.onNodeWithTag("travel-area-screen").assertIsDisplayed()
        composeRule.onNodeWithText("Busse & Mobilität").assertIsDisplayed()
        composeRule.onNodeWithTag("travel-area-screen").performScrollToIndex(3)
        composeRule.onNodeWithTag("travel-more-mobility").performScrollTo().performClick()
        composeRule.onNodeWithTag("travel-more-mobility").assertTextContains("−")

        pressBack()
        composeRule.onNodeWithTag("travel-area-mobility").assertIsDisplayed()
    }

    @Test
    fun busShortcutOpensKolymbiaTimetableDirectly() {
        composeRule.onNodeWithTag("kolymbia-bus-link").performClick()

        composeRule.onNodeWithTag("kolymbia-timetable").assertIsDisplayed()
        composeRule.onNodeWithText("Busse ab Kolymbia Beach").assertIsDisplayed()

        pressBack()
        composeRule.onNodeWithText("RHODOS").assertIsDisplayed()
    }

    @Test
    fun checklistShortcutOpensChecklistDirectly() {
        composeRule.onNodeWithTag("checklist-link").performClick()

        composeRule.onNodeWithTag("travel-checklist-screen").assertIsDisplayed()
        composeRule.onNodeWithText("Checkliste").assertIsDisplayed()

        pressBack()
        composeRule.onNodeWithText("RHODOS").assertIsDisplayed()
    }

    @Test
    fun backFromArticleReturnsToNewsOverview() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("rhodos_news", Context.MODE_PRIVATE).edit()
            .putString(
                "latest_payload",
                """{"generatedAt":"2026-07-06T08:00:00Z","items":[{"id":"test-article","originalTitle":"Δοκιμή","germanTitle":"Testmeldung aus Rhodos","germanSummary":"Eine kurze Zusammenfassung.","originalUrl":"https://example.com/article","publishedAt":"2026-07-06T08:00:00Z","source":"Testquelle","category":"RHODOS"}]}"""
            )
            .commit()
        composeRule.activityRule.scenario.recreate()

        composeRule.onNodeWithTag("main-nav-news").performClick()
        composeRule.onNodeWithText("Aktuelles von Rhodos").assertIsDisplayed()
        composeRule.onNodeWithText("NEUESTE MELDUNG").assertIsDisplayed()
        composeRule.onNodeWithTag("news-more-filters").performClick()
        composeRule.onNodeWithText("Wetter/Unwetter").assertIsDisplayed()
        composeRule.onAllNodesWithTag("news-open-detail")[0].performClick()
        composeRule.onNodeWithText("Auf Deutsch").assertIsDisplayed()

        pressBack()

        composeRule.onNodeWithText("Aktuelles von Rhodos").assertIsDisplayed()
    }

    private fun pressBack() {
        composeRule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }
        composeRule.waitForIdle()
    }

    private fun clearPinnedImage() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("rhodos_settings", Context.MODE_PRIVATE)
            .edit()
            .remove("pinned_image_name")
            .commit()
    }
}
