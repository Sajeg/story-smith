package com.sajeg.storycreator

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.TextPart
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

object AiCore {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash-latest",
        apiKey = BuildConfig.geminiApiKey,
        systemInstruction = Content(
            parts = listOf(
                TextPart(
                    """
           Create a story, but with a twist: the reader is also the protagonist, 
           that means that the reader needs to say what he wants to do.
           In short: You create a small snippet of a long Story and then the reader can influence what is happening, 
           but you can still decide what the story is about and create plot twists.
           It is important, that you address the reader with you and that you follow the language and 
           the topic given you in the first message.
           Always remember to keep it short, but not too short, so you can still make plot twists. 
           200 to 300 words fit the best, but if you need more it's okay too.
           Don't use the Asterisk Symbol or Brackets at any time. Remember to always use the present tense.
           In the first message generate a title and generate always 3 suggestions after each message. 
           So that you have {"title":"","story":"",suggestions:["","",""]}
                   """
                )
            )
        ),
        safetySettings = listOf(
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE)
        ),
        generationConfig = generationConfig {
            responseMimeType = "application/json"
        }
    )

    private var chat = generativeModel.startChat()

    fun action(prompt: String, responseFromModel: (response: JSONObject?, isError: Boolean, errorDesc: String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val answer: String?
            try {
                answer = chat.sendMessage(prompt).candidates[0].content.parts[0].asTextOrNull()
                val jsonResponse = JSONObject(answer!!)
                Log.d("ResponseViewModel", "Response content: $answer")
                responseFromModel(jsonResponse, false, null)
            } catch (e: Exception) {
                Log.e("ChatModelInit", "Error initializing chat: $e")
                responseFromModel(null, true, e.toString())
            }
        }
    }
}
