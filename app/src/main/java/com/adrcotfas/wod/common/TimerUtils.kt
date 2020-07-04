package com.adrcotfas.wod.common

import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.model.SessionType
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.security.InvalidParameterException
import java.util.concurrent.TimeUnit

class TimerUtils {

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

        fun toFavoriteFormat(sessionMinimal: SessionMinimal): String {
            return when(sessionMinimal.type) {
                SessionType.AMRAP, SessionType.FOR_TIME -> {
                    secondsToNiceFormat(sessionMinimal.duration)
                }
                SessionType.EMOM -> {
                    "${sessionMinimal.numRounds} ×" + secondsToNiceFormat(sessionMinimal.duration)
                }
                SessionType.TABATA -> {
                    val workString = secondsToNiceFormat(sessionMinimal.duration)
                    val breakString = secondsToNiceFormat(sessionMinimal.breakDuration)
                    "${sessionMinimal.numRounds} × $workString / $breakString"
                }
                else -> throw InvalidParameterException("received: ${sessionMinimal.type}")
            }
        }

        private fun secondsToNiceFormat(elapsed: Int): String {
            val duration = secondsToMinutesAndSeconds(elapsed)
            return when {
                duration.first == 0 -> "$duration.second sec"
                duration.second == 0 -> "${duration.first} min"
                else -> "${duration.first} min ${duration.second} sec"
            }
        }

        private fun insertPrefixZero(value: Long): String {
            return if (value < 10) "0$value" else value.toString()
        }

        private val dateTimeFormatter: DateTimeFormatter =
            DateTimeFormat.forPattern("EEE', 'MMM d', ' yyyy, HH:mm")
        fun formatDateAndTime(millis: Long): String {
            return dateTimeFormatter.print(millis)
        }
    }
}