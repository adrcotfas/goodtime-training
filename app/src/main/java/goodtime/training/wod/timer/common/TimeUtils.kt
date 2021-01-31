package goodtime.training.wod.timer.common

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class TimeUtils {
    companion object {
        fun millisToSecondOfDay(millis: Long): Long {
            return Instant.ofEpochMilli(millis)
                    .atZone(ZoneId.systemDefault()).toLocalTime().toSecondOfDay().toLong()
        }

        fun millisToLocalDate(millis: Long): LocalDate {
            return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        }

        fun formatDateLong(date: LocalDate): String {
            return date.format(DateTimeFormatter.ofPattern("EEE', 'MMM d', ' yyyy"))
        }

        fun formatTime(time: LocalTime): String {
            return time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
        }
    }
}