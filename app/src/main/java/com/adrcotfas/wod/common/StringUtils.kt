package com.adrcotfas.wod.common

import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.model.SessionType
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.security.InvalidParameterException
import java.util.concurrent.TimeUnit

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

        fun toFavoriteFormat(sessionMinimal: SessionMinimal): String {
            return when(sessionMinimal.type) {
                SessionType.AMRAP -> {
                    secondsToNiceFormat(sessionMinimal.duration)
                }
                SessionType.FOR_TIME -> {
                    "TC ${secondsToNiceFormat(sessionMinimal.duration)}"
                }
                SessionType.EMOM -> {
                    "${sessionMinimal.numRounds} × ${secondsToNiceFormat(sessionMinimal.duration)}"
                }
                SessionType.TABATA -> {
                    if (sessionMinimal.duration == 20 && sessionMinimal.breakDuration == 10 && sessionMinimal.numRounds == 8) {
                        "default"
                    } else {
                        val workString = secondsToNiceFormat(sessionMinimal.duration)
                        val breakString = secondsToNiceFormat(sessionMinimal.breakDuration)
                        "${sessionMinimal.numRounds} × $workString / $breakString"
                    }
                }
                else -> {
                    //TODO: do something here
                    "" }
            }
        }

        private fun secondsToNiceFormat(elapsed: Int): String {
            val duration = secondsToMinutesAndSeconds(elapsed)
            return when {
                duration.first == 0 -> "${duration.second} sec"
                duration.second == 0 -> "${duration.first} min"
                else -> "${duration.first} min ${duration.second} sec"
            }
        }

        fun toFavoriteDescription(favoriteCandidate : SessionMinimal): String {
            return when (favoriteCandidate.type) {
                SessionType.AMRAP, SessionType.TABATA -> "${favoriteCandidate.type.name} ${toFavoriteFormat(
                    favoriteCandidate
                )}"
                SessionType.EMOM -> {
                    if (favoriteCandidate.duration == 60)
                        "EMOM ${favoriteCandidate.duration / 60 * favoriteCandidate.numRounds} min"
                    else toFavoriteFormat(favoriteCandidate)
                }
                SessionType.FOR_TIME -> "FOR TIME ${toFavoriteFormat(favoriteCandidate)}"
                else -> ""
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