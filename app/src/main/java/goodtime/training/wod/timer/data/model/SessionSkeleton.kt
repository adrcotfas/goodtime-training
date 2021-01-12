package goodtime.training.wod.timer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity
@TypeConverters(TypeConverter::class)
data class SessionSkeleton (
        @PrimaryKey(autoGenerate = true)
        var id: Long = 0,
        var duration: Int = 0,
        var breakDuration: Int = 0,
        var numRounds: Int = 0,
        var type: SessionType = SessionType.REST
) {
    fun isSame(other: SessionSkeleton): Boolean {
        return (this.duration == other.duration) &&
                (this.breakDuration == other.breakDuration) &&
                (this.numRounds == other.numRounds) &&
                (this.type == other.type)
    }
}
