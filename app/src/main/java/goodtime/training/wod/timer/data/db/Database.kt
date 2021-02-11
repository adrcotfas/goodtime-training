package goodtime.training.wod.timer.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import goodtime.training.wod.timer.data.model.*

@Database(entities = [Session::class, SessionSkeleton::class, CustomWorkoutSkeleton::class, WeeklyGoal::class],
    version = 1, exportSchema = false)
@TypeConverters(TypeConverter::class)
abstract class Database: RoomDatabase() {
    abstract fun sessionsDao(): SessionDao
    abstract fun sessionSkeletonDao(): SessionSkeletonDao
    abstract fun customWorkoutSkeletonDao(): CustomWorkoutSkeletonDao
    abstract fun weeklyGoalDao(): WeeklyGoalDao
}
