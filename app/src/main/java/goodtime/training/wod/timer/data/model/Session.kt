package goodtime.training.wod.timer.data.model

import androidx.room.*
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.ForeignKey.Companion.SET_DEFAULT
import java.lang.IllegalArgumentException

@Entity(
    foreignKeys =
    [ForeignKey(
        entity = CustomWorkoutSkeleton::class,
        parentColumns = ["name"],
        childColumns = ["name"],
        onUpdate = CASCADE,
        onDelete = SET_DEFAULT
    )]
)
@TypeConverters(TypeConverter::class)
//TODO: rename to CompletedWorkout
data class Session(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var skeleton: SessionSkeleton = SessionSkeleton(),
    var actualDuration: Int = 0, // might differ from the skeleton's duration in case of FOR_TIME
    var actualRounds: Int = 0,
    var actualReps: Int = 0,
    var notes: String? = null,

    /**
     *  The name is non-null for custom workouts and null for regular workouts
     *  or deleted custom ones where name is set to default
     */
    var name: String? = null,
    var timestamp: Long = System.currentTimeMillis(),
    var isCompleted: Boolean = true,

    /**
     * True for custom workouts containing FOR_TIME sessions
     */
    var isTimeBased: Boolean = false
) {

    fun isCustom() = skeleton.type == SessionType.CUSTOM

    companion object {
        fun prepareSessionToAdd(
            skeleton: SessionSkeleton,
            id: Long = 0,
            actualDuration: Int = 0,
            actualRounds: Int = 0,
            notes: String? = null,
            completed: Boolean = true
        ): Session {
            return Session(
                id,
                skeleton,
                actualDuration,
                actualRounds,
                notes = notes,
                isCompleted = completed
            )
        }

        fun prepareSessionToAdd(
            skeleton: SessionSkeleton,
            timestamp: Long,
            actualDuration: Int = 0,
            actualRounds: Int = 0,
            actualReps: Int = 0,
            notes: String? = null,
            completed: Boolean = true
        ): Session {
            return Session(
                0,
                skeleton,
                timestamp = timestamp,
                actualDuration = actualDuration,
                actualRounds = actualRounds,
                actualReps = actualReps,
                notes = notes,
                isCompleted = completed
            )
        }

        fun calculateTotal(sessions: List<SessionSkeleton>): Int {
            var total = 0
            for (i in sessions.withIndex()) {
                total += when (i.value.type) {
                    SessionType.AMRAP, SessionType.FOR_TIME, SessionType.REST -> i.value.duration
                    SessionType.INTERVALS -> (i.value.duration * i.value.numRounds)
                    SessionType.HIIT -> (i.value.duration * i.value.numRounds + i.value.breakDuration * i.value.numRounds) -
                            // Don't count the last break if a HIIT is the last session
                            if (i.index == sessions.size - 1) i.value.breakDuration else 0
                    else -> throw IllegalArgumentException("invalid for custom")
                }
            }
            return total
        }

        /**
         * The absolute minimum of seconds needed to complete such a workout, considering FOR TIME sessions
         * For example, you would hypothetically need at least 1 minute and 1 second to complete
         * a 1 minute AMRAM and a 1 minute FOR_TIME
         */
        fun calculateMinimumToComplete(sessions: List<SessionSkeleton>): Int {
            var total = 0
            var numberOfForTimeWorkouts = 0
            for (i in sessions.withIndex()) {
                total += when (i.value.type) {
                    SessionType.AMRAP, SessionType.REST -> i.value.duration
                    SessionType.INTERVALS -> (i.value.duration * i.value.numRounds)
                    SessionType.HIIT -> (i.value.duration * i.value.numRounds + i.value.breakDuration * i.value.numRounds) -
                            // Don't count the last break if a HIIT is the last session
                            if (i.index == sessions.size - 1) i.value.breakDuration else 0
                    else -> 0
                }
                if (i.value.type == SessionType.FOR_TIME) ++numberOfForTimeWorkouts
            }
            return total + numberOfForTimeWorkouts
        }
    }
}
