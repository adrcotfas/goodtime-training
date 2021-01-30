package goodtime.training.wod.timer.data.model

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import androidx.room.ForeignKey.SET_DEFAULT
import java.lang.IllegalArgumentException

@Entity(
        foreignKeys =
        [ForeignKey(
                entity = CustomWorkoutSkeleton::class,
                parentColumns = ["name"],
                childColumns = ["name"],
                onUpdate = CASCADE,
                onDelete = SET_DEFAULT)])
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
        var isTimeBased: Boolean = false) {

    fun isCustom() = skeleton.type == SessionType.CUSTOM

    companion object {
        fun prepareSessionToAdd(
                skeleton: SessionSkeleton,
                id: Long = 0,
                actualDuration: Int = 0,
                actualRounds: Int = 0,
                notes: String? = null,
                completed: Boolean = true) : Session {
            return Session(id, skeleton, actualDuration, actualRounds, notes = notes, isCompleted = completed)
        }

        fun calculateTotal(sessions: ArrayList<SessionSkeleton>): Int {
            var total = 0
            for (session in sessions) {
                total += when (session.type) { //TODO: this ended up being null but how?
                    SessionType.AMRAP, SessionType.FOR_TIME, SessionType.REST -> session.duration
                    SessionType.EMOM -> (session.duration * session.numRounds)
                    SessionType.HIIT -> (session.duration * session.numRounds + session.breakDuration * session.numRounds)
                    else -> throw IllegalArgumentException("invalid for custom")
                }
            }
            return total
        }
    }
}
