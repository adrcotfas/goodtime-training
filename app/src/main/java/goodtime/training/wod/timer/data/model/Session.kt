package goodtime.training.wod.timer.data.model

import androidx.annotation.Nullable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlin.collections.ArrayList

@Entity
@TypeConverters(TypeConverter::class)
data class Session(
        @PrimaryKey(autoGenerate = true)
        var id: Int = 0,
        var skeleton: SessionSkeleton,
        var actualDuration: Int = 0,
        var actualRounds: ArrayList<Int>,
        @Nullable
        var notes: String?,
        var timestamp: Long = System.currentTimeMillis()) {

    //TODO: use a boolean to signal a custom session

    companion object {
        fun constructSession(
                skeleton: SessionSkeleton,
                actualDuration: Int = 0,
                actualRounds: ArrayList<Int> = arrayListOf(),
                notes: String = "") : Session {
            return Session(0, skeleton, actualDuration, actualRounds, notes)
        }
    }
}
