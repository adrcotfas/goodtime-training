package goodtime.training.wod.timer.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import goodtime.training.wod.timer.common.CombinedLiveData
import goodtime.training.wod.timer.common.TimeUtils
import goodtime.training.wod.timer.data.model.Session
import goodtime.training.wod.timer.data.model.WeeklyGoal
import goodtime.training.wod.timer.data.repository.AppRepository
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class WeeklyGoalViewModel(private val repo: AppRepository) : ViewModel() {

    fun getWeeklyGoalData(): CombinedLiveData<WeeklyGoalData> {
        return CombinedLiveData(repo.getWeeklyGoal(), repo.getSessionsOfLastWeek(), repo.getSessionsOfCurrentWeek()) {
            data: List<Any?> ->
            if (data[0] == null || data[1] == null || data[2] == null){
                WeeklyGoalData(WeeklyGoal(0, 0,0, 0), 0, 0)
            } else {
                val sessionsLastWeek = data[1] as List<Session>
                var secondsLastWeek = 0
                sessionsLastWeek.forEach { secondsLastWeek += it.actualDuration }

                val sessionsThisWeek = data[2] as List<Session>
                var secondsThisWeek = 0
                sessionsThisWeek.forEach { secondsThisWeek += it.actualDuration }

                WeeklyGoalData(
                        data[0] as WeeklyGoal,
                        TimeUnit.SECONDS.toMinutes(secondsLastWeek.toLong()),
                        TimeUnit.SECONDS.toMinutes(secondsThisWeek.toLong()))
            }
        }
    }

    fun updateStreaks(data: WeeklyGoalData) {
        val firstDayOfCurrentWeekMillis = TimeUtils.firstDayOfCurrentWeekMillis()
        // the trigger for a streak update is the start of a new week
        // in which the last week's progress has not been updated yet
        if (data.goal.lastUpdateMillis < firstDayOfCurrentWeekMillis) {
            data.goal.lastUpdateMillis = firstDayOfCurrentWeekMillis
            // goal was achieved
            if (data.minutesLastWeek >= data.goal.minutes) {
                data.goal.currentStreak++
                if (data.goal.currentStreak > data.goal.bestStreak) {
                    data.goal.bestStreak = data.goal.currentStreak
                }
            } else { //goal failed - reset streak
                data.goal.currentStreak = 0
            }
            viewModelScope.launch {
                repo.updateWeeklyGoal(data.goal)
            }
        }
    }
}