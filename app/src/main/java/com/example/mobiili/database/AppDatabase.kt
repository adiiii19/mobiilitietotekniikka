package com.example.mobiili.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mobiili.data.MessageData
import com.example.mobiili.data.MessageDataDao

@Database(entities = [MessageData::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDataDao(): MessageDataDao
}
