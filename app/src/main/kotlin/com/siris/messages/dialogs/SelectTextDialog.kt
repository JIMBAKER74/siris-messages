package com.siris.messages.dialogs

import com.siris.mobile.messages.common.activities.BaseSimpleActivity
import com.siris.mobile.messages.common.extensions.getAlertDialogBuilder
import com.siris.mobile.messages.common.extensions.setupDialogStuff
import com.siris.messages.databinding.DialogSelectTextBinding

// helper dialog for selecting just a part of a message, not copying the whole into clipboard
class SelectTextDialog(val activity: BaseSimpleActivity, val text: String) {
    init {
        val binding = DialogSelectTextBinding.inflate(activity.layoutInflater).apply {
            dialogSelectTextValue.text = text
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(com.siris.mobile.messages.common.R.string.ok) { _, _ -> { } }
            .apply {
                activity.setupDialogStuff(binding.root, this)
            }
    }
}
