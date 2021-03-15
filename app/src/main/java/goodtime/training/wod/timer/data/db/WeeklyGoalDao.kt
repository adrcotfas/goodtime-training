package goodtime.training.wod.timer.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import goodtime.training.wod.timer.data.model.WeeklyGoal

@Dao
interface WeeklyGoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(weeklyGoal: WeeklyGoal)

    @Query("select * from WeeklyGoal where id = :id")
    fun get(id: Int = WeeklyGoal.ID): LiveData<WeeklyGoal>
}