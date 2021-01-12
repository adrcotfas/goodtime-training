package goodtime.training.wod.timer.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.TypeConverters
import goodtime.training.wod.timer.data.model.Session
import goodtime.training.wod.timer.data.model.TypeConverter

@Dao
interface SessionDao {

    @Insert
    @TypeConverters(TypeConverter::class)
    suspend fun add(session: Session)

    @Query("select * from Session")
    @TypeConverters(TypeConverter::class)
    fun get(): LiveData<List<Session>>

    @Query("select * from Session where name = :name")
    @TypeConverters(TypeConverter::class)
    fun get(name: String?): LiveData<List<Session>>
}
