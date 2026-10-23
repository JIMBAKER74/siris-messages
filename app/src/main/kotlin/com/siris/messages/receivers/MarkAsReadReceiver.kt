package com.siris.messages.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.siris.mobile.messages.common.extensions.notificationManager
import com.siris.mobile.messages.common.helpers.ensureBackgroundThread
import com.siris.messages.extensions.conversationsDB
import com.siris.messages.extensions.markThreadMessagesRead
import com.siris.messages.extensions.updateUnreadCountBadge
import com.siris.messages.helpers.MARK_AS_READ
import com.siris.messages.helpers.THREAD_ID
import com.siris.messages.helpers.refreshMessages

class MarkAsReadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            MARK_AS_READ -> {
                val threadId = intent.getLongExtra(THREAD_ID, 0L)
                context.notificationManager.cancel(threadId.hashCode())
                ensureBackgroundThread {
                    context.markThreadMessagesRead(threadId)
                    context.conversationsDB.markRead(threadId)
                    context.updateUnreadCountBadge(context.conversationsDB.getUnreadConversations())
                    refreshMessages()
                }
            }
        }
    }
}
