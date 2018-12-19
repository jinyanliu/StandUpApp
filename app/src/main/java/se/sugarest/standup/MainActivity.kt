package se.sugarest.standup

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.support.v7.app.AppCompatActivity
import android.util.Log
import java.util.*
import kotlin.collections.ArrayList


const val LOG_TAG = "StandUp"
const val VOICE_RECOGNITION_REQUEST_CODE = 1234

class MainActivity : AppCompatActivity() {

    private var speak: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        speak = TextToSpeech(this@MainActivity, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = speak?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(LOG_TAG, "Language is not supported")
                } else {
                    speak?.speak("Hello! A, please", TextToSpeech.QUEUE_FLUSH, null, "0")
                }
                speak?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onDone(utteranceId: String) {
                        Log.i(LOG_TAG, "TTS finished")
                        startVoiceRecognitionActivity()
                    }

                    override fun onError(utteranceId: String) {}
                    override fun onStart(utteranceId: String) {}
                })
            } else {
                Log.e(LOG_TAG, "TTS Initialization Failed!")
            }
        })
    }

    private fun startVoiceRecognitionActivity() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            "Speech recognition demo"
        )
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) ?: ArrayList()

            if (matches.contains("next")) {
                Log.i(LOG_TAG, "Next captured!")

                speak = TextToSpeech(this@MainActivity, TextToSpeech.OnInitListener { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        val result = speak?.setLanguage(Locale.US)
                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.e(LOG_TAG, "Language is not supported")
                        } else {
                            speak?.speak("Thanks! B, please", TextToSpeech.QUEUE_FLUSH, null, "1")
                        }
                    } else {
                        Log.e(LOG_TAG, "TTS Initialization Failed!")
                    }
                })
            }
        }
    }

    override fun onPause() {
        speak?.stop()
        speak?.shutdown()
        super.onPause()
    }
}
