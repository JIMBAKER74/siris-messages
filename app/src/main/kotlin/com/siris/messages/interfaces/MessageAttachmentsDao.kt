package com.siris.messages.interfaces

import androidx.room.Dao
import androidx.room.Query
import com.siris.messages.models.MessageAttachment

@Dao
interface MessageAttachmentsDao {
    @Query("SELECT * FROM message_attachments")
    fun getAll(): List<MessageAttachment>
}
