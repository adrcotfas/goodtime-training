package goodtime.training.wod.timer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity
@TypeConverters(TypeConverter::class)
class CustomWorkoutSkeleton(
    @PrimaryKey
    var name: String,
    var sessions: ArrayList<SessionSkeleton>
)
