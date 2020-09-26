package com.adrcotfas.wod.common.soundplayer

import android.content.Context
import android.content.ContextWrapper
import android.media.AudioAttributes
import android.media.SoundPool
import com.adrcotfas.wod.R

class SoundPlayer(base: Context) : ContextWrapper(base) {
    private val soundPool = SoundPool.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                //TODO: override sound profile setting
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build())
        .setMaxStreams(2)
        .build()

    private var sounds = HashMap<Int, Int>(4)

    init {
        sounds[COUNTDOWN] = soundPool.load(applicationContext, COUNTDOWN, 1)
        sounds[COUNTDOWN_LONG] = (soundPool.load(applicationContext, COUNTDOWN_LONG, 1))
        sounds[REST] = (soundPool.load(applicationContext, REST, 1))
        sounds[WORKOUT_COMPLETE] = (soundPool.load(applicationContext, WORKOUT_COMPLETE, 1))
    }

    companion object {
        const val COUNTDOWN: Int = R.raw.ding
        const val COUNTDOWN_LONG: Int = R.raw.long_ding
        const val REST: Int = R.raw.rest
        const val WORKOUT_COMPLETE: Int = R.raw.workout_complete
        //TODO: LAST_ROUND, HALFWAY_THERE, X_SECONDS_REMAINING
    }

    fun play(soundId: Int) {
        soundPool.play(sounds[soundId]!!, 1.0f, 1.0f, 1, 0, 1.0f)
    }
}