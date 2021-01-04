package goodtime.training.wod.timer.ui.stats

import androidx.lifecycle.ViewModel
import goodtime.training.wod.timer.common.StringUtils
import goodtime.training.wod.timer.data.model.Session
import goodtime.training.wod.timer.data.repository.AppRepository
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.time.temporal.TemporalAdjusters

class StatisticsViewModel(private val appRepository: AppRepository) : ViewModel() {

    fun addSession(session: Session) = appRepository.addSession(session)
    fun getSessions() = appRepository.getSessions()

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

    class Stats(var today: Long, var week: Long, var month: Long, var total: Long)
}