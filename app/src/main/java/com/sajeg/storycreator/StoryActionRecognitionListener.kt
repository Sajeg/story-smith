package com.sajeg.storycreator

import android.os.Bundle
import android.speech.RecognitionListener
import android.util.Log

class StoryActionRecognitionListener : RecognitionListener {

    override fun onReadyForSpeech(params: Bundle?) {
        Log.d("Listener", "ready for speech")
        // Called when the recognizer is ready to start listening
    }

    override fun onBeginningOfSpeech() {
        Log.d("Listener", "begining of speech")
        // Called when speech begins
    }

    override fun onRmsChanged(rmsdB: Float) {
        Log.d("Listener", "rms changed")
        // Called when the RMS (root mean square) of the audio changes
    }

    override fun onBufferReceived(buffer: ByteArray?) {
        Log.d("Listener", "buffer received")
        // Called when a buffer of audio data is received
    }

    override fun onEndOfSpeech() {
        Log.d("Listener", "end of speech")
        // Called when speech ends
    }

    override fun onError(error: Int) {
        // Called when an error occurs
        Log.d("Listener", "error")
        SpeechRecognition.errorResponse(error)
    }

    override fun onResults(results: Bundle?) {
        // Called when a set of speech recognition results is available
        SpeechRecognition.resultResponse(results)
    }

    override fun onPartialResults(partialResults: Bundle?) {
        Log.d("Listener", "partialResults")
        // Called when partial results are available
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        Log.d("Listener", "on event")
        // Called when a non-recognition event occurs
    }

}