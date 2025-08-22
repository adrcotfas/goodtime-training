package goodtime.training.wod.timer.common.sound_and_vibration

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class VibrationHelper(context: Context) {
    private val vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

    fun notifyCountdown() {
        vibrate(COUNTDOWN)
    }

    fun notifyFastTwice() {
        vibrate(FAST_TWICE)
    }

    private fun vibrate(pattern: LongArray, repeat : Int = -1) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val vibrationAttributes =
                VibrationAttributes
                    .Builder()
                    .setUsage(VibrationAttributes.USAGE_ALARM)
                    .build()
            vibrator.vibrate(
                VibrationEffect.createWaveform(pattern, repeat),
                vibrationAttributes,
            )
        } else {
            val audioAttributes =
                AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
            @Suppress("DEPRECATION")
            vibrator.vibrate(
                VibrationEffect.createWaveform(pattern, repeat),
                audioAttributes,
            )
        }
    }

    fun stop() {
        vibrator.cancel()
    }

    companion object {
        private val FAST_TWICE = longArrayOf(0, 200, 100, 200)
        private val COUNTDOWN = longArrayOf(0,
            400, // 3
            600,
            400, // 2
            600,
            400, // 1
            600,
            900 // GO!
        )
    }
}