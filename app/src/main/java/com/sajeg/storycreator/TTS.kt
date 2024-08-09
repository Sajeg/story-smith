package com.sajeg.storycreator

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.sajeg.storycreator.states.ActionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object TTS {
    private lateinit var tts: TextToSpeech
    private var isSpeaking: Boolean = false

    fun initTextToSpeech(context: Context){
        tts = TextToSpeech(context) { status ->
            if (status != TextToSpeech.SUCCESS) {
                Log.e("StorySmithTTS", "Error Initializing TTS engine")
            }
        }
    }

    fun isSpeaking(): Boolean {
        return isSpeaking
    }

    fun speak(text: String, actionChanged: (state: ActionState) -> Unit, onVoiceResults: (speechOutput: String) -> Unit) {
        tts.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "MODEL_MESSAGE"
        )
        tts.setOnUtteranceProgressListener(object :
            UtteranceProgressListener() {
            override fun onDone(utteranceId: String) {
                isSpeaking = false
                CoroutineScope(Dispatchers.Main).launch {
                    actionChanged(ActionState.Listening)
                    SpeechRecognition.startRecognition( onStateChange = {actionChanged(it)}, onResults = { onVoiceResults(it)})
                }
            }

            @Deprecated("Deprecated in Java", ReplaceWith(
                "actionChanged(ActionState.Error(-1))",
                "com.sajeg.storycreator.states.ActionState"
            )
            )
            override fun onError(utteranceId: String?) {
                isSpeaking = false
                actionChanged(ActionState.Error(-1))
            }

            override fun onStart(utteranceId: String) {
                isSpeaking = true
            }
        })
    }

    fun stop() {
        isSpeaking = false
        tts.stop()
    }
}
