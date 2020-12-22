package goodtime.training.wod.timer.common.sound_and_vibration

import android.content.Context
import android.os.Vibrator

class Vibrator(context: Context) {
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    fun notifyCountdown() {
        vibrator.vibrate(COUNTDOWN, -1)
    }

    fun notifyFastTwice() {
            vibrator.vibrate(FAST_TWICE, -1)
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