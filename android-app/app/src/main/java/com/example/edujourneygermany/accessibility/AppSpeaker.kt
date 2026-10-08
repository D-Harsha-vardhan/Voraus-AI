package com.example.edujourneygermany.accessibility

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

object AppSpeaker {
    private var tts: TextToSpeech? = null
    var isSpeaking = false
        private set

    fun init(context: Context) {
        if (tts != null) return
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        isSpeaking = true
                    }
                    override fun onDone(utteranceId: String?) {
                        isSpeaking = false
                    }
                    override fun onError(utteranceId: String?) {
                        isSpeaking = false
                    }
                })
            }
        }
    }

    fun speak(text: String, flush: Boolean = true) {
        val queueMode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        tts?.speak(text, queueMode, null, "TTS_ID_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
        isSpeaking = false
    }
}
