package se.sugarest.standup

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.support.v7.app.AppCompatActivity
import android.util.Log
import java.util.*
import kotlin.collections.ArrayList

const val LOG_TAG = "StandUp"
const val VOICE_RECOGNITION_REQUEST_CODE = 1234
const val SECONDS = 5L

class MainActivity : AppCompatActivity() {

    private var speak: TextToSpeech? = null

    private var countDownTimer: CountDownTimer? = null

    private var teamMembers = listOf("Olivier", "Elviss", "Jinyan", "Vlonjat")

    private var currentPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        countDownTimer = object : CountDownTimer(SECONDS * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                Log.i(LOG_TAG, "Time is up!")
                finishActivity(VOICE_RECOGNITION_REQUEST_CODE)
                currentPosition += 1
                val toSpeak =
                    "Thanks " + teamMembers[currentPosition - 1] + "! Time is up! " + teamMembers[currentPosition] + " please."
                speak?.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, "0")
                speak?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onDone(utteranceId: String) {
                        Log.i(LOG_TAG, "TTS finished")
                        startVoiceRecognitionActivity()
                    }

                    override fun onError(utteranceId: String) {}
                    override fun onStart(utteranceId: String) {}
                })
            }
        }

        speak = TextToSpeech(this@MainActivity, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = speak?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(LOG_TAG, "Language is not supported")
                } else {
                    val toSpeak = "Hello team! " + teamMembers[currentPosition] + ", please."
                    speak?.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, "0")
                }
                speak?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onDone(utteranceId: String) {
                        Log.i(LOG_TAG, "TTS finished")
                        startTimer()
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

    private fun startTimer() {

        countDownTimer?.start()
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

            for (item in matches) {
                for (word in item.split(" ")) {
                    if (word.contains("next")) {
                        next()
                        return
                    }
                }
            }
        }
    }

    private fun next() {
        Log.i(LOG_TAG, "Next captured!")
        countDownTimer?.cancel()
        currentPosition += 1

        speak = TextToSpeech(this@MainActivity, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = speak?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(LOG_TAG, "Language is not supported")
                } else {

                    if (currentPosition >= teamMembers.size) {

                        getDayOfTheWeek()

                        val toSpeak = "OK! Stand up finished! " + getDayOfTheWeek() + "! Let's do it!"
                        speak?.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, "0")
                        speak?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onDone(utteranceId: String) {
                                Log.i(LOG_TAG, "Activity finished")
                                finish()
                            }

                            override fun onError(utteranceId: String) {}
                            override fun onStart(utteranceId: String) {}
                        })
                    } else {
                        val toSpeak = "Thanks! " + teamMembers[currentPosition] + ", please."
                        speak?.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, "0")
                        speak?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onDone(utteranceId: String) {
                                Log.i(LOG_TAG, "TTS finished")
                                startVoiceRecognitionActivity()
                            }

                            override fun onError(utteranceId: String) {}
                            override fun onStart(utteranceId: String) {}
                        })
                    }
                }
            } else {
                Log.e(LOG_TAG, "TTS Initialization Failed!")
            }
        })
    }

    private fun getDayOfTheWeek(): String {
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_WEEK)

        return when (day) {
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> ""
        }
    }

    override fun onDestroy() {
        speak?.stop()
        speak?.shutdown()
        countDownTimer?.cancel()
        super.onDestroy()
    }
}
