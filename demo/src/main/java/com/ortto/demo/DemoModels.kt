package com.ortto.demo

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.ui.graphics.vector.ImageVector
import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class DemoTab(val title: String, val screenName: String, val icon: ImageVector) {
    Home("Home", "sdk-home", Icons.Default.Home),
    Delivery("Delivery", "push-delivery", Icons.Default.Notifications),
    Log("Log", "push-diagnostics", Icons.Default.Terminal),
}

enum class DemoTone { Ready, Working, Success, Warning, Blocked }

data class DemoActionStatus(
    val text: String,
    val tone: DemoTone = DemoTone.Ready,
)

data class DemoWidget(val id: String, val type: String)

data class DemoLogEntry(
    val source: String,
    val level: String,
    val message: String,
    val timestamp: String = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
)

data class ConfigurationIssue(
    val title: String,
    val detail: String,
    val critical: Boolean,
)
