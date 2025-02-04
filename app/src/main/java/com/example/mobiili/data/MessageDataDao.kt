package com.example.mobiili.data // or your chosen package

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface MessageDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessageData(message: MessageData)

    @Query("SELECT * FROM message_data")
    suspend fun getAllMessages(): List<MessageData>

    // Method to update all messages (changing the author and imageUrl)
    @Query("UPDATE message_data SET author = :newAuthor, imageUrl = :newImageUrl")
    suspend fun updateAllMessages(newAuthor: String, newImageUrl: String)
}
