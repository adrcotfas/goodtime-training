package goodtime.training.wod.timer.data.model

import androidx.annotation.Nullable
import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import androidx.room.ForeignKey.SET_DEFAULT

@Entity(
        foreignKeys =
        [ForeignKey(
                entity = CustomWorkoutSkeleton::class,
                parentColumns = [ "name" ],
                childColumns = [ "name" ],
                onUpdate = CASCADE,
                onDelete = SET_DEFAULT)])
@TypeConverters(TypeConverter::class)
data class Session(
        @PrimaryKey(autoGenerate = true)
        var id: Long = 0,
        var skeleton: SessionSkeleton = SessionSkeleton(),
        var actualDuration: Int = 0,
        var actualRounds: Int = 0,
        var actualReps: Int = 0,
        @Nullable
        var notes: String? = null,
        var name: String? = null, // non-null for custom workouts
        var timestamp: Long = System.currentTimeMillis(),
        var completed: Boolean = true) {

    companion object {
        fun constructSession(
                skeleton: SessionSkeleton,
                actualDuration: Int = 0,
                actualRounds: Int = 0,
                notes: String? = null,
                completed: Boolean = true) : Session {
            return Session(0, skeleton, actualDuration, actualRounds, notes = notes, completed = completed)
        }
    }
}
