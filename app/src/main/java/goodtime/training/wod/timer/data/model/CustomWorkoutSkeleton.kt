package goodtime.training.wod.timer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity
@TypeConverters(TypeConverter::class)
class CustomWorkoutSkeleton(
    @PrimaryKey
    var name: String,
    //TODO: check if migration is required for refactoring to List
    var sessions: List<SessionSkeleton>
)
