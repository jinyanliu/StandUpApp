package se.sugarest.standup

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View.GONE
import android.widget.TextView
import java.util.*
import kotlin.collections.ArrayList

const val LOG_TAG = "StandUp"
const val VOICE_RECOGNITION_REQUEST_CODE = 1234
const val SECONDS = 10L

class MainActivity : AppCompatActivity() {
    private var hasSelectedPerson: Boolean = false

    private var tvOne: TextView? = null
    private var tvTwo: TextView? = null
    private var tvThree: TextView? = null
    private var tvFour: TextView? = null
    private var tvFive: TextView? = null
    private var tvSix: TextView? = null
    private var tvSeven: TextView? = null
    private var tvEight: TextView? = null
    private var tvNine: TextView? = null
    private var tvTen: TextView? = null
    private var tvEleven: TextView? = null

    private var hasOne: Boolean = false
    private var hasTwo: Boolean = false
    private var hasThree: Boolean = false
    private var hasFour: Boolean = false
    private var hasFive: Boolean = false
    private var hasSix: Boolean = false
    private var hasSeven: Boolean = false
    private var hasEight: Boolean = false
    private var hasNine: Boolean = false
    private var hasTen: Boolean = false
    private var hasEleven: Boolean = false

    private var speak: TextToSpeech? = null

    private var countDownTimer: CountDownTimer? = null

    private var teamMembers: ArrayList<String> = ArrayList()

    private var currentPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findTextViews()

        Thread.sleep(2000)

