package com.adrcotfas.wod.common.soundplayer

import android.content.Context
import android.content.ContextWrapper
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler

//TODO: preference for sound every minute /
// last x minutes, sound in the middle of a workout (Tabata)
// find a cool synthesized voice or custom voice
class SoundPlayer(base: Context?) : ContextWrapper(base) {

    companion object {
        const val COUNTDOWN: Int = com.adrcotfas.wod.R.raw.ding
        const val COUNTDOWN_LONG: Int = com.adrcotfas.wod.R.raw.long_ding
        const val REST: Int = com.adrcotfas.wod.R.raw.rest
        const val WORKOUT_COMPLETE: Int = com.adrcotfas.wod.R.raw.workout_complete
    }

    fun play(sound: Int) {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val soundPool = SoundPool.Builder()
            .setAudioAttributes(attributes)
            .build()
        val soundId = soundPool.load(applicationContext, sound, 1)

        soundPool.setOnLoadCompleteListener { sp: SoundPool, _, _ ->
            sp.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
            //TODO: find a more elegant solution for the SoundPool crash
            Handler().postDelayed({ sp.release()}, 3000)
        }
    }
}