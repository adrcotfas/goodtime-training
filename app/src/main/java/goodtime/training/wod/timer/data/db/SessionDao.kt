package goodtime.training.wod.timer.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import goodtime.training.wod.timer.common.TimeUtils
import goodtime.training.wod.timer.data.model.Session
import goodtime.training.wod.timer.data.model.TypeConverter

@Dao
interface SessionDao {

    @Insert
    @TypeConverters(TypeConverter::class)
    suspend fun add(session: Session)

    @Update
    fun edit(session: Session)

    @Query("select * from Session order by timestamp desc")
    @TypeConverters(TypeConverter::class)
    fun get(): LiveData<List<Session>>

    @Query("select * from Session where name = :name order by timestamp desc")
    @TypeConverters(TypeConverter::class)
    fun get(name: String?): LiveData<List<Session>>

    @Query("select * from Session where id = :id")
    @TypeConverters(TypeConverter::class)
    fun get(id: Long): LiveData<Session>

    @Query("delete from Session where id = :id")
    fun remove(id: Long)

    @Query("select * from Session where timestamp >= :firstDayOfCurrentWeek")
    fun getSessionsOfCurrentWeek(
            firstDayOfCurrentWeek: Long = TimeUtils.firstDayOfCurrentWeekMillis()): LiveData<List<Session>>

    @Query("select * from Session where timestamp >= :firstDayOfLastWeek and timestamp < :firstDayOfCurrentWeek")
    fun getSessionsOfLastWeek(
            firstDayOfLastWeek: Long = TimeUtils.firstDayOfLastWeekMillis(),
            firstDayOfCurrentWeek: Long = TimeUtils.firstDayOfCurrentWeekMillis()): LiveData<List<Session>>
}
