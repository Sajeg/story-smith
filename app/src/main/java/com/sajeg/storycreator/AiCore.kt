package com.sajeg.storycreator

import android.util.Log
import androidx.compose.ui.text.intl.Locale
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.TextPart
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


private val generativeModel = GenerativeModel(
    modelName = "gemini-1.5-flash-latest",
    apiKey = BuildConfig.geminiApiKey,
    systemInstruction = Content(parts = listOf(TextPart("""
           Create a story, but with a twist: the reader is also the protagonist, 
           that means that the reader needs to say what he wants to do.
           In short: You create a small snippet of a long Story and then the reader can influence what is happening.
           It is important, that you address the reader with you.
           Always remember to keep it short.
           Don't use the Asterisk Symbol or Brackets at any time. Remember to always use the present tense.
           At the end of each part specify 3 Suggestions like this:
            { Suggestion1; Suggestion2; Suggestion3 } and always separate them with a semicolon.
                   """))),
    safetySettings = listOf(
        SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE),
        SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE),
        SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE)
    )
)

var chat = generativeModel.startChat()
val locale  = Locale.current.language
var storyTheme: String = ""


fun initStoryTelling(theme: String, responseFromModel: (response: MutableList<String>, isError: Boolean) -> Unit) {
   CoroutineScope(Dispatchers.IO).launch {
       try {
           storyTheme = theme
           val processedAnswers = mutableListOf<String>()
           for (i in 0..2) {
               Log.d("ResponseViewModel", "Theme: $storyTheme")
               Log.d("ResponseViewModel", "Locale: $locale")
               val answer: String? = generativeModel.startChat().sendMessage(
                   "Start the story with the following theme: $storyTheme " +
                           "and with the following language: $locale")
                   .candidates[0].content.parts[0].asTextOrNull()
               Log.d("ResponseViewModel", "Response content: $answer")
               if (answer != null) {
                   processedAnswers.add(answer)
               }
           }
           responseFromModel(processedAnswers, false)
       } catch (e: Exception) {
           Log.e("ChatModelInit", "Error initializing chat: $e")
           responseFromModel(mutableListOf(e.toString()), true)
       }
   }
}

fun addSelectedBeginning (beginning: String) {
    chat = generativeModel.startChat(
        history = listOf(
            content(role = "user") { text(
                "Start the story with the following theme: $storyTheme " +
                    "and with the following locale: $locale") },
            content(role = "model") { text(beginning) }
        )
    )
}

fun action(prompt: String, responseFromModel: (response: String, isError: Boolean) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        val answer:String?
        try {
            answer = chat.sendMessage(prompt).candidates[0].content.parts[0].asTextOrNull()
            Log.d("ResponseViewModel", "Response content: $answer")
            responseFromModel(answer.toString(), false)
        } catch (e: Exception) {
            Log.e("ResponseViewModel", "Error updating last answer: $e")
            responseFromModel(e.toString(), true)
        }
    }
}
