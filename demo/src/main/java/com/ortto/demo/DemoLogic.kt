package com.ortto.demo

import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

object DemoLogic {
    fun normalizeEmail(value: String): String = value.trim()

    fun short(value: String?, edge: Int = 7): String {
        if (value.isNullOrBlank()) return "None"
        if (value.length <= edge * 2 + 1) return value
        return value.take(edge) + "…" + value.takeLast(edge)
    }

    fun hasTrackingUrl(value: String): Boolean = queryParameter(value, "tracking_url")
        ?.isNotBlank() == true

    fun deepLinkTarget(value: String): DemoTab? {
        val uri = runCatching { URI(value) }.getOrNull() ?: return null
        return when ((uri.host ?: "").lowercase()) {
            "home", "" -> DemoTab.Home
            "delivery", "campaign", "push", "confirm" -> DemoTab.Delivery
            "log", "logs", "diagnostics" -> DemoTab.Log
            else -> null
        }
    }

    fun tokenFingerprint(value: String?): String {
        if (value.isNullOrBlank()) return "None"
        val bytes = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return bytes.take(6).joinToString("") { "%02x".format(it) }
    }

    fun sanitizeLog(value: String): String = value
        .replace(Regex("\\\"appk\\\":\\\"[^\\\"]+\\\""), "\"appk\":\"[configured]\"")
        .replace(Regex("\\\"s\\\":\\\"[^\\\"]+\\\""), "\"s\":\"[redacted]\"")
        .replace(Regex("\\\"e\\\":\\\"[^\\\"]+\\\""), "\"e\":\"[redacted]\"")
        .replace(Regex("\\\"ei\\\":\\\"[^\\\"]+\\\""), "\"ei\":\"[redacted]\"")
        .replace(Regex("(?i)(token[=: ]+)[A-Za-z0-9_:-]{40,}"), "$1[redacted]")

    private fun queryParameter(value: String, name: String): String? {
        val query = runCatching { URI(value).rawQuery }.getOrNull() ?: return null
        return query.split('&').firstNotNullOfOrNull { part ->
            val components = part.split('=', limit = 2)
            val key = URLDecoder.decode(components.first(), StandardCharsets.UTF_8.name())
            if (key != name) {
                null
            } else {
                URLDecoder.decode(components.getOrElse(1) { "" }, StandardCharsets.UTF_8.name())
            }
        }
    }
}
