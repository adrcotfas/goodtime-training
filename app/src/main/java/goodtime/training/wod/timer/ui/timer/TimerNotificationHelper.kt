package goodtime.training.wod.timer.ui.timer

import android.content.Context
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import goodtime.training.wod.timer.common.sound_and_vibration.SoundPlayer
import goodtime.training.wod.timer.common.sound_and_vibration.TorchHelper
import goodtime.training.wod.timer.common.sound_and_vibration.VibrationHelper

//TODO: find a better name
class TimerNotificationHelper(
        context: Context,
        private val preferenceHelper: PreferenceHelper,
        private val soundPlayer: SoundPlayer) {

    private val vibrator = VibrationHelper(context)

    private val torchHandler = TorchHelper(context)

    fun notifyCountDown() {
        if (preferenceHelper.isSoundEnabled()) {
            val profile = preferenceHelper.getSoundProfile()
            soundPlayer.play(
                    if (profile == 0) SoundPlayer.START_COUNTDOWN
                    else SoundPlayer.START_COUNTDOWN_GYM)
        }
        if (preferenceHelper.isVibrationEnabled()) {
            vibrator.notifyCountdown()
        }
        if (preferenceHelper.isFlashEnabled()) {
            torchHandler.notifyCountdown()
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
            if (preferenceHelper.isFlashEnabled()) {
                torchHandler.notifyFastTwice()
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
        vibrator.stop()
        torchHandler.stop()
    }

    fun notifyLastRound() {
        if (preferenceHelper.isSoundEnabled() && preferenceHelper.isVoiceEnabled()) {
            soundPlayer.play(SoundPlayer.LAST_ROUND)
        }
    }
}