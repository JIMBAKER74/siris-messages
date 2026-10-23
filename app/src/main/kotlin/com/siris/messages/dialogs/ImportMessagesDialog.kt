package com.siris.messages.dialogs

import androidx.appcompat.app.AlertDialog
import com.siris.mobile.messages.common.extensions.getAlertDialogBuilder
import com.siris.mobile.messages.common.extensions.setupDialogStuff
import com.siris.mobile.messages.common.extensions.toast
import com.siris.mobile.messages.common.helpers.ensureBackgroundThread
import com.siris.messages.R
import com.siris.messages.activities.SimpleActivity
import com.siris.messages.databinding.DialogImportMessagesBinding
import com.siris.messages.extensions.config
import com.siris.messages.helpers.MessagesImporter
import com.siris.messages.models.ImportResult
import com.siris.messages.models.MessagesBackup

class ImportMessagesDialog(
    private val activity: SimpleActivity,
    private val messages: List<MessagesBackup>,
) {

    private val config = activity.config

    init {
        var ignoreClicks = false
        val binding = DialogImportMessagesBinding.inflate(activity.layoutInflater).apply {
            importSmsCheckbox.isChecked = config.importSms
            importMmsCheckbox.isChecked = config.importMms
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(com.siris.mobile.messages.common.R.string.ok, null)
            .setNegativeButton(com.siris.mobile.messages.common.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, R.string.import_messages) { alertDialog ->
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        if (ignoreClicks) {
                            return@setOnClickListener
                        }

                        if (!binding.importSmsCheckbox.isChecked && !binding.importMmsCheckbox.isChecked) {
                            activity.toast(R.string.no_option_selected)
                            return@setOnClickListener
                        }

                        ignoreClicks = true
                        activity.toast(com.siris.mobile.messages.common.R.string.importing)
                        config.importSms = binding.importSmsCheckbox.isChecked
                        config.importMms = binding.importMmsCheckbox.isChecked
                        ensureBackgroundThread {
                            MessagesImporter(activity).restoreMessages(messages) {
                                handleParseResult(it)
                                alertDialog.dismiss()
                            }
                        }
                    }
                }
            }
    }

    private fun handleParseResult(result: ImportResult) {
        activity.toast(
            when (result) {
                ImportResult.IMPORT_OK -> com.siris.mobile.messages.common.R.string.importing_successful
                ImportResult.IMPORT_PARTIAL -> com.siris.mobile.messages.common.R.string.importing_some_entries_failed
                ImportResult.IMPORT_FAIL -> com.siris.mobile.messages.common.R.string.importing_failed
                else -> com.siris.mobile.messages.common.R.string.no_items_found
            }
        )
    }
}
