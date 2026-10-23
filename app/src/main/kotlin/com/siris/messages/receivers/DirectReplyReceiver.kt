package com.siris.messages.receivers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.core.app.RemoteInput
import com.siris.mobile.messages.common.extensions.showErrorToast
import com.siris.mobile.messages.common.helpers.SimpleContactsHelper
import com.siris.mobile.messages.common.helpers.ensureBackgroundThread
import com.siris.messages.extensions.*
import com.siris.messages.helpers.REPLY
import com.siris.messages.helpers.THREAD_ID
import com.siris.messages.helpers.THREAD_NUMBER
import com.siris.messages.messaging.sendMessageCompat

class DirectReplyReceiver : SirisBroadcastReceiver("${DirectReplyReceiver::class.simpleName}") {
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val address = intent.getStringExtra(THREAD_NUMBER) ?: return
        val threadId = intent.getLongExtra(THREAD_ID, 0L)
        var body = RemoteInput.getResultsFromIntent(intent)?.getCharSequence(REPLY)?.toString() ?: return
        body = context.removeDiacriticsIfNeeded(body)
        LOG_DEBUG("onReceive(): address=$address, threadId=$threadId, body=$body")

        var subscriptionId: Int? = null
        val availableSIMs = context.subscriptionManagerCompat().activeSubscriptionInfoList
        if ((availableSIMs?.size ?: 0) > 1) {
            val currentSIMCardIndex = context.config.getUseSIMIdAtNumber(address)
            val wantedId = availableSIMs.getOrNull(currentSIMCardIndex)
            if (wantedId != null) {
                subscriptionId = wantedId.subscriptionId
            }
        }

        ensureBackgroundThread {
            var messageId = 0L
            try {
                context.sendMessageCompat(body, listOf(address), subscriptionId, emptyList())
                val message = context.getMessages(threadId, getImageResolutions = false, includeScheduledMessages = false, limit = 1).lastOrNull()
                if (message != null) {
                    context.messagesDB.insertOrUpdate(message)
                    messageId = message.id

                    context.updateLastConversationMessage(threadId)
                }
            } catch (e: Exception) {
                context.showErrorToast(e)
            }

            val photoUri = SimpleContactsHelper(context).getPhotoUriFromPhoneNumber(address)
            val bitmap = context.getNotificationBitmap(photoUri)
            Handler(Looper.getMainLooper()).post {
                context.notificationHelper.showMessageNotification(messageId, address, body, threadId, bitmap, sender = null, alertOnlyOnce = true)
            }

            context.markThreadMessagesRead(threadId)
            context.conversationsDB.markRead(threadId)
        }
    }
}
