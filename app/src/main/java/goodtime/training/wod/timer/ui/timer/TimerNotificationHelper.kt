package goodtime.training.wod.timer.ui.timer

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import goodtime.training.wod.timer.common.sound_and_vibration.SoundPlayer
import goodtime.training.wod.timer.common.sound_and_vibration.Vibrator

//TODO: find a better name
class TimerNotificationHelper(
        context: Context,
        private val preferenceHelper: PreferenceHelper,
        private val soundPlayer: SoundPlayer) {

    private val vibrator = Vibrator(context)
    private val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun notifyCountDown() {
        if (preferenceHelper.isSoundEnabled()) {
            val profile = preferenceHelper.getSoundProfile()
            soundPlayer.play(
                    if (profile == "Default") SoundPlayer.START_COUNTDOWN
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

    fun toggleDndMode(enabled: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (notificationManager.isNotificationPolicyAccessGranted) {
                notificationManager.setInterruptionFilter(
                        if (enabled) NotificationManager.INTERRUPTION_FILTER_PRIORITY
                        else NotificationManager.INTERRUPTION_FILTER_ALL)
            }
        }
    }
}