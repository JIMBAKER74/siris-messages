package com.siris.mobile.lib.smsmms.base

import android.util.Log
import com.siris.mobile.lib.smsmms.telemetry.ITelemetryService

abstract class BaseClass(
    /**
     * TAG used for logging/telemetry.
     */
    var TAG: String
) {
    companion object {
        @JvmStatic
        fun LOG_VERBOSE(tag: String, message: String) = ITelemetryService.recordLog(Log.VERBOSE, tag, message)

        @JvmStatic
        fun LOG_DEBUG(tag: String, message: String) = ITelemetryService.recordLog(Log.DEBUG, tag, message)

        @JvmStatic
        fun LOG_INFO(tag: String, message: String) = ITelemetryService.recordLog(Log.INFO, tag, message)

        @JvmStatic
        fun LOG_WARN(tag: String, message: String) = ITelemetryService.recordLog(Log.WARN, tag, message)

        @JvmStatic
        fun LOG_ERROR(tag: String, message: String) = ITelemetryService.recordLog(Log.ERROR, tag, message)

        @JvmStatic
        fun LOG_ASSERT(tag: String, message: String) = ITelemetryService.recordLog(Log.ASSERT, tag, message)
    }
}
