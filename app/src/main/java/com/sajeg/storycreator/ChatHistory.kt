package com.sajeg.storycreator

import android.util.Log

data class ChatHistory(
    val title: String,
    var role: String,
    val content: String,
    var wasReadAloud: Boolean = false,
    val endOfChat: Boolean = false,
    private var suggestions: Array<String> = arrayOf("","","")
) {

    fun isModel(): Boolean{
        return role == "Gemini"
    }
    fun isInitializer(): Boolean{
        //Log.d("ChatHistory", (role == "Gemini").toString())
        return role == "Initializer"
    }

    fun addSuggestions(input: Array<String>){
        val formattedSuggestions = Array(input.size) { i -> input[i]
                .replace("Vorschlag${(i+1)}:", "")
                .replace("Suggestion${(i+1)}:", "")
                .trim() }
        Log.d("ChatHistory", "Added suggestions: ${formattedSuggestions[0]}")
        suggestions = formattedSuggestions
    }

    fun getSuggestions(): Array<String>{
        return suggestions
    }

    fun hasSuggestions(): Boolean {
        var emptySuggestion = 0
        for (item in suggestions) {
            if(item == ""){
                emptySuggestion++
            }
        }
        return emptySuggestion == 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChatHistory

        if (role != other.role) return false
        if (content != other.content) return false
        if (endOfChat != other.endOfChat) return false
        return suggestions.contentEquals(other.suggestions)
    }

    override fun hashCode(): Int {
        var result = role.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + endOfChat.hashCode()
        result = 31 * result + suggestions.contentHashCode()
        return result
    }
}