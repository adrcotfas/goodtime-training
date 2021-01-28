package goodtime.training.wod.timer.ui.common

import android.content.Context
import android.text.format.DateFormat
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.*

class TimePickerDialogBuilder(context: Context, private val listener: Listener) {

    private val is24HourFormat = DateFormat.is24HourFormat(context)

    interface Listener {
        fun onTimeSet(secondOfDay: Long)
    }

    fun buildDialog(secondOfDay: Int): MaterialTimePicker {
        val time : LocalTime = LocalTime.ofSecondOfDay(secondOfDay.toLong())
        val dialog = MaterialTimePicker.Builder()
                .setHour(time.hour)
                .setMinute(time.minute)
                .setTimeFormat(if (is24HourFormat) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
                .setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)
                .build()
        dialog.addOnPositiveButtonClickListener{
            val newValue = LocalTime.of(dialog.hour, dialog.minute).toSecondOfDay()
            listener.onTimeSet(newValue.toLong())
        }
        return dialog
    }
}
