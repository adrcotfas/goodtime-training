package goodtime.training.wod.timer.common.soundplayer

import android.content.Context
import android.content.ContextWrapper
import android.media.*
import goodtime.training.wod.timer.R

class SoundPlayer(base: Context) : ContextWrapper(base) {
    private val soundPool = SoundPool.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
                .build())
        .setMaxStreams(2)
        .build()

    private var sounds = HashMap<Int, Int>(4)
    private var streamID = 0

    init {
        sounds[START_COUNTDOWN] = soundPool.load(applicationContext, START_COUNTDOWN, 1)
        sounds[REST] = (soundPool.load(applicationContext, REST, 1))
        sounds[WORKOUT_COMPLETE] = (soundPool.load(applicationContext, WORKOUT_COMPLETE, 1))
        sounds[LAST_ROUND] = (soundPool.load(applicationContext, LAST_ROUND, 1))
        sounds[HALFWAY_THERE_BEEP] = (soundPool.load(applicationContext, HALFWAY_THERE_BEEP, 1))
        sounds[GO] = (soundPool.load(applicationContext, GO, 1))
    }

    companion object {
        const val START_COUNTDOWN: Int = R.raw.start_full
        const val REST: Int = R.raw.rest
        const val WORKOUT_COMPLETE: Int = R.raw.done_well_done
        const val LAST_ROUND: Int = R.raw.last_round
        const val HALFWAY_THERE_BEEP: Int = R.raw.time_halfway_there_beep
        const val GO: Int = R.raw.go
        //TODO: X_SECONDS_REMAINING
    }

    fun play(soundId: Int) {
        streamID = soundPool.play(sounds[soundId]!!, 1.0f, 1.0f, 1, 0, 1.0f)
    }

    fun stop() {
        soundPool.stop(streamID)
    }
}