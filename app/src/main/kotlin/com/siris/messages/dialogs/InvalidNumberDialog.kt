package com.siris.messages.dialogs

import com.siris.mobile.messages.common.activities.BaseSimpleActivity
import com.siris.mobile.messages.common.extensions.getAlertDialogBuilder
import com.siris.mobile.messages.common.extensions.setupDialogStuff
import com.siris.messages.databinding.DialogInvalidNumberBinding

class InvalidNumberDialog(val activity: BaseSimpleActivity, val text: String) {
    init {
        val binding = DialogInvalidNumberBinding.inflate(activity.layoutInflater).apply {
            dialogInvalidNumberDesc.text = text
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(com.siris.mobile.messages.common.R.string.ok) { _, _ -> { } }
            .apply {
                activity.setupDialogStuff(binding.root, this)
            }
    }
}
