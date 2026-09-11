package com.ortto.demo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoLogicTest {
    @Test
    fun normalizeEmailTrimsWithoutChangingIdentity() {
        assertEquals("person+push@example.test", DemoLogic.normalizeEmail("  person+push@example.test \n"))
    }

    @Test
    fun shortKeepsSmallValuesAndReducesLargeValues() {
        assertEquals("None", DemoLogic.short(null))
        assertEquals("small", DemoLogic.short("small"))
        assertEquals("abc…hij", DemoLogic.short("abcdefghij", edge = 3))
    }

    @Test
    fun trackingUrlMustBePresentAndNonEmpty() {
        assertTrue(DemoLogic.hasTrackingUrl("ortto-demo-android://delivery?tracking_url=https%3A%2F%2Fexample.test%2Fc%3Fx%3D1"))
        assertFalse(DemoLogic.hasTrackingUrl("ortto-demo-android://delivery?tracking_url="))
        assertFalse(DemoLogic.hasTrackingUrl("not a uri"))
    }

    @Test
    fun deepLinksMapToTheThreeDemoTabs() {
        assertEquals(DemoTab.Home, DemoLogic.deepLinkTarget("ortto-demo-android://home"))
        assertEquals(DemoTab.Delivery, DemoLogic.deepLinkTarget("ortto-demo-android://push"))
        assertEquals(DemoTab.Log, DemoLogic.deepLinkTarget("ortto-demo-android://diagnostics"))
        assertNull(DemoLogic.deepLinkTarget("ortto-demo-android://unknown"))
    }

    @Test
    fun tokenFingerprintIsStableAndDoesNotExposeTheToken() {
        val fingerprint = DemoLogic.tokenFingerprint("a-realistic-fcm-token-value")
        assertEquals("29de20c549e9", fingerprint)
        assertFalse(fingerprint.contains("token"))
    }

    @Test
    fun diagnosticLogRedactsCredentialsAndIdentityValues() {
        val token = "fcm-token-abcdefghijklmnopqrstuvwxyz-0123456789-ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val sanitized = DemoLogic.sanitizeLog(
            "{\"appk\":\"secret-key\",\"e\":\"person@example.test\",\"ei\":\"external-id\",\"s\":\"session-id\"} token: $token",
        )

        assertFalse(sanitized.contains("secret-key"))
        assertFalse(sanitized.contains("person@example.test"))
        assertFalse(sanitized.contains("external-id"))
        assertFalse(sanitized.contains("session-id"))
        assertFalse(sanitized.contains(token))
    }
}
