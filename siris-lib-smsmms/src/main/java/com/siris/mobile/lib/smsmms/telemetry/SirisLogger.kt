package com.siris.mobile.lib.smsmms.telemetry

import android.util.Log

object SirisLogger {

    private val DIVIDER = '|'
    private var logPriority: Int = Log.DEBUG
    private var appVersion: String = ""

    fun log(priority: Int, tag: String, message: String): String {
        val builtMessage = buildLog(message)
        if (priority >= logPriority) {
            Log.println(priority, tag, message)
        }
        return builtMessage
    }

    private fun buildLog(message: String) = "SERIAL-NUMBER${DIVIDER}appV=$appVersion$DIVIDER$message"
}
