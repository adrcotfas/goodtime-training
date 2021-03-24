package goodtime.training.wod.timer.ui.timer

import android.content.Context
import android.util.Log
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import goodtime.training.wod.timer.common.sound_and_vibration.SoundPlayer
import goodtime.training.wod.timer.common.sound_and_vibration.TorchHelper
import goodtime.training.wod.timer.common.sound_and_vibration.VibrationHelper

//TODO: find a better name
class TimerNotificationHelper(
    context: Context,
    private val preferenceHelper: PreferenceHelper,
    private val soundPlayer: SoundPlayer
) {

    private val vibrator = VibrationHelper(context)

    private val torchHandler = TorchHelper(context)

    fun notifyGetReady() {
        if (preferenceHelper.isVoiceEnabled()) {
            val voice = preferenceHelper.getVoiceProfile()
            if (voice == 0) soundPlayer.play(SoundPlayer.GET_READY_KATIE)
            else soundPlayer.play(SoundPlayer.GET_READY_ERIC)
        }
    }

    fun notifyCountDown(firstRound: Boolean = false) {
        if (preferenceHelper.isSoundEnabled()) {
            soundPlayer.play(
                if (preferenceHelper.getSoundProfile() == 0) SoundPlayer.START_COUNTDOWN
                else SoundPlayer.START_COUNTDOWN_GYM
            )
        }
        if (firstRound && preferenceHelper.isVoiceEnabled()) {
            soundPlayer.play(
                if (preferenceHelper.getVoiceProfile() == 0) SoundPlayer.THREE_TWO_ONE_KATIE
                else SoundPlayer.THREE_TWO_ONE_ERIC
            )
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
            if (preferenceHelper.isVoiceEnabled()) {
                soundPlayer.play(
                    if (preferenceHelper.getVoiceProfile() == 0) SoundPlayer.HALFWAY_THERE_KATIE
                    else SoundPlayer.HALFWAY_THERE_ERIC
                )
            }
            notifyInfo()
        }
    }

    fun notifyStart() {
        if (preferenceHelper.isVoiceEnabled()) {
            soundPlayer.play(
                if (preferenceHelper.getVoiceProfile() == 0) SoundPlayer.GO_KATIE
                else SoundPlayer.GO_ERIC
            )
        }
    }

    fun notifyRest() {
        if (preferenceHelper.isVoiceEnabled()) {
            soundPlayer.play(
                if (preferenceHelper.getVoiceProfile() == 0) SoundPlayer.REST_KATIE
                else SoundPlayer.REST_ERIC
            )
        }
    }

    fun notifyTrainingComplete() {
        if (preferenceHelper.isVoiceEnabled()) {
            soundPlayer.play(
                if (preferenceHelper.getVoiceProfile() == 0) SoundPlayer.WELL_DONE_KATIE
                else SoundPlayer.WELL_DONE_ERIC
            )
        }
    }

    fun notifyTenSecRemaining() {
        if (preferenceHelper.isTenSecRemainingNotificationEnabled()) {
            if (preferenceHelper.isVoiceEnabled()) {
                soundPlayer.play(
                    if (preferenceHelper.getVoiceProfile() == 0) SoundPlayer.TEN_SEC_KATIE
                    else SoundPlayer.TEN_SEC_ERIC
                )
            }
            notifyInfo()
        }
    }

    fun notifyLastMinute() {
        if (preferenceHelper.isLastMinuteNotificationEnabled()) {
            if (preferenceHelper.isVoiceEnabled()) {
                soundPlayer.play(
                    if (preferenceHelper.getVoiceProfile() == 0) SoundPlayer.LAST_MINUTE_KATIE
                    else SoundPlayer.LAST_MINUTE_ERIC
                )
            }
            notifyInfo()
        }
    }

    fun notifyLastRound() {
        if (preferenceHelper.isVoiceEnabled()) {
            soundPlayer.play(
                if (preferenceHelper.getVoiceProfile() == 0) SoundPlayer.LAST_ROUND_KATIE
                else SoundPlayer.LAST_ROUND_ERIC
            )
        }
    }

    private fun notifyInfo() {
        if (preferenceHelper.isSoundEnabled()) {
            soundPlayer.play(SoundPlayer.INFO_BEEP)
        }
        if (preferenceHelper.isVibrationEnabled()) {
            vibrator.notifyFastTwice()
        }
        if (preferenceHelper.isFlashEnabled()) {
            torchHandler.notifyFastTwice()
        }
    }

    fun stop() {
        Log.i("TimerNotificationHelper", "stop")
        soundPlayer.stop()
        vibrator.stop()
        torchHandler.stop()
    }
}