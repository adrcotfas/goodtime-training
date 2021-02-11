package goodtime.training.wod.timer.common

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

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

        fun firstDayOfCurrentWeekMillis() : Long {
            return LocalDate.now()
                    .atStartOfDay()
                    .with(TemporalAdjusters.previousOrSame(StringUtils.firstDayOfWeek()))
                    .atZone(ZoneId.systemDefault())
                    .toInstant().toEpochMilli()
        }

        fun firstDayOfLastWeekMillis(): Long {
            return LocalDate.now()
                    .minus(1, ChronoUnit.WEEKS)
                    .atStartOfDay()
                    .with(TemporalAdjusters.previousOrSame(StringUtils.firstDayOfWeek()))
                    .atZone(ZoneId.systemDefault())
                    .toInstant().toEpochMilli()
        }

        fun nowMillis(): Long {
            return LocalDateTime.now()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
        }
    }
}