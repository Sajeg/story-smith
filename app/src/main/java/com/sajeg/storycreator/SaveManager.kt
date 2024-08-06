package com.sajeg.storycreator

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.room.Room
import com.sajeg.storycreator.db.Database
import com.sajeg.storycreator.db.Story
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalDate


object SaveManager {
    // Room database
    private var db: Database? = null

    fun initDatabase(context: Context) {
        db = Room.databaseBuilder(
            context,
            Database::class.java, "stories"
        ).build()
    }

    fun getNewId(done: (id: Int) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            done(db!!.storyDao().getHighestId() + 1)
        }
    }

    fun saveStory(data: History, id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val storyDao = db!!.storyDao()
                val date = LocalDate.now()
                val story = Story(
                    id = id,
                    title = data.title,
                    date = date.toString(),
                    content = buildJsonArray {
                        for (part in data.parts) {
                            add(part.toJsonElement())
                        }
                    }.toString()
                )
                storyDao.saveStory(story)
            } catch (e: Exception) {
                Log.e("SaveManager", "Saving story failed: ${e.localizedMessage}")
            }
        }
    }

    fun loadStory(id: Int, done: (history: History) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val storyDao = db!!.storyDao()
            val data = storyDao.getStory(id)
            val parts = mutableListOf<StoryPart>()
            val content = Json.parseToJsonElement(data.content).jsonArray

            for (message in content) {
                val suggestions = arrayOf("", "", "")
                for ((i, suggestion) in message.jsonObject["suggestions"]!!.jsonArray.withIndex()) {
                    Log.d("Suggestions", suggestion.toString())
                    suggestions[i] = suggestion.toString().replace('"', ' ').trim()
                }
                parts.add(
                    StoryPart(
                        wasReadAloud = false,
                        role = message.jsonObject["role"]!!.jsonPrimitive.content,
                        content = message.jsonObject["content"]!!.jsonPrimitive.content,
                        suggestions = suggestions
                    )
                )
            }
            val history = History(
                title = data.title,
                parts = parts
            )
            done(history)
        }

    }

    fun getStories(done: (stories: List<StoryTitle>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val storyDao = db!!.storyDao()
            done(storyDao.getStories())
        }
    }

    // Preferences DataStore
    fun readString(value: String, context: Context, onResponse: (data: String) -> Unit) {
        val valueKey = stringPreferencesKey(value)
        CoroutineScope(Dispatchers.IO).launch {
            val preferences = context.dataStore.data.first()
            val data = preferences[valueKey] ?: ""
            onResponse(data)
        }
    }

    fun saveString(value: String, data: String, context: Context) {
        val valueKey = stringPreferencesKey(value)
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.edit { settings ->
                settings[valueKey] = data
            }
        }
    }

    fun readBoolean(value: String, context: Context, onResponse: (data: Boolean?) -> Unit) {
        val valueKey = booleanPreferencesKey(value)
        CoroutineScope(Dispatchers.IO).launch {
            val preferences = context.dataStore.data.first()
            val data = preferences[valueKey]
            onResponse(data)
        }
    }

    fun saveBoolean(value: String, data: Boolean, context: Context) {
        val valueKey = booleanPreferencesKey(value)
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.edit { settings ->
                settings[valueKey] = data
            }
        }
    }
}