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
        var skeleton: SessionSkeleton,
        var actualDuration: Int = 0,
        var actualRounds: ArrayList<Int> = arrayListOf(),
        var actualReps: Int = 0,
        @Nullable
        var notes: String?,
        var name: String? = null, // non-null for custom workouts
        var timestamp: Long = System.currentTimeMillis()) {

    companion object {
        fun constructSession(
                skeleton: SessionSkeleton,
                actualDuration: Int = 0,
                actualRounds: ArrayList<Int> = arrayListOf(),
                notes: String? = null) : Session {
            return Session(0, skeleton, actualDuration, actualRounds, notes = notes)
        }
    }
}
