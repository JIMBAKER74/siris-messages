package com.siris.messages.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.siris.mobile.messages.common.extensions.notificationManager
import com.siris.mobile.messages.common.helpers.ensureBackgroundThread
import com.siris.messages.extensions.conversationsDB
import com.siris.messages.extensions.deleteMessage
import com.siris.messages.extensions.updateLastConversationMessage
import com.siris.messages.extensions.updateUnreadCountBadge
import com.siris.messages.helpers.IS_MMS
import com.siris.messages.helpers.MESSAGE_ID
import com.siris.messages.helpers.THREAD_ID
import com.siris.messages.helpers.refreshMessages

class DeleteSmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val threadId = intent.getLongExtra(THREAD_ID, 0L)
        val messageId = intent.getLongExtra(MESSAGE_ID, 0L)
        val isMms = intent.getBooleanExtra(IS_MMS, false)
        context.notificationManager.cancel(threadId.hashCode())
        ensureBackgroundThread {
            context.deleteMessage(messageId, isMms)
            context.updateUnreadCountBadge(context.conversationsDB.getUnreadConversations())
            context.updateLastConversationMessage(threadId)
            refreshMessages()
        }
    }
}
