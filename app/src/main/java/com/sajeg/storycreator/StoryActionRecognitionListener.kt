package com.sajeg.storycreator

import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.util.Log
import org.json.JSONObject

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

    override fun onPartialResults(partialResults: Bundle?) {
        // Called when partial results are available
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        // Called when a non-recognition event occurs
    }

}