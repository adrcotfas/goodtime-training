package goodtime.training.wod.timer.data.model

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import androidx.room.ForeignKey.SET_DEFAULT

@Entity(
        foreignKeys =
        [ForeignKey(
                entity = CustomWorkoutSkeleton::class,
                parentColumns = ["name"],
                childColumns = ["name"],
                onUpdate = CASCADE,
                onDelete = SET_DEFAULT)])
@TypeConverters(TypeConverter::class)
data class Session(
        @PrimaryKey(autoGenerate = true)
        var id: Long = 0,
        var skeleton: SessionSkeleton = SessionSkeleton(), // custom workouts have the skeleton.type == REST
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

    fun isCustom() = skeleton.type == SessionType.REST

    companion object {
        fun prepareSessionToAdd(
                skeleton: SessionSkeleton,
                actualDuration: Int = 0,
                actualRounds: Int = 0,
                notes: String? = null,
                completed: Boolean = true) : Session {
            return Session(0, skeleton, actualDuration, actualRounds, notes = notes, isCompleted = completed)
        }
    }
}
