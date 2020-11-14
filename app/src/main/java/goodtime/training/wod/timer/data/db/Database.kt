package goodtime.training.wod.timer.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import goodtime.training.wod.timer.data.model.CustomWorkoutSkeleton
import goodtime.training.wod.timer.data.model.Session
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.TypeConverter

@Database(entities = [Session::class, SessionSkeleton::class, CustomWorkoutSkeleton::class],
    version = 1, exportSchema = false)
@TypeConverters(TypeConverter::class)
abstract class Database: RoomDatabase() {
    abstract fun sessionsDao(): SessionDao
    abstract fun sessionSkeletonDao(): SessionSkeletonDao
    abstract fun customWorkoutSkeletonDao(): CustomWorkoutSkeletonDao
}
