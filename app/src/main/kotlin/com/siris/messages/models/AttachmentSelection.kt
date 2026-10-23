package com.siris.messages.models

import android.net.Uri
import com.siris.messages.extensions.isImageMimeType
import com.siris.messages.extensions.isVCardMimeType
import com.siris.messages.extensions.isVideoMimeType
import com.siris.messages.helpers.ATTACHMENT_DOCUMENT
import com.siris.messages.helpers.ATTACHMENT_MEDIA
import com.siris.messages.helpers.ATTACHMENT_VCARD

/**
 * Used by [com.siris.messages.activities.ThreadActivity] when user adds attachments to a message. When an attachment is selected, this
 * object is created to represent that attachment. Instances of this object are managed by [com.siris.messages.adapters.AttachmentsAdapter].
 *
 * @property id
 * @property uri
 * @property mimetype
 * @property filename
 * @property isPending
 * @property viewType
 */
data class AttachmentSelection(
    val id: String,
    val uri: Uri,
    val mimetype: String,
    val filename: String,
    var isPending: Boolean,
    val viewType: Int = getViewTypeForMimeType(mimetype)
) {
    companion object {
        fun getViewTypeForMimeType(mimetype: String): Int {
            return when {
                mimetype.isImageMimeType() || mimetype.isVideoMimeType() -> ATTACHMENT_MEDIA
                mimetype.isVCardMimeType() -> ATTACHMENT_VCARD
                else -> ATTACHMENT_DOCUMENT
            }
        }

        fun areItemsTheSame(first: AttachmentSelection, second: AttachmentSelection): Boolean {
            return first.id == second.id
        }

        fun areContentsTheSame(first: AttachmentSelection, second: AttachmentSelection): Boolean {
            return first.uri == second.uri && first.mimetype == second.mimetype && first.filename == second.filename
        }
    }
}
