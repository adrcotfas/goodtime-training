package goodtime.training.wod.timer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity
@TypeConverters(TypeConverter::class)
data class Session(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var duration: Int = 0,
    var breakDuration: Int = 0,
    var numRounds: Int = 0,
    var actualDuration: Int = 0,
    var type: SessionType = SessionType.REST,

    var rounds: Int,
    var timestamp: Long = System.currentTimeMillis(),
    var finished: Boolean) {

    companion object {
        fun constructSession(skeleton: SessionSkeleton, timestamp: Long, rounds: ArrayList<Int> = arrayListOf(0), actualDuration: Int = 0) : Session {
            return Session(0, skeleton.duration, skeleton.breakDuration, skeleton.numRounds, actualDuration, skeleton.type,
                 0, timestamp, true)
        }
    }

}
