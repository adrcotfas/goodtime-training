package goodtime.training.wod.timer.ui.stats

import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import goodtime.training.wod.timer.common.*

class MinutesAndSecondsEditTexts(
        private val listener: Listener,
        var isTimeBasedCompleted: Boolean = true,
        private val minutesEt: TextInputEditText,
        private val secondsEt: TextInputEditText,
        actualDuration: Int,
        private val min: Int,
        private val max: Int) {

    private var minMinutesAndSeconds = StringUtils.secondsToMinutesAndSeconds(min)
    private var maxMinutesAndSeconds = StringUtils.secondsToMinutesAndSeconds(max)

    private var minutes = -1
    private var seconds = -1

    interface Listener {
        fun onValidityChanged(isValid: Boolean)
        fun onMaxTimeSet(isMaxTime: Boolean)
        fun onForTimeMinimumConditionViolated()
    }

    init {
        minutesEt.setupZeroPrefixBehaviourOnFocus()
        secondsEt.setupZeroPrefixBehaviourOnFocus()

        minutesEt.addTextChangedListener {

            val tmpMinutes = toInt(it.toString())
            if (tmpMinutes == minutes) return@addTextChangedListener

            minutes = tmpMinutes
            onTextChanged(seconds <= maxMinutesAndSeconds.second)
        }
        secondsEt.addTextChangedListener {
            // for custom workouts with greater minimum,
            // allow the user to enter the two digits before trimming the time
            if (minMinutesAndSeconds.second > 1) {
                if (it.toString().length == 1) {
                    return@addTextChangedListener
                }
            }

            seconds = toInt(it.toString())
            if (seconds > 59) {
                secondsEt.setText(59.toString())
            }
            onTextChanged(minutes > maxMinutesAndSeconds.first)
        }

        val minutesAndSeconds = StringUtils.secondsToMinutesAndSeconds(actualDuration)
        minutesEt.setTextWithZeroPrefix(minutesAndSeconds.first.toString())
        secondsEt.setTextWithZeroPrefix(minutesAndSeconds.second.toString())
    }

    fun setMaximum(minutesFirst: Boolean) {
        if (minutesFirst) {
            minutesEt.setTextWithZeroPrefix(maxMinutesAndSeconds.first.toString())
            secondsEt.setTextWithZeroPrefix(maxMinutesAndSeconds.second.toString())
        } else {
            secondsEt.setTextWithZeroPrefix(maxMinutesAndSeconds.second.toString())
            minutesEt.setTextWithZeroPrefix(maxMinutesAndSeconds.first.toString())
        }
    }

    private fun setMinimum(minutesFirst: Boolean) {
        if (minutesFirst) {
            minutesEt.setTextWithZeroPrefix(minMinutesAndSeconds.first.toString())
            secondsEt.setTextWithZeroPrefix(minMinutesAndSeconds.second.toString())
        } else {
            secondsEt.setTextWithZeroPrefix(minMinutesAndSeconds.second.toString())
            minutesEt.setTextWithZeroPrefix(minMinutesAndSeconds.first.toString())
        }
    }

    fun getCurrentDuration() = minutes * 60 + seconds

    /**
     * Use this minutesFirst trick to avoid
     */
    private fun onTextChanged(minutesFirst: Boolean) {
        if (getCurrentDuration() > max) {
            setMaximum(minutesFirst)
        } else if (min != max && getCurrentDuration() < min && isTimeBasedCompleted) {
            setMinimum(minutesFirst)
        }

        listener.onValidityChanged(getCurrentDuration() != 0)
        listener.onMaxTimeSet(getCurrentDuration() == max)

        if (isTimeBasedCompleted && getCurrentDuration() < min) {
            listener.onForTimeMinimumConditionViolated()
        }
    }
}