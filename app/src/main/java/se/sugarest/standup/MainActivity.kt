package se.sugarest.standup

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.support.v7.app.AppCompatActivity
import android.util.Log
import java.util.*

const val LOG_TAG = "StandUp"

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
                    speak?.speak("Hello", TextToSpeech.QUEUE_FLUSH, null, "0")
                }
            } else {
                Log.e(LOG_TAG, "TTS Initialization Failed!")
            }
        })
    }
}
