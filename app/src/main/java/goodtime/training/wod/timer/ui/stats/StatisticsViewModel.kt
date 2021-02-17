package goodtime.training.wod.timer.ui.stats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import goodtime.training.wod.timer.common.StringUtils
import goodtime.training.wod.timer.data.model.Session
import goodtime.training.wod.timer.data.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAdjusters
import java.util.ArrayList

class StatisticsViewModel(private val repo: AppRepository) : ViewModel() {

    val filteredWorkoutName = MutableLiveData<String?>(null)

    fun deleteCompletedWorkouts(selectedItems: ArrayList<Long>) {
        for (i in selectedItems) {
            viewModelScope.launch(Dispatchers.IO) {
                repo.removeSession(i)
            }
        }
    }

    fun getSessions(): LiveData<List<Session>> = repo.getSessions()
    fun getCustomSessions(name: String?) = repo.getCustomSessions(name)

    fun calculateOverviewStats(sessions: List<Session>): Stats {
        val today = LocalDate.now()
        val thisWeekStart: LocalDate = today.with(TemporalAdjusters.previousOrSame(StringUtils.firstDayOfWeek()))
        val thisWeekEnd: LocalDate = today.with(TemporalAdjusters.nextOrSame(StringUtils.lastDayOfWeek()))
        val thisMonthStart: LocalDate = today.with(TemporalAdjusters.firstDayOfMonth())
        val thisMonthEnd: LocalDate = today.with(TemporalAdjusters.lastDayOfMonth())

        val stats = Stats(0, 0, 0, 0)

        for (s in sessions) {
            val crt = LocalDateTime.ofInstant(Instant.ofEpochMilli(s.timestamp), ZoneId.systemDefault()).toLocalDate()

            if (crt.isEqual(today)) {
                stats.today += s.actualDuration
            }
            if ((crt >= thisWeekStart) && (crt <= thisWeekEnd)) {
                stats.week += s.actualDuration
            }
            if ((crt >= thisMonthStart) && (crt <= thisMonthEnd)) {
                stats.month += s.actualDuration
            }
            stats.total += s.actualDuration
        }
        return stats
    }

    fun getThisWeekNumber() = LocalDate.now().with(TemporalAdjusters.previousOrSame(StringUtils.firstDayOfWeek())).get(ChronoField.ALIGNED_WEEK_OF_YEAR)
    fun getCurrentMonthString(): String = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM"))

    fun findPersonalRecord(sessions: List<Session>): Long {
        var id = -1L
        if (sessions.isNotEmpty()) {
            if (sessions[0].isTimeBased) {
                id = sessions.minWithOrNull { o1, o2 ->
                    when {
                        o1.actualDuration > o2.actualDuration -> 1
                        o1.actualDuration == o2.actualDuration -> 0
                        else -> -1
                    }
                }?.id ?: -1L
            } else {
                if (sessions.find { it.actualRounds > 0 || it.actualReps > 0 } != null) {
                    id = sessions.maxWithOrNull { o1, o2 ->
                        when {
                            (o1.actualRounds > o2.actualRounds) ||
                                    ((o1.actualRounds == o2.actualRounds) &&
                                            (o1.actualReps > o2.actualReps)) -> 1
                            ((o1.actualRounds == o2.actualRounds) && ((o1.actualReps == o2.actualReps))) -> 0
                            else -> -1
                        }
                    }?.id ?: -1L
                }
            }
        }
        return id
    }

    class Stats(var today: Long, var week: Long, var month: Long, var total: Long)
}