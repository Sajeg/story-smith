package com.sajeg.storycreator

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.compose.ui.text.intl.Locale

object SpeechRecognition {
    private lateinit var stt: SpeechRecognizer
    private lateinit var intent: Intent
    private val listener = StoryActionRecognitionListener()


    fun initRecognition(context: Context) {
        if (SpeechRecognizer.isOnDeviceRecognitionAvailable(context)) {
            stt = SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } else if (SpeechRecognizer.isRecognitionAvailable(context)) {
            stt = SpeechRecognizer.createSpeechRecognizer(context)
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

    fun stopRecognition() {
        stt.stopListening()
    }

    fun startRecognition() {
        stt.startListening(intent)
    }

    fun destroy() {
        stt.destroy()
    }
}