package goodtime.training.wod.timer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

//TODO: make it unique based on duration, breakDuration, rounds and type to avoid duplicates
@Entity
@TypeConverters(TypeConverter::class)
data class SessionSkeleton (
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var duration: Int = 0,
    var breakDuration: Int = 0,
    var numRounds: Int = 0,
    var type: SessionType = SessionType.REST
) {
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        other as SessionSkeleton
        return (this.duration == other.duration &&
                this.breakDuration == other.breakDuration &&
                this.numRounds == other.numRounds &&
                this.type == other.type)
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + duration
        result = 31 * result + breakDuration
        result = 31 * result + numRounds
        result = 31 * result + type.hashCode()
        return result
    }
}
