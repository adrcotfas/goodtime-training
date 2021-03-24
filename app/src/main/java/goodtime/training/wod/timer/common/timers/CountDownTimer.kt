package goodtime.training.wod.timer.common.timers

import java.util.concurrent.TimeUnit
import kotlin.math.ceil

class CountDownTimer(secondsInTheFuture: Long, originalSeconds: Long, private val listener: Listener)
    : android.os.CountDownTimer(TimeUnit.SECONDS.toMillis(secondsInTheFuture), 1000) {

    private val halfway = ceil(originalSeconds / 2.0).toInt() - 1
    private val shouldNotifyTenSecRemaining = originalSeconds >= 40
    private val shouldNotifyLastMinute = originalSeconds >= 180

    var seconds = 0
        private set

    interface Listener {
        fun onTick(seconds : Int)
        fun onFinishSet()
        fun onHalfwayThere()
        fun onTenSecRemaining()
        fun onLastMinute()
    }

    override fun onTick(millisUntilFinished: Long) {
        // workaround for a bug in CountDownTimer which causes
        // onTick to be called twice inside a countDownInterval
        if (millisUntilFinished < 100) {
            return
        }

        seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished).toInt()
        listener.onTick(seconds)

        if (seconds == halfway) {
            listener.onHalfwayThere()
        }
        if (seconds == 9 && shouldNotifyTenSecRemaining) {
            listener.onTenSecRemaining()
        }
        if (seconds == 59 && shouldNotifyLastMinute) {
            listener.onLastMinute()
        }
    }

    override fun onFinish() {
        listener.onFinishSet()
    }
}
