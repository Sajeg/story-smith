package com.sajeg.storycreator

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

data class History(
    var title: String,
    val parts: MutableList<StoryPart>,
    var isEnded: Boolean = false
) {
    fun toJsonElement(): JsonElement {
        val jsonObject = buildJsonObject {
            put("title", title)
            put("isEnded", isEnded)
            putJsonArray("parts") {
                for (part in parts) {
                    add(part.toJsonElement())
                }
            }
        }
        return jsonObject
    }
}

data class StoryPart(
    var role: String,
    var content: String,
    var wasReadAloud: Boolean = false,
    var suggestions: Array<String> = arrayOf("","","")
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