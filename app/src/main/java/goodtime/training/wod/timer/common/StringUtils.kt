package goodtime.training.wod.timer.common

import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class StringUtils {

    companion object {
        fun secondsToMinutesAndSeconds(seconds: Int) : Pair<Int, Int> {
            val min = TimeUnit.SECONDS.toMinutes(seconds.toLong()).toInt()
            val sec = seconds - min * 60
            return Pair(min, sec)
        }

        fun secondsToTimerFormat(elapsed: Int): String {
            val minutes = TimeUnit.SECONDS.toMinutes(elapsed.toLong())
            val seconds = elapsed - minutes * 60
            return (insertPrefixZero(minutes)
                    + ":" + insertPrefixZero(seconds))
        }

        fun secondsOfDayToTimerFormat(seconds: Int, is24HourFormat: Boolean = true): String {
            val time = LocalTime.ofSecondOfDay(seconds.toLong())
            return time.format(DateTimeFormatter.ofPattern(
                    if (is24HourFormat) "HH:mm"
                    else "hh:mm a"))
        }

        fun toFavoriteFormat(session: SessionSkeleton): String {
            return when(session.type) {
                SessionType.AMRAP, SessionType.FOR_TIME, SessionType.REST -> {
                    secondsToNiceFormat(session.duration)
                }
                SessionType.EMOM -> {
                    "${session.numRounds} × ${secondsToNiceFormat(session.duration)}"
                }
                SessionType.HIIT -> {
                    val workString = "${session.duration} s"
                    val breakString = "${session.breakDuration} s"
                    "${session.numRounds} × $workString / $breakString"
                }
            }
        }

        fun secondsToNiceFormat(elapsed: Int): String {
            val duration = secondsToMinutesAndSeconds(elapsed)
            return when {
                duration.first == 0 -> "${duration.second} s"
                duration.second == 0 -> "${duration.first} min"
                else -> "${duration.first} min ${duration.second} s"
            }
        }

        private fun secondsToNiceFormatExtended(elapsed: Int): String {
            val duration = secondsToMinutesAndSeconds(elapsed)
            return when {
                duration.first == 0 -> "${duration.second} second" + if (duration.second > 1) "s" else ""
                duration.second == 0 -> "${duration.first} minute" + if (duration.first > 1) "s" else ""
                else -> {
                    val seconds = duration.second.toString() + " second" + if (duration.second > 1) "s" else ""
                    val minutes = duration.first.toString() + " minute" + if (duration.first > 1) "s" else ""
                    "$minutes and $seconds"
                }
            }
        }

        fun toFavoriteDescriptionDetailed(session: SessionSkeleton): String {
            return when (session.type) {
                SessionType.AMRAP -> "As many rounds/reps as possible in ${
                    secondsToNiceFormatExtended(
                            session.duration
                    )
                }"
                SessionType.FOR_TIME -> "For time with a time cap of ${
                    secondsToNiceFormatExtended(
                            session.duration
                    )
                }"
                SessionType.EMOM -> {
                    (if (session.duration == 60) "Every minute on the minute for ${
                        secondsToNiceFormatExtended(
                                session.numRounds * session.duration
                        )
                    }"
                    else
                        "Every ${secondsToNiceFormatExtended(session.duration)} for " +
                                secondsToNiceFormatExtended(session.numRounds * session.duration))
                }
                SessionType.HIIT -> {
                    val workString =
                            session.duration.toString() + " second" + if (session.duration > 1) "s" else ""
                    val breakString =
                            session.breakDuration.toString() + " second" + if (session.breakDuration > 1) "s" else ""
                    "${session.numRounds} high intensity intervals of $workString of work with $breakString of rest"
                }
                SessionType.REST -> "Rest for ${secondsToNiceFormatExtended(session.duration)}"
            }
        }


        private fun insertPrefixZero(value: Long): String {
            return if (value < 10) "0$value" else value.toString()
        }

        fun formatDateAndTime(millis: Long): String {
            val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
            return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        }

        fun toString(sessionType: SessionType) : String {
            return when (sessionType) {
                SessionType.AMRAP -> "AMRAP"
                SessionType.FOR_TIME -> "FOR TIME"
                SessionType.EMOM -> "INTERVALS"
                SessionType.HIIT -> "HIIT"
                SessionType.REST -> "REST"
            }
        }

        private val congratsStrings = arrayListOf(
                "Good job! \uD83D\uDCAA",
                "Not bad! \uD83E\uDD1C\uD83E\uDD1B",
                "Well done! \uD83D\uDD25",
                "Congrats! \uD83C\uDFC5",
                "Congrats! \uD83C\uDFC6"
        )

        fun generateCongrats() : String {
            val randomIdx = Random.nextInt(0, congratsStrings.size)
            return congratsStrings[randomIdx]
        }

        fun getDaysOfWeekShort() : ArrayList<String> {
            val daysOfWeekRaw = mutableListOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY,
                    DayOfWeek.SATURDAY,
                    DayOfWeek.SUNDAY
            )

            val result = ArrayList<String>(7)
            for (day in daysOfWeekRaw) {
                result.add(day.getDisplayName(TextStyle.NARROW, Locale.getDefault()))
            }
            return result
        }

        fun firstDayOfWeek(): DayOfWeek =
            WeekFields.of(Locale.getDefault()).firstDayOfWeek
    }
}