package com.example.mobiili.data // or wherever your project package is

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "message_data")
data class MessageData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val author: String,
    val body: String,
    val imageUrl: String
)

