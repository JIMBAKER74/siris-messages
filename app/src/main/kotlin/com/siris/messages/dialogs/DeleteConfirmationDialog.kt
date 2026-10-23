package com.siris.messages.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.siris.mobile.messages.common.extensions.beGoneIf
import com.siris.mobile.messages.common.extensions.getAlertDialogBuilder
import com.siris.mobile.messages.common.extensions.setupDialogStuff
import com.siris.messages.databinding.DialogDeleteConfirmationBinding

class DeleteConfirmationDialog(
    private val activity: Activity,
    private val message: String,
    private val showSkipRecycleBinOption: Boolean,
    private val callback: (skipRecycleBin: Boolean) -> Unit
) {

    private var dialog: AlertDialog? = null
    val binding = DialogDeleteConfirmationBinding.inflate(activity.layoutInflater)

    init {
        binding.deleteRememberTitle.text = message
        binding.skipTheRecycleBinCheckbox.beGoneIf(!showSkipRecycleBinOption)
        activity.getAlertDialogBuilder()
            .setPositiveButton(com.siris.mobile.messages.common.R.string.yes) { _, _ -> dialogConfirmed() }
            .setNegativeButton(com.siris.mobile.messages.common.R.string.no, null)
            .apply {
                activity.setupDialogStuff(binding.root, this) { alertDialog ->
                    dialog = alertDialog
                }
            }
    }

    private fun dialogConfirmed() {
        dialog?.dismiss()
        callback(binding.skipTheRecycleBinCheckbox.isChecked)
    }
}
