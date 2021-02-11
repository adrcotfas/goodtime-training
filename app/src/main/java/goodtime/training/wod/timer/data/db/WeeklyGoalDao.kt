package goodtime.training.wod.timer.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import goodtime.training.wod.timer.data.model.WeeklyGoal

@Dao
interface WeeklyGoalDao {

    @Insert
    suspend fun add(weeklyGoal: WeeklyGoal)

    @Update
    suspend fun update(weeklyGoal: WeeklyGoal)

    @Query("select * from WeeklyGoal where id = :id")
    fun get(id: Int = WeeklyGoal.ID): LiveData<WeeklyGoal>
}