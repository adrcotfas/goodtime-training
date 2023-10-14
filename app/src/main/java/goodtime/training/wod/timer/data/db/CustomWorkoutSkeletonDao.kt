package goodtime.training.wod.timer.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.Companion.REPLACE
import goodtime.training.wod.timer.data.model.CustomWorkoutSkeleton
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.TypeConverter

@Dao
interface CustomWorkoutSkeletonDao {
    @Insert(onConflict = REPLACE)
    suspend fun add(skeleton: CustomWorkoutSkeleton)

    @Update
    suspend fun update(skeleton: CustomWorkoutSkeleton)

    @Query("select * from CustomWorkoutSkeleton order by `name`")
    fun get() : LiveData<List<CustomWorkoutSkeleton>>

    @Query("select * from CustomWorkoutSkeleton where name = :name")
    fun get(name: String) : LiveData<CustomWorkoutSkeleton>

    @Query("delete from CustomWorkoutSkeleton where name = :name")
    suspend fun remove(name: String)

    @Query("update CustomWorkoutSkeleton set name = :newName, sessions = :newSessions where name = :name")
    @TypeConverters(TypeConverter::class)
    fun edit(name: String, newName: String, newSessions: List<SessionSkeleton>)
}