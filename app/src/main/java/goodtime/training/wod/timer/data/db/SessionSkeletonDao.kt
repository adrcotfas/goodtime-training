package goodtime.training.wod.timer.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import goodtime.training.wod.timer.data.model.TypeConverter

@Dao
interface SessionSkeletonDao {

    @Insert
    @TypeConverters(TypeConverter::class)
    suspend fun add(session: SessionSkeleton)

    @Query("select * from SessionSkeleton where type = :type order by duration")
    @TypeConverters(TypeConverter::class)
    fun get(type: SessionType) : LiveData<List<SessionSkeleton>>

    @Query("delete from SessionSkeleton where id = :id")
    suspend fun remove(id: Long)
}
