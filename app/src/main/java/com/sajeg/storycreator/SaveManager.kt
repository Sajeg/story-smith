package com.sajeg.storycreator

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


object SaveManager {
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