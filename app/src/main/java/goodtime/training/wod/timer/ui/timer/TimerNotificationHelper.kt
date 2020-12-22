package goodtime.training.wod.timer.ui.timer

import android.content.Context
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import goodtime.training.wod.timer.common.sound_and_vibration.SoundPlayer
import goodtime.training.wod.timer.common.sound_and_vibration.Vibrator

class TimerNotificationHelper(
        context: Context,
        private val preferenceHelper: PreferenceHelper) {

    private val soundPlayer = SoundPlayer(context)
    private val vibrator = Vibrator(context)

    fun notifyCountDown() {
        if (preferenceHelper.isSoundEnabled()) {
            val profile = preferenceHelper.getSoundProfile()
            soundPlayer.play(
                    if(profile == "Default") SoundPlayer.START_COUNTDOWN
                    else SoundPlayer.START_COUNTDOWN_GYM)
        }
        if (preferenceHelper.isVibrationEnabled()) {
            vibrator.notifyCountdown()
        }
    }

    fun notifyMiddleOfTraining() {
        if (preferenceHelper.isMidNotificationEnabled()) {
            if (preferenceHelper.isSoundEnabled()) {
                soundPlayer.play(SoundPlayer.HALFWAY_THERE_BEEP)
                if (preferenceHelper.isVoiceEnabled()) {
                    soundPlayer.play(SoundPlayer.HALFWAY_THERE_VOICE)
                }
            }
            if (preferenceHelper.isVibrationEnabled()) {
                vibrator.notifyFastTwice()
            }
        }
    }

    fun notifyStart() {
        if (preferenceHelper.isSoundEnabled() && preferenceHelper.isVoiceEnabled()) {
            soundPlayer.play(SoundPlayer.GO)
        }
    }

    fun notifyRest() {
        if (preferenceHelper.isSoundEnabled() && preferenceHelper.isVoiceEnabled()) {
            soundPlayer.play(SoundPlayer.REST)
        }
    }

    fun notifyTrainingComplete() {
        if (preferenceHelper.isSoundEnabled() && preferenceHelper.isVoiceEnabled()) {
            soundPlayer.play(SoundPlayer.WORKOUT_COMPLETE)
        }
    }

    fun stop() {
        soundPlayer.stop()
    }

    fun notifyLastRound() {
        if (preferenceHelper.isSoundEnabled() && preferenceHelper.isVoiceEnabled()) {
            soundPlayer.play(SoundPlayer.LAST_ROUND)
        }
    }
}