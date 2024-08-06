package com.sajeg.storycreator.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Story::class], version = 1)
abstract class Database: RoomDatabase() {
    abstract fun storyDao(): StoryDao
}