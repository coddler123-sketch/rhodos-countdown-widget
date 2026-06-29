package com.example.rhodoswidget

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
        composeRule.onNodeWithText("Unser Urlaubs-Countdown").assertIsDisplayed()
        composeRule.onNodeWithText("TAGE").assertIsDisplayed()
        composeRule.onNodeWithText("STD.").assertIsDisplayed()
        composeRule.onNodeWithText("MIN.").assertIsDisplayed()
        composeRule.onNodeWithText("SEK.").assertIsDisplayed()
    }

    @Test
    fun settingsSheetOpensGallery() {
        composeRule.onNodeWithTag("settings-button").performClick()
        composeRule.onNodeWithText("Optionen").assertIsDisplayed()

        composeRule.onNodeWithTag("settings-open-gallery").performClick()

        composeRule.onNodeWithText("Hintergrundbild Galerie").assertIsDisplayed()
        composeRule.onNodeWithTag("gallery-auto-image").assertIsDisplayed()
    }

    @Test
    fun galleryImageSelectionPersistsAfterActivityRecreation() {
        composeRule.onNodeWithTag("settings-button").performClick()
        composeRule.onNodeWithTag("settings-open-gallery").performClick()

        composeRule.onNodeWithTag("gallery-image-prasonisi_rhodes_023").performClick()

        composeRule.onNodeWithText("RHODOS").assertIsDisplayed()
        composeRule.onNodeWithText("Unser Urlaubs-Countdown").assertIsDisplayed()

        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("prasonisi_rhodes_023", Images.getPinnedImage(context))

        composeRule.activityRule.scenario.recreate()
        composeRule.onNodeWithTag("settings-button").performClick()
        composeRule.onNodeWithTag("settings-open-gallery").performClick()
        composeRule.onNodeWithTag("gallery-image-prasonisi_rhodes_023").assertIsSelected()
    }

    private fun clearPinnedImage() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("rhodos_settings", Context.MODE_PRIVATE)
            .edit()
            .remove("pinned_image_name")
            .commit()
    }
}
