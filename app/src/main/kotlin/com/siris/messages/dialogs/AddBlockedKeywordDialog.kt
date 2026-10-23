package com.siris.messages.dialogs

import androidx.appcompat.app.AlertDialog
import com.siris.mobile.messages.common.activities.BaseSimpleActivity
import com.siris.mobile.messages.common.extensions.getAlertDialogBuilder
import com.siris.mobile.messages.common.extensions.setupDialogStuff
import com.siris.mobile.messages.common.extensions.showKeyboard
import com.siris.mobile.messages.common.extensions.value
import com.siris.messages.databinding.DialogAddBlockedKeywordBinding
import com.siris.messages.extensions.config

class AddBlockedKeywordDialog(val activity: BaseSimpleActivity, private val originalKeyword: String? = null, val callback: () -> Unit) {
    init {
        val binding = DialogAddBlockedKeywordBinding.inflate(activity.layoutInflater).apply {
            if (originalKeyword != null) {
                addBlockedKeywordEdittext.setText(originalKeyword)
            }
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(com.siris.mobile.messages.common.R.string.ok, null)
            .setNegativeButton(com.siris.mobile.messages.common.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this) { alertDialog ->
                    alertDialog.showKeyboard(binding.addBlockedKeywordEdittext)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val newBlockedKeyword = binding.addBlockedKeywordEdittext.value
                        if (originalKeyword != null && newBlockedKeyword != originalKeyword) {
                            activity.config.removeBlockedKeyword(originalKeyword)
                        }

                        if (newBlockedKeyword.isNotEmpty()) {
                            activity.config.addBlockedKeyword(newBlockedKeyword)
                        }

                        callback()
                        alertDialog.dismiss()
                    }
                }
            }
    }
}
