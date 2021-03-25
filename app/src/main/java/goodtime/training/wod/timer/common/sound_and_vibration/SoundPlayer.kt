package goodtime.training.wod.timer.common.sound_and_vibration

import android.content.Context
import android.content.ContextWrapper
import android.media.*
import goodtime.training.wod.timer.R

class SoundPlayer(base: Context) : ContextWrapper(base) {
    private val soundPool = SoundPool.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .build()
        )
        .setMaxStreams(4)
        .build()

    private var sounds = HashMap<Int, Int>(21)
    private var streamIds = arrayListOf<Int>()

    init {
        sounds[START_COUNTDOWN] = soundPool.load(applicationContext, START_COUNTDOWN, 1)
        sounds[START_COUNTDOWN_GYM] = soundPool.load(applicationContext, START_COUNTDOWN_GYM, 1)
        sounds[INFO_BEEP] = (soundPool.load(applicationContext, INFO_BEEP, 1))
        sounds[TEN_SEC_ERIC] = soundPool.load(applicationContext, TEN_SEC_ERIC, 1)
        sounds[TEN_SEC_KATIE] = soundPool.load(applicationContext, TEN_SEC_KATIE, 1)
        sounds[THREE_TWO_ONE_ERIC] = soundPool.load(applicationContext, THREE_TWO_ONE_ERIC, 1)
        sounds[THREE_TWO_ONE_KATIE] = soundPool.load(applicationContext, THREE_TWO_ONE_KATIE, 1)
        sounds[GET_READY_ERIC] = soundPool.load(applicationContext, GET_READY_ERIC, 1)
        sounds[GET_READY_KATIE] = soundPool.load(applicationContext, GET_READY_KATIE, 1)
        sounds[GO_ERIC] = soundPool.load(applicationContext, GO_ERIC, 1)
        sounds[GO_KATIE] = soundPool.load(applicationContext, GO_KATIE, 1)
        sounds[HALFWAY_THERE_ERIC] = soundPool.load(applicationContext, HALFWAY_THERE_ERIC, 1)
        sounds[HALFWAY_THERE_KATIE] = soundPool.load(applicationContext, HALFWAY_THERE_KATIE, 1)
        sounds[LAST_MINUTE_ERIC] = soundPool.load(applicationContext, LAST_MINUTE_ERIC, 1)
        sounds[LAST_MINUTE_KATIE] = soundPool.load(applicationContext, LAST_MINUTE_KATIE, 1)
        sounds[LAST_ROUND_ERIC] = soundPool.load(applicationContext, LAST_ROUND_ERIC, 1)
        sounds[LAST_ROUND_KATIE] = soundPool.load(applicationContext, LAST_ROUND_KATIE, 1)
        sounds[REST_ERIC] = soundPool.load(applicationContext, REST_ERIC, 1)
        sounds[REST_KATIE] = soundPool.load(applicationContext, REST_KATIE, 1)
        sounds[WELL_DONE_ERIC] = soundPool.load(applicationContext, WELL_DONE_ERIC, 1)
        sounds[WELL_DONE_KATIE] = soundPool.load(applicationContext, WELL_DONE_KATIE, 1)
    }

    companion object {
        const val START_COUNTDOWN: Int = R.raw.beep_start_default
        const val START_COUNTDOWN_GYM: Int = R.raw.beep_start_gym
        const val INFO_BEEP: Int = R.raw.time_halfway_there_beep

        const val TEN_SEC_ERIC: Int = R.raw.voice_10sec_eric
        const val TEN_SEC_KATIE: Int = R.raw.voice_10sec_katie
        const val THREE_TWO_ONE_ERIC: Int = R.raw.voice_321_eric
        const val THREE_TWO_ONE_KATIE: Int = R.raw.voice_321_katie
        const val GET_READY_ERIC: Int = R.raw.voice_get_ready_eric
        const val GET_READY_KATIE: Int = R.raw.voice_get_ready_katie
        const val GO_ERIC: Int = R.raw.voice_go_eric
        const val GO_KATIE: Int = R.raw.voice_go_katie
        const val HALFWAY_THERE_ERIC: Int = R.raw.voice_halfway_there_eric
        const val HALFWAY_THERE_KATIE: Int = R.raw.voice_halfway_there_katie
        const val LAST_MINUTE_ERIC: Int = R.raw.voice_last_minute_eric
        const val LAST_MINUTE_KATIE: Int = R.raw.voice_last_minute_katie
        const val LAST_ROUND_ERIC: Int = R.raw.voice_last_round_eric
        const val LAST_ROUND_KATIE: Int = R.raw.voice_last_round_katie
        const val REST_ERIC: Int = R.raw.voice_rest_eric
        const val REST_KATIE: Int = R.raw.voice_rest_katie
        const val WELL_DONE_ERIC: Int = R.raw.voice_well_done_eric
        const val WELL_DONE_KATIE: Int = R.raw.voice_well_done_katie
    }

    fun play(soundId: Int) {
        streamIds.add(soundPool.play(sounds[soundId]!!, 1.0f, 1.0f, 1, 0, 1.0f))
    }

    fun stop() {
        streamIds.forEach { soundPool.stop(it) }
        streamIds.clear()
    }
}