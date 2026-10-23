package com.siris.mobile.messages.common.telemetry

interface ITelemetryService {

    companion object {
        fun recordLog(priority: Int, tag: String, message: String) {
            val builtMessage = SirisLogger.log(priority, tag, message)
        }
    }
}
