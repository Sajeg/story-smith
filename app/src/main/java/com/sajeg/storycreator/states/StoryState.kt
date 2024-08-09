package com.sajeg.storycreator.states

import org.json.JSONObject

sealed class StoryState {
    data object FirstLoading : StoryState()
    data class Loading(val prompt: String): StoryState()
    data class Response(val json: JSONObject): StoryState()
    data class Error(val errorDesc: String): StoryState()
    data object Idle : StoryState()
}