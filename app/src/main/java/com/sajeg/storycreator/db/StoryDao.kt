package com.sajeg.storycreator.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface StoryDao {
    @Query("SELECT * FROM Story")
    fun getAll(): List<Story>

    @Query("SELECT * FROM STORY WHERE id IN (:id)")
    fun getStory(id: Int): Story

    @Query("SELECT GREATEST(id) FROM Story")
    fun getHighestId(): Int

    @Insert
    fun addStory(story: Story)

    @Delete
    fun deleteStory(story: Story)
}