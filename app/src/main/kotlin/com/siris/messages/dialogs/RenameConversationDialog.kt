package com.siris.messages.dialogs

import android.app.Activity
import android.content.DialogInterface.BUTTON_POSITIVE
import androidx.appcompat.app.AlertDialog
import com.siris.mobile.messages.common.extensions.getAlertDialogBuilder
import com.siris.mobile.messages.common.extensions.setupDialogStuff
import com.siris.mobile.messages.common.extensions.showKeyboard
import com.siris.mobile.messages.common.extensions.toast
import com.siris.messages.R
import com.siris.messages.databinding.DialogRenameConversationBinding
import com.siris.messages.models.Conversation

class RenameConversationDialog(
    private val activity: Activity,
    private val conversation: Conversation,
    private val callback: (name: String) -> Unit,
) {
    private var dialog: AlertDialog? = null

    init {
        val binding = DialogRenameConversationBinding.inflate(activity.layoutInflater).apply {
            renameConvEditText.apply {
                if (conversation.usesCustomTitle) {
                    setText(conversation.title)
                }

                hint = conversation.title
            }
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(com.siris.mobile.messages.common.R.string.ok, null)
            .setNegativeButton(com.siris.mobile.messages.common.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, R.string.rename_conversation) { alertDialog ->
                    dialog = alertDialog
                    alertDialog.showKeyboard(binding.renameConvEditText)
                    alertDialog.getButton(BUTTON_POSITIVE).apply {
                        setOnClickListener {
                            val newTitle = binding.renameConvEditText.text.toString()
                            if (newTitle.isEmpty()) {
                                activity.toast(com.siris.mobile.messages.common.R.string.empty_name)
                                return@setOnClickListener
                            }

                            callback(newTitle)
                            alertDialog.dismiss()
                        }
                    }
                }
            }
    }
}