        speak = TextToSpeech(this@MainActivity, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = speak?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(LOG_TAG, "Language is not supported")
                } else {
                    val toSpeak = "Morning everyone! Who are present today?"
                    speak?.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, "0")
                }
                speak?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onDone(utteranceId: String) {
                        startVoiceRecognitionActivity()
                    }

                    override fun onError(utteranceId: String) {}
                    override fun onStart(utteranceId: String) {}
                })
            } else {
                Log.e(LOG_TAG, "TTS Initialization Failed!")
            }
        })

        countDownTimer = object : CountDownTimer(SECONDS * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                Log.i(LOG_TAG, "Time is up!")
                finishActivity(VOICE_RECOGNITION_REQUEST_CODE)
                currentPosition += 1
                if (currentPosition == teamMembers.size) {
                    val toSpeak =
                        "Thanks " + teamMembers[currentPosition - 1] + "! Time is up! " + " OK! You are the last one! Stand up finished! " + getDayOfTheWeek() + "! Let's do it!"
                    finishStandUp(toSpeak)
                } else {
                    val toSpeak =
                        "Thanks " + teamMembers[currentPosition - 1] + "! Time is up! " + teamMembers[currentPosition] + " please."
                    speak?.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, "0")
                    speak?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onDone(utteranceId: String) {
                            startTimer()
                            startVoiceRecognitionActivity()
                        }

                        override fun onError(utteranceId: String) {}
                        override fun onStart(utteranceId: String) {}
                    })
                }
            }
        }
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

            if (!hasSelectedPerson) {
                for (item in matches) {
                    for (word in item.split(" ")) {
                        selectMembers(word)
                    }
                }

                if (hasSelectedPerson) {
                    hideMembersWhoAreNotPresent()
                    makeMembersList()
                    Log.i(LOG_TAG, "membersList=" + teamMembers.toString())
                    Thread.sleep(1000)
                    startStandUp()
                    return
                }

            } else {
                for (item in matches) {
                    for (word in item.split(" ")) {
                        if (word.contains("next") || word.contains("yes")) {
                            next()
                            return
                        }
                    }
                }

                speak = TextToSpeech(this@MainActivity, TextToSpeech.OnInitListener { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        val result = speak?.setLanguage(Locale.US)
                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.e(LOG_TAG, "Language is not supported")
                        } else {
                            val toSpeak = "Are you finished, " + teamMembers[currentPosition] + "?"
                            speak?.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, "0")
                            speak?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                                override fun onDone(utteranceId: String) {
                                    startVoiceRecognitionActivity()
                                }

                                override fun onError(utteranceId: String) {}
                                override fun onStart(utteranceId: String) {}
                            })

                        }
                    } else {
                        Log.e(LOG_TAG, "TTS Initialization Failed!")
                    }
                })
            }
        }
    }

    private fun startStandUp() {
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

                    if (currentPosition == teamMembers.size) {
                        val toSpeak =
                            "OK! You are the last one! Stand up finished! " + getDayOfTheWeek() + "! Let's do it!"
                        finishStandUp(toSpeak)
                    } else {
                        val toSpeak =
                            "Thanks, " + teamMembers[currentPosition - 1] + "! " + teamMembers[currentPosition] + ", please."
                        speak?.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, "0")
                        speak?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onDone(utteranceId: String) {
                                startTimer()
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

    private fun finishStandUp(toSpeak: String) {
        getDayOfTheWeek()
        speak?.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, "0")
        speak?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onDone(utteranceId: String) {
                finish()
            }

            override fun onError(utteranceId: String) {}
            override fun onStart(utteranceId: String) {}
        })
    }

    private fun startTimer() {
        countDownTimer?.start()
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

    private fun findTextViews() {
        tvOne = findViewById(R.id.tmOne)
        tvTwo = findViewById(R.id.tmTwo)
        tvThree = findViewById(R.id.tmThree)
        tvFour = findViewById(R.id.tmFour)
        tvFive = findViewById(R.id.tmFive)
        tvSix = findViewById(R.id.tmSix)
        tvSeven = findViewById(R.id.tmSeven)
        tvEight = findViewById(R.id.tmEight)
        tvNine = findViewById(R.id.tmNine)
        tvTen = findViewById(R.id.tmTen)
        tvEleven = findViewById(R.id.tmEleven)
    }

    private fun selectMembers(word: String) {
        if (word.contains("one") || word.contains("1")) {
            hasOne = true
            select(tvOne)
        }
        if (word.contains("two") || word.contains("2")) {
            hasTwo = true
            select(tvTwo)
        }
        if (word.contains("three") || word.contains("3")) {
            hasThree = true
            select(tvThree)
        }
        if (word.contains("four") || word.contains("4")) {
            hasFour = true
            select(tvFour)
        }
        if (word.contains("five") || word.contains("5")) {
            hasFive = true
            select(tvFive)
        }
        if (word.contains("six") || word.contains("6")) {
            hasSix = true
            select(tvSix)
        }
        if (word.contains("seven") || word.contains("7")) {
            hasSeven = true
            select(tvSeven)
        }
        if (word.contains("eight") || word.contains("8")) {
            hasEight = true
            select(tvEight)
        }
        if (word.contains("nine") || word.contains("9")) {
            hasNine = true
            select(tvNine)
        }
        if (word.contains("ten") || word.contains("10")) {
            hasTen = true
            select(tvTen)
        }
        if (word.contains("eleven") || word.contains("11")) {
            hasEleven = true
            select(tvEleven)
        }
    }

    private fun hideMembersWhoAreNotPresent() {
        if (!hasOne) {
            setInvisible(tvOne)
        }
        if (!hasTwo) {
            setInvisible(tvTwo)
        }
        if (!hasThree) {
            setInvisible(tvThree)
        }
        if (!hasFour) {
            setInvisible(tvFour)
        }
        if (!hasFive) {
            setInvisible(tvFive)
        }
        if (!hasSix) {
            setInvisible(tvSix)
        }
        if (!hasSeven) {
            setInvisible(tvSeven)
        }
        if (!hasEight) {
            setInvisible(tvEight)
        }
        if (!hasNine) {
            setInvisible(tvNine)
        }
        if (!hasTen) {
            setInvisible(tvTen)
        }
        if (!hasEleven) {
            setInvisible(tvEleven)
        }
    }

    private fun makeMembersList() {
        if (hasOne) {
            teamMembers.add(getName(tvOne))
        }
        if (hasTwo) {
            teamMembers.add(getName(tvTwo))
        }
        if (hasThree) {
            teamMembers.add(getName(tvThree))
        }
        if (hasFour) {
            teamMembers.add(getName(tvFour))
        }
        if (hasFive) {
            teamMembers.add(getName(tvFive))
        }
        if (hasSix) {
            teamMembers.add(getName(tvSix))
        }
        if (hasSeven) {
            teamMembers.add(getName(tvSeven))
        }
        if (hasEight) {
            teamMembers.add(getName(tvEight))
        }
        if (hasNine) {
            teamMembers.add(getName(tvNine))
        }
        if (hasTen) {
            teamMembers.add(getName(tvTen))
        }
        if (hasEleven) {
            teamMembers.add(getName(tvEleven))
        }
    }

    private fun getName(tv: TextView?): String {
        return tv?.text.toString().split(" ")[1]
    }

    private fun setInvisible(view: TextView?) {
        view?.visibility = GONE
    }

    private fun select(view: TextView?) {
        view?.setBackgroundColor(resources.getColor(R.color.colorAccent, null))
        hasSelectedPerson = true
    }

    override fun onDestroy() {
        speak?.stop()
        speak?.shutdown()
        countDownTimer?.cancel()
        super.onDestroy()
    }
}
