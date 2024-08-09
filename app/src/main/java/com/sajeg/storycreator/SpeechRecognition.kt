package com.sajeg.storycreator

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.compose.ui.text.intl.Locale
import org.json.JSONObject

object SpeechRecognition {
    private lateinit var stt: SpeechRecognizer
    private lateinit var intent: Intent
    private val listener = StoryActionRecognitionListener()
    private var isListening = false

    fun initRecognition(context: Context) {
        stt = if (SpeechRecognizer.isOnDeviceRecognitionAvailable(context)) {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } else if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            Log.e("SpeechRecognition", "Unexpected error while initializing recognizer")
            return
        }
        intent = Intent(RecognizerIntent.ACTION_VOICE_SEARCH_HANDS_FREE)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.current.language + "-" + Locale.current.region
        )
        stt.setRecognitionListener(listener)
    }

    fun errorResponse(error: Int) {
        isListening = false
        if (error == 13 && Build.VERSION.SDK_INT >= 33) {
            downloadModel()
        } else {
            Log.e("RecognitionListener", "Error Recognizing Speech. ERROR CODE: $error")
        }
    }

    fun isListening() : Boolean{
        return isListening
    }

    fun stopRecognition() {
        isListening = false
        stt.stopListening()
    }

    private fun downloadModel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            stt.triggerModelDownload(intent)
        }
    }

    fun startRecognition() {
        isListening = true
        stt.startListening(intent)
    }

    fun destroy() {
        isListening = false
        stt.destroy()
    }

    fun resultResponse(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val speechOutput = matches[0]
            Log.d("RecognitionListener", speechOutput)


            history.parts.add(StoryPart("Sajeg", speechOutput))
            AiCore.action(speechOutput, responseFromModel = { response: JSONObject?, error: Boolean, errorDesc: String? ->
                if (!error) {
                    history.parts.add(
                        StoryPart(
                            role = "Gemini",
                            content = response!!.getString("story"),
                            suggestions = response.getJSONArray("suggestions") as Array<String>
                        )
                    )
                } else {
                    history.title = "Error"
                    history.isEnded = true
                    history.parts.add(
                        StoryPart(
                            role = "Gemini",
                            content = "A error occurred: $errorDesc",
                        )
                    )
                }
            })
        }
    }
}