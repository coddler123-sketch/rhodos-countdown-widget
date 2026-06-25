package com.example.rhodoswidget

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivitySmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mainScreenShowsCoreCountdownContent() {
        composeRule.onNodeWithText("RHODOS").assertIsDisplayed()
        composeRule.onNodeWithText("Unser Urlaubs-Countdown").assertIsDisplayed()
        composeRule.onNodeWithText("TAGE").assertIsDisplayed()
        composeRule.onNodeWithText("STD.").assertIsDisplayed()
        composeRule.onNodeWithText("MIN.").assertIsDisplayed()
        composeRule.onNodeWithText("SEK.").assertIsDisplayed()
    }
}
