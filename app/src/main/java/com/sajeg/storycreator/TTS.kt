package com.sajeg.storycreator

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.PlatformLocale
import com.sajeg.storycreator.states.ActionState

object TTS {
    private lateinit var tts: TextToSpeech
    private lateinit var language: PlatformLocale
    private var onStateChangeCallback: ((ActionState) -> Unit)? = null

    fun initTextToSpeech(context: Context){
        tts = TextToSpeech(context) { status ->
            if (status != TextToSpeech.SUCCESS) {
                Log.e("TTS", "Error Initializing TTS engine")
            }
        }
        try {
            SaveManager.readInt("language", context) {
                val languageCode = arrayOf("en-US", "de-DE", "fr-FR", "es-ES", "it-IT")
                language = if (it == null) {
                    Locale.current.platformLocale
                } else {
                    java.util.Locale.forLanguageTag(languageCode[it])
                }
            }
        } catch (e: Exception) {
            Log.d("TTS", "Failed to set right language")
        }
    }

    fun speak(text: String, actionChanged: (state: ActionState) -> Unit, onFinished: () -> Unit) {
        onStateChangeCallback = actionChanged
        tts.setLanguage(language)
        tts.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "MODEL_MESSAGE"
        )
        tts.setOnUtteranceProgressListener(object :
            UtteranceProgressListener() {
            override fun onDone(utteranceId: String) {
                onStateChangeCallback!!.invoke(ActionState.Waiting)
                onFinished()
            }

            override fun onError(utteranceId: String?) {
                onStateChangeCallback!!.invoke(ActionState.Error(-1))
            }

            override fun onStart(utteranceId: String) {
                actionChanged(ActionState.Speaking)
            }
        })
    }

    fun stop() {
        tts.stop()
    }

    fun destroy() {
        tts.shutdown()
    }

    fun setLanguage(input: String) {
        language = java.util.Locale.forLanguageTag(input)
    }
}
