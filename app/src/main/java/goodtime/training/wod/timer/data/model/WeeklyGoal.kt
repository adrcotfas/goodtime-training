package goodtime.training.wod.timer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WeeklyGoal(
        var minutes: Int,
        var lastUpdateMillis: Long,
        var currentStreak: Int,
        var bestStreak: Int
) {
    @PrimaryKey
    // only store one row in this table and modify it
    var id: Int = ID
    companion object {
        const val ID = 42
    }
}