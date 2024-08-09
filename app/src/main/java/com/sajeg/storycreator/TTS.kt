package com.sajeg.storycreator

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
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

    fun speak(text: String) {

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
                    SpeechRecognition.startRecognition()
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
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
