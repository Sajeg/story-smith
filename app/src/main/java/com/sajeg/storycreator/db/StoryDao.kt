package com.sajeg.storycreator.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sajeg.storycreator.StoryTitle

@Dao
interface StoryDao {
    @Query("SELECT id, title FROM Story")
    fun getStories(): List<StoryTitle>

    @Query("SELECT * FROM STORY WHERE id IN (:id)")
    fun getStory(id: Int): Story

    @Query("SELECT MAX(id) FROM Story")
    fun getHighestId(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveStory(story: Story)

    @Delete
    fun deleteStory(story: Story)
}