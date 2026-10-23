package com.siris.messages.interfaces

import androidx.room.Dao
import androidx.room.Query
import com.siris.messages.models.Attachment

@Dao
interface AttachmentsDao {
    @Query("SELECT * FROM attachments")
    fun getAll(): List<Attachment>
}
