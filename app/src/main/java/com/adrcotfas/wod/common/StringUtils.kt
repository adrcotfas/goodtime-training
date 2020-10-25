package com.adrcotfas.wod.common

import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.model.SessionType
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
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

        fun toFavoriteFormat(session: SessionMinimal): String {
            return when(session.type) {
                SessionType.AMRAP -> {
                    secondsToNiceFormat(session.duration)
                }
                SessionType.FOR_TIME -> {
                    "TC ${secondsToNiceFormat(session.duration)}"
                }
                SessionType.EMOM -> {
                    "${session.numRounds} × ${secondsToNiceFormat(session.duration)}"
                }
                SessionType.TABATA -> {
                    val workString = secondsToNiceFormat(session.duration)
                    val breakString = secondsToNiceFormat(session.breakDuration)
                    "${session.numRounds} × $workString / $breakString"
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

        fun toFavoriteDescriptionDetailed(session : SessionMinimal): String {
            return when (session.type) {
                SessionType.AMRAP -> "As many rounds as possible in ${secondsToNiceFormatExtended(session.duration)}"
                SessionType.FOR_TIME -> "For time with a time cap of ${secondsToNiceFormatExtended(session.duration)}"
                SessionType.EMOM -> {
                    (if (session.duration == 60) "Every minute on the minute for ${secondsToNiceFormatExtended(session.numRounds * session.duration)}"
                    else
                        "Every ${secondsToNiceFormatExtended(session.duration)} for " +
                                secondsToNiceFormatExtended(session.numRounds * session.duration)) +
                            " (${session.numRounds} × ${secondsToNiceFormatExtended(session.duration)})"
                }
                SessionType.TABATA -> {
                    val workString = session.duration.toString() + " second" +  if (session.duration > 1) "s" else ""
                    val breakString = session.breakDuration.toString() + " second" +  if (session.breakDuration > 1) "s" else ""
                    "${session.numRounds} high intensity intervals of $workString of work and $breakString of rest"
                }
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

        fun toString(sessionType : SessionType) : String {
            return when (sessionType) {
                SessionType.AMRAP -> "AMRAP"
                SessionType.FOR_TIME -> "FOR TIME"
                SessionType.EMOM -> "EMOM"
                SessionType.TABATA -> "HIIT"
                else -> ""
            }
        }
    }
}