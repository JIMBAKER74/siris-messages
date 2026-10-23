package com.siris.mobile.messages.common.interfaces

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.siris.mobile.messages.common.models.contacts.Group

@Dao
interface GroupsDao {
    @Query("SELECT * FROM groups")
    fun getGroups(): List<Group>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(group: Group): Long

    @Query("DELETE FROM groups WHERE id = :id")
    fun deleteGroupId(id: Long)
}
