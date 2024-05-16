package com.sajeg.storycreator

import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.util.Log

class StoryActionRecognitionListener : RecognitionListener {

    override fun onReadyForSpeech(params: Bundle?) {
        // Called when the recognizer is ready to start listening
    }

    override fun onBeginningOfSpeech() {
        // Called when speech begins
    }

    override fun onRmsChanged(rmsdB: Float) {
        // Called when the RMS (root mean square) of the audio changes
    }

    override fun onBufferReceived(buffer: ByteArray?) {
        // Called when a buffer of audio data is received
    }

    override fun onEndOfSpeech() {
        // Called when speech ends
    }

    override fun onError(error: Int) {
        // Called when an error occurs
        Log.e("RecognitionListener", "Error Recognizing Speech. ERROR CODE: $error")
    }

    override fun onResults(results: Bundle?) {
        // Called when a set of speech recognition results is available
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val speechOutput = matches[0]
            Log.d("RecognitionListener", speechOutput)


            history.add(ChatHistory("Sajeg", speechOutput))
            action(speechOutput, responseFromModel = { response: String, error: Boolean ->
                if (!error) {
                    val parts = response.split("{", "}")
                    val suggestions = parts[1].split(";").toTypedArray()
                    history.add(
                        ChatHistory(
                            role = "Gemini",
                            content = parts[0].trimEnd(),
                        )
                    )
                    history.lastOrNull()?.addSuggestions(suggestions)
                } else {
                    history.add(
                        ChatHistory(
                            role = "Gemini",
                            content = "A error occurred: $response",
                            endOfChat = true
                        )
                    )
                }
            })


        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        // Called when partial results are available
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        // Called when a non-recognition event occurs
    }

}