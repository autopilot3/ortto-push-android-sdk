package com.ortto.demo

import android.Manifest
import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assume.assumeTrue

class DemoAppTest {
    @get:Rule
    val compose = createEmptyComposeRule()

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun launchFreshDemo() {
        InstrumentationRegistry.getInstrumentation().targetContext
            .getSharedPreferences("ortto_push_demo", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .putBoolean("asked_first_open_push", true)
            .commit()
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun closeDemo() {
        scenario.close()
    }

    @Test
    fun signInShowsCompleteThreeTabExperience() {
        compose.onNodeWithText("Push Demo").assertIsDisplayed()
        val email = compose.onNodeWithContentDescription("Email address")
        email.performTextInput("android-ui@example.test")
        email.performImeAction()

        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithText("Push registration").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithContentDescription("Home tab").assertIsDisplayed()
        compose.onNodeWithContentDescription("Delivery tab").assertIsDisplayed().performClick()
        compose.onNodeWithText("FCM token override").assertIsDisplayed()
        compose.onNodeWithContentDescription("Log tab").assertIsDisplayed().performClick()
        compose.onAllNodesWithText("demo@push-demo", substring = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun pushoverFcmRegistrationFlow() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        assumeTrue(InstrumentationRegistry.getArguments().getString("pushoverE2E") == "true")
        instrumentation.uiAutomation
            .executeShellCommand("pm grant com.ortto.demo ${Manifest.permission.POST_NOTIFICATIONS}")
            .close()

        val email = compose.onNodeWithContentDescription("Email address")
        email.performTextInput("android-demo-live@example.test")
        email.performImeAction()
        waitForText("Push registration")

        compose.onNodeWithContentDescription("Delivery tab").performClick()
        waitForText("FCM token override")
        compose.onNodeWithTag("delivery-list")
            .performScrollToNode(hasText("Request and register FCM token"))
        compose.onNodeWithText("Request and register FCM token")
            .performClick()

        waitForText("FCM token registered", timeoutMillis = 30_000)
        compose.onNodeWithText("FCM token registered").assertIsDisplayed()
    }

    private fun waitForText(text: String, timeoutMillis: Long = 5_000) {
        compose.waitUntil(timeoutMillis = timeoutMillis) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
