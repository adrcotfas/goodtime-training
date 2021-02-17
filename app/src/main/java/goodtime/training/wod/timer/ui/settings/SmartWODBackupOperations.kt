package goodtime.training.wod.timer.ui.settings

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.LifecycleCoroutineScope
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import goodtime.training.wod.timer.common.FileUtils
import goodtime.training.wod.timer.common.executeAsyncTask
import goodtime.training.wod.timer.data.db.GoodtimeDatabase
import goodtime.training.wod.timer.data.model.Session
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.math.min

class SmartWODBackupOperations {
    companion object {

        private val ONE_HOUR = TimeUnit.HOURS.toSeconds(1).toInt()
        private const val MAX_ROUNDS = 60

        /**
         * Imports a CSV file exported from SmartWOD
         */
        fun doImportSmartWOD(scope: LifecycleCoroutineScope, context: Context, uri: Uri): Boolean {
            scope.executeAsyncTask(
                onPreExecute = {},
                doInBackground = {
                    val importedSessions = arrayListOf<Session>()
                    lateinit var tmpFile: File
                    try {
                        val importStream = context.contentResolver.openInputStream(uri)
                        importStream.use {
                            tmpFile = File.createTempFile("import_smart_wod", null, context.filesDir)
                            FileUtils.copy(importStream!!, tmpFile)
                            readSmartWODSessions(tmpFile, importedSessions)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return@executeAsyncTask Pair(false, null)
                    } finally {
                        tmpFile.delete()
                    }
                    return@executeAsyncTask Pair(true, importedSessions)
                },
                onPostExecute = {
                    val result = it
                    val success = result.first
                    Toast.makeText(
                        context,
                        if (success) "Backup import successful" else "Backup import failed",
                        Toast.LENGTH_SHORT
                    ).show()
                    if (it.second != null) {
                        (result.second as ArrayList<Session>).forEach {
                            scope.launch { GoodtimeDatabase.getDatabase(context).sessionsDao().add(it) }
                        }
                    }
                })
            return false
        }

        private fun readSmartWODSessions(tempFile: File, importedSessions: ArrayList<Session>) {
            val reader = csvReader {
                quoteChar = '"'
                delimiter = ';'
            }

            // This is how the header looks like:
            // "Date";"Time";"Type";"Duration";"Workout Settings";"Notes";"Round Times"

            reader.open(tempFile) {
                readAllAsSequence().forEachIndexed { index, row: List<String> ->
                    if (index == 0) return@forEachIndexed // skip header
                    val notes = row[5]

                    val skeleton = SessionSkeleton()
                    skeleton.type = extractSessionType(row[2])
                    if (!extractWorkoutProperties(
                            row[4],
                            skeleton
                        )
                    ) return@forEachIndexed //something went wrong; skip it
                    val timestamp = extractTimestamp(row[0], row[1])
                    val minutesAndSeconds = row[3].split(":").toTypedArray()
                    val actualDuration = min(minutesAndSeconds[0].toInt() * 60 + minutesAndSeconds[1].toInt(), TimeUnit.HOURS.toSeconds(2).toInt())
                    val secondsToDecrease = extractSecondsToDecrease(notes)
                    val (actualRounds, actualReps: Int) = extractRoundsAndReps(row)
                    val unfinished = notes.contains("Unfinished workout")

                    importedSessions.add(
                        Session.prepareSessionToAdd(
                            skeleton,
                            timestamp,
                            actualDuration - secondsToDecrease,
                            actualRounds,
                            actualReps,
                            notes,
                            !unfinished
                        )
                    )
                }
            }
        }

        private fun extractSessionType(rawSessionType: String): SessionType {
            return when {
                rawSessionType.contains("AMRAP") -> SessionType.AMRAP
                rawSessionType.contains("FOR TIME") -> SessionType.FOR_TIME
                rawSessionType.contains("FOR TIME") -> SessionType.FOR_TIME
                rawSessionType.contains("TABATA") -> SessionType.HIIT
                rawSessionType.contains("EMOM") -> SessionType.INTERVALS
                else -> SessionType.REST
            }
        }

        private fun extractTimestamp(date: String, time: String): Long {
            val formatter = DateTimeFormatter.ofPattern("MM/dd/yy hh:mm a", Locale.US)
            return LocalDateTime.parse("$date $time", formatter)
                .atZone(ZoneId.systemDefault())
                .toInstant().toEpochMilli()
        }

        private fun extractRoundsAndReps(row: List<String>): Pair<Int, Int> {
            val numberOfRoundsPattern = "R\\d+".toRegex()
            val roundTimesRaw = row[6]
            val actualRounds = numberOfRoundsPattern.findAll(roundTimesRaw).count()

            val numberOfRepsPattern = "reps: (\\d+)".toRegex()
            val findRepsResult = numberOfRepsPattern.find(roundTimesRaw)
            val actualReps: Int = findRepsResult?.groupValues?.get(1)?.toInt() ?: 0
            return Pair(actualRounds, actualReps)
        }

        /**
         * This seems to be valid only for older logs. SmartWod doesn't seem to log unfinished workouts anymore
         */
        private fun extractSecondsToDecrease(notes: String): Int {
            val minutesAndSecondsToDecreasePattern = "Unfinished workout: -(\\d+):(\\d+) minutes".toRegex()
            val secondsToDecreasePattern = "Unfinished workout: -(\\d+) seconds".toRegex()
            val minutesAndSecondsToDecreaseResult = minutesAndSecondsToDecreasePattern.find(notes)
            val secondsToDecreaseResult = secondsToDecreasePattern.find(notes)
            var secondsToDecrease = 0
            if (minutesAndSecondsToDecreaseResult != null) {
                secondsToDecrease = minutesAndSecondsToDecreaseResult.groupValues[1].toInt() * 60 +
                        minutesAndSecondsToDecreaseResult.groupValues[2].toInt()
            } else if (secondsToDecreaseResult != null) {
                secondsToDecrease = secondsToDecreaseResult.groupValues[1].toInt()
            }
            return secondsToDecrease
        }

        /**
         * Extracts work and break duration and rounds from the "Workout Settings" column
         * @return true if the extraction was successful and false otherwise
         */
        private fun extractWorkoutProperties(workoutSettings: String, skeleton: SessionSkeleton): Boolean {
            when (skeleton.type) {
                SessionType.AMRAP -> return extractAMRAPProperties(workoutSettings, skeleton)
                SessionType.FOR_TIME -> {
                    extractForTimeProperties(workoutSettings, skeleton)
                    return true
                }
                SessionType.INTERVALS -> extractIntervalsProperties(workoutSettings, skeleton)
                SessionType.HIIT -> extractHIITProperties(workoutSettings, skeleton)
                else -> return false
            }
            return true
        }

        private fun extractAMRAPProperties(
            workoutSettings: String,
            skeleton: SessionSkeleton
        ): Boolean {
            val minutesPattern = "(\\d+):?(\\d+)? minute".toRegex()
            val secondsPattern = "(\\d+) seconds".toRegex()
            val findMinutesResult = minutesPattern.find(workoutSettings)
            val findSecondsResult = secondsPattern.find(workoutSettings)

            if (findMinutesResult != null) {
                if (findMinutesResult.groupValues[2] != "") {
                    skeleton.duration = min(
                        findMinutesResult.groupValues[1].toInt() * 60 + findMinutesResult.groupValues[2].toInt(),
                        ONE_HOUR)
                } else if (findMinutesResult.groupValues[1] != "") {
                    skeleton.duration = min(findMinutesResult.groupValues[1].toInt() * 60, ONE_HOUR)
                }
            } else if (findSecondsResult != null) {
                skeleton.duration = min(findSecondsResult.groupValues[1].toInt(), ONE_HOUR)
            } else {
                return false
            }
            return true
        }

        private fun extractForTimeProperties(
            workoutSettings: String,
            skeleton: SessionSkeleton
        ) {
            val minutesPattern = "(\\d+):?(\\d+)? minute".toRegex()
            val secondsPattern = "(\\d+) seconds".toRegex()
            val anotherPattern = "(\\d+):(\\d+)".toRegex()

            val findMinutesResult = minutesPattern.find(workoutSettings)
            val findSecondsResult = secondsPattern.find(workoutSettings)
            val findAnotherResult = anotherPattern.find(workoutSettings)

            if (findMinutesResult != null) {
                if (findMinutesResult.groupValues[2] != "") {
                    skeleton.duration = min(
                        findMinutesResult.groupValues[1].toInt() * 60 + findMinutesResult.groupValues[2].toInt(),
                        ONE_HOUR)
                } else if (findMinutesResult.groupValues[1] != "") {
                    skeleton.duration = min(findMinutesResult.groupValues[1].toInt() * 60, ONE_HOUR)
                }
            } else if (findSecondsResult != null) {
                skeleton.duration = min(findSecondsResult.groupValues[1].toInt(), ONE_HOUR)
            } else if (findAnotherResult != null) {
                skeleton.duration = min(
                    findAnotherResult.groupValues[1].toInt() * 60 + findAnotherResult.groupValues[2].toInt(),
                    ONE_HOUR)
            } else {
                skeleton.duration = ONE_HOUR
            }
        }

        private fun extractIntervalsProperties(workoutSettings: String, skeleton: SessionSkeleton): Boolean {
            val everyPatternMinutes = "Every (\\d+):(\\d+) minutes".toRegex()
            val forPatternMinutes = "for (\\d+):(\\d+) minutes".toRegex()
            val everyPatternSeconds = "Every (\\d+) seconds".toRegex()
            val forPatternSeconds = "for (\\d+) seconds".toRegex()
            val asLongAsPossiblePattern = "as long as possible".toRegex()

            val findEveryMinutesResult = everyPatternMinutes.find(workoutSettings)
            val findForMinutesResult = forPatternMinutes.find(workoutSettings)
            val findEverySecondsResult = everyPatternSeconds.find(workoutSettings)
            val findForSecondsResult = forPatternSeconds.find(workoutSettings)
            val findAsLongAsPossibleResult = asLongAsPossiblePattern.find(workoutSettings)

            val maxIntervalsDuration = 10 * 60 + 59
            if (findEveryMinutesResult != null) {
                val minutes = findEveryMinutesResult.groupValues[1].toInt()
                val seconds = findEveryMinutesResult.groupValues[2].toInt()
                skeleton.duration = min(minutes * 60 + seconds, maxIntervalsDuration)
                when {
                    findForMinutesResult != null -> {
                        val totalMinutes = findForMinutesResult.groupValues[1].toInt()
                        val totalSeconds = findForMinutesResult.groupValues[2].toInt()
                        val totalMinutesAndSecondsValue = totalMinutes * 60 + totalSeconds
                        skeleton.numRounds = min(totalMinutesAndSecondsValue / skeleton.duration, MAX_ROUNDS)
                    }
                    findAsLongAsPossibleResult != null -> {
                        skeleton.numRounds = MAX_ROUNDS
                    }
                    else -> {
                        return false
                    }
                }
            } else if (findEverySecondsResult != null) {
                val everySecondValue = findEverySecondsResult.groupValues[1].toInt()
                skeleton.duration = min(everySecondValue, maxIntervalsDuration)
                when {
                    findForMinutesResult != null -> {
                        val totalValue = findForMinutesResult.groupValues[1].toInt()
                        skeleton.numRounds = min(totalValue * 60 / everySecondValue, MAX_ROUNDS)
                    }
                    findForSecondsResult != null -> {
                        val totalValue = findForSecondsResult.groupValues[1].toInt()
                        skeleton.numRounds = min(totalValue / everySecondValue, MAX_ROUNDS)
                    }
                    findAsLongAsPossibleResult != null -> {
                        skeleton.numRounds = MAX_ROUNDS
                    }
                    else -> {
                        return false
                    }
                }
            }
            return true
        }

        private fun extractHIITProperties(workoutSettings: String, skeleton: SessionSkeleton): Boolean {
            val tabataPattern = "(\\d+)/(\\d+) x (\\d+)".toRegex()

            val setsPattern = "(\\d+) sets of".toRegex()
            val findSetsResult = setsPattern.find(workoutSettings)
            val tabataResult = tabataPattern.find(workoutSettings)

            var sets = 1
            if (findSetsResult != null) {
                sets = findSetsResult.groupValues[1].toInt()
            }

            if (tabataResult != null) {
                skeleton.duration = min(tabataResult.groupValues[1].toInt(), 90)
                skeleton.breakDuration = min(tabataResult.groupValues[2].toInt(), 90)
                skeleton.numRounds = min(tabataResult.groupValues[3].toInt() * sets, 60)
            } else {
                return false
            }
            return true
        }
    }
}