package com.sajeg.storycreator

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.compose.ui.text.intl.Locale
import com.sajeg.storycreator.states.ActionState

object TTS {
    private lateinit var tts: TextToSpeech

    fun initTextToSpeech(context: Context){
        tts = TextToSpeech(context) { status ->
            if (status != TextToSpeech.SUCCESS) {
                Log.e("TTS", "Error Initializing TTS engine")
            }
        }
        try {
            tts.setLanguage(Locale.current.platformLocale)
        } catch (e: Exception) {
            Log.d("TTS", "Failed to set right language")
        }
    }

    fun speak(text: String, actionChanged: (state: ActionState) -> Unit, onFinished: () -> Unit) {
        tts.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "MODEL_MESSAGE"
        )
        tts.setOnUtteranceProgressListener(object :
            UtteranceProgressListener() {
            override fun onDone(utteranceId: String) {
                onFinished()
            }

            @Deprecated("Deprecated in Java", ReplaceWith(
                "actionChanged(ActionState.Error(-1))",
                "com.sajeg.storycreator.states.ActionState"
            )
            )
            override fun onError(utteranceId: String?) {
                actionChanged(ActionState.Error(-1))
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
}
