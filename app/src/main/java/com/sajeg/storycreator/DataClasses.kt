package com.sajeg.storycreator

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

data class History(
    val title: String,
    val parts: MutableList<StoryPart>,
    val isEnded: Boolean = false
) {
    fun toJsonElement(): JsonElement {
        val parts =

        val data = """{
            "tile": "$title",
            "isEnded": "$isEnded",
            "parts": [
                "${parts[0]}", "${parts[1]}", "${parts[2]}"
            ]
            }"""

        return Json.parseToJsonElement(data)
    }
}

data class StoryPart(
    var role: String,
    var content: String,
    var wasReadAloud: Boolean,
    var suggestions: Array<String>
) {
    fun isModel(): Boolean {
        return role == "Gemini"
    }

    fun getSuggestions(): Array<String> {
        return suggestions
    }

    fun hasSuggestions(): Boolean {
        var emptySuggestion = 0
        for (item in suggestions) {
            if (item == "") {
                emptySuggestion++
            }
        }
        return emptySuggestion == 0
    }

    fun toJsonElement(): JsonElement {
        val data = """{
            "role": "$role",
            "content": "${content.replace("\"", "\\\"")}",
            "wasReadAloud": "$wasReadAloud",
            "suggestions": [
                "${suggestions[0]}", "${suggestions[1]}", "${suggestions[2]}"
            ]
            }"""

        return Json.parseToJsonElement(data)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StoryPart

        return suggestions.contentEquals(other.suggestions)
    }

    override fun hashCode(): Int {
        return suggestions.contentHashCode()
    }
}