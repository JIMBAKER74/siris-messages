package com.siris.messages.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.siris.mobile.lib.smsmms.base.BaseClass
import com.siris.mobile.messages.common.helpers.ensureBackgroundThread

abstract class SirisBroadcastReceiver(var TAG: String) : BroadcastReceiver() {

    init {
        TAG = "[RECEIVER:${SirisBroadcastReceiver::class.simpleName}:$TAG]"
    }

    override fun onReceive(context: Context, intent: Intent) {
        LOG_DEBUG("onReceive(intent=$intent): resultCode=$resultCode")
    }

    fun LOG_VERBOSE(message: String) = com.siris.mobile.lib.smsmms.base.BaseClass.LOG_VERBOSE(TAG, message)
    fun LOG_DEBUG(message: String) = com.siris.mobile.lib.smsmms.base.BaseClass.LOG_DEBUG(TAG, message)
    fun LOG_INFO(message: String) = com.siris.mobile.lib.smsmms.base.BaseClass.LOG_INFO(TAG, message)
    fun LOG_WARN(message: String) = com.siris.mobile.lib.smsmms.base.BaseClass.LOG_WARN(TAG, message)
    fun LOG_ERROR(message: String) = com.siris.mobile.lib.smsmms.base.BaseClass.LOG_ERROR(TAG, message)
    fun LOG_ASSERT(message: String) = com.siris.mobile.lib.smsmms.base.BaseClass.LOG_ASSERT(TAG, message)
}

abstract class SendStatusReceiver(TAG: String) : SirisBroadcastReceiver(TAG) {

    // Updates the status of the message in the internal database
    abstract fun updateAndroidDatabase(context: Context, intent: Intent, receiverResultCode: Int)

    // allows the implementer to update the status of the message in their database
    abstract fun updateAppDatabase(context: Context, intent: Intent, receiverResultCode: Int)

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val resultCode = resultCode
        ensureBackgroundThread {
            updateAndroidDatabase(context, intent, resultCode)
            updateAppDatabase(context, intent, resultCode)
        }
    }

    companion object {
        const val SMS_SENT_ACTION = "com.siris.mobile.messages.com.siris.messages.receiver.SMS_SENT"
        const val SMS_DELIVERED_ACTION = "com.siris.mobile.messages.com.siris.messages.receiver.SMS_DELIVERED"

        // Defined by platform, but no constant provided. See docs for SmsManager.sendTextMessage.
        const val EXTRA_ERROR_CODE = "errorCode"
        const val EXTRA_SUB_ID = "subId"

        const val NO_ERROR_CODE = -1
    }
}
