package goodtime.training.wod.timer.ui.stats

import goodtime.training.wod.timer.data.model.WeeklyGoal

data class WeeklyGoalData(
        var goal: WeeklyGoal,
        var minutesLastWeek: Long,
        var minutesThisWeek: Long)