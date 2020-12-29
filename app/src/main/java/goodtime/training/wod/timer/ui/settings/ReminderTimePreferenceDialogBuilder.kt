package goodtime.training.wod.timer.ui.settings

import android.content.Context
import android.text.format.DateFormat
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.LocalTime

class ReminderTimePreferenceDialogBuilder(context: Context, private val listener: Listener) {

    private val is24HourFormat = DateFormat.is24HourFormat(context)

    interface Listener {
        fun onReminderTimeSet(secondOfDay: Int)
    }

    fun buildDialog(secondOfDay: Int): MaterialTimePicker {
        val time : LocalTime = LocalTime.ofSecondOfDay(secondOfDay.toLong())
        val dialog = MaterialTimePicker.Builder()
                .setHour(time.hour)
                .setMinute(time.minute)
                .setTimeFormat(if (is24HourFormat) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .build()
        dialog.addOnPositiveButtonClickListener{
            val newValue = LocalTime.of(dialog.hour, dialog.minute).toSecondOfDay()
            listener.onReminderTimeSet(newValue)
        }
        return dialog
    }
}
