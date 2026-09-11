package com.ortto.demo

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.mutableStateListOf
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger

object DemoLog {
    val entries = mutableStateListOf<DemoLogEntry>()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun demo(message: String, level: String = "info") = append("demo", level, message)

    fun append(source: String, level: String, message: String) {
        val add = {
            entries.add(DemoLogEntry(source, level, DemoLogic.sanitizeLog(message)))
            if (entries.size > 300) entries.removeRange(0, entries.size - 300)
        }
        if (Looper.myLooper() == Looper.getMainLooper()) add() else mainHandler.post(add)
    }

    fun sdkLogger(): Logger = Logger.getLogger("ortto-demo-sdk").apply {
        useParentHandlers = false
        handlers.forEach(::removeHandler)
        addHandler(object : java.util.logging.Handler() {
            override fun publish(record: LogRecord?) {
                record ?: return
                val level = when {
                    record.level.intValue() >= Level.SEVERE.intValue() -> "err"
                    record.level.intValue() >= Level.WARNING.intValue() -> "warn"
                    record.level.intValue() <= Level.FINE.intValue() -> "debug"
                    else -> "info"
                }
                append("ortto", level, record.message ?: "")
            }

            override fun flush() = Unit
            override fun close() = Unit
        })
    }

}
