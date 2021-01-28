package goodtime.training.wod.timer.ui.common

import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.android.parcel.Parcelize
import java.time.LocalDate
import java.time.ZoneId

class DatePickerDialogHelper {

    companion object {
        @Parcelize
        class TodayAndPastDateValidator : CalendarConstraints.DateValidator {
            override fun isValid(date: Long): Boolean {
                return date < LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }
        }

        fun buildDatePicker(millis: Long): MaterialDatePicker<Long> {
            val builder = MaterialDatePicker.Builder.datePicker()
            return builder.setCalendarConstraints(CalendarConstraints.Builder()
                    .setValidator(TodayAndPastDateValidator()).build())
                    .setSelection(millis)
                    .build()
        }
    }
}