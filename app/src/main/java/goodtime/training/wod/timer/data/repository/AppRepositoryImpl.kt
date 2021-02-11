package goodtime.training.wod.timer.data.repository

import androidx.lifecycle.LiveData
import goodtime.training.wod.timer.data.db.CustomWorkoutSkeletonDao
import goodtime.training.wod.timer.data.db.SessionDao
import goodtime.training.wod.timer.data.db.SessionSkeletonDao
import goodtime.training.wod.timer.data.db.WeeklyGoalDao
import goodtime.training.wod.timer.data.model.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AppRepositoryImpl(
    private val sessionDao: SessionDao,
    private val sessionSkeletonDao: SessionSkeletonDao,
    private val customWorkoutSkeletonDao: CustomWorkoutSkeletonDao,
    private val weeklyGoalDao: WeeklyGoalDao)
    : AppRepository {
    override fun addSession(session: Session) {
        GlobalScope.launch {
            sessionDao.add(session)
        }
    }

    override fun getSessions() : LiveData<List<Session>> = sessionDao.get()

    override fun getSession(id: Long) = sessionDao.get(id)

    override fun editSession(session: Session) {
        GlobalScope.launch {
            sessionDao.edit(session)
        }
    }

    override fun removeSession(id: Long) {
        GlobalScope.launch {
            sessionDao.remove(id)
        }
    }

    override fun getCustomSessions(name: String?) = sessionDao.get(name)

    override fun addSessionSkeleton(session: SessionSkeleton) {
        GlobalScope.launch {
            sessionSkeletonDao.add(session)
        }
    }

    override fun getSessionSkeletons(type: SessionType): LiveData<List<SessionSkeleton>>
        = sessionSkeletonDao.get(type)

    override fun removeSessionSkeleton(id: Long) {
      GlobalScope.launch {
          sessionSkeletonDao.remove(id)
      }
    }

    override fun addCustomWorkoutSkeleton(workout: CustomWorkoutSkeleton) {
        GlobalScope.launch {
            customWorkoutSkeletonDao.add(workout)
        }
    }

    override fun getCustomWorkoutSkeletons() = customWorkoutSkeletonDao.get()
    override fun getCustomWorkoutSkeleton(name: String) = customWorkoutSkeletonDao.get(name)

    override fun editCustomWorkoutSkeleton(workout: CustomWorkoutSkeleton) {
        GlobalScope.launch {
            customWorkoutSkeletonDao.add(workout)
        }
    }

    override fun removeCustomWorkoutSkeleton(name: String) {
        GlobalScope.launch {
            customWorkoutSkeletonDao.remove(name)
        }
    }

    override fun addWeeklyGoal(goal: WeeklyGoal) {
        GlobalScope.launch {
            weeklyGoalDao.add(goal)
        }
    }

    override fun updateWeeklyGoal(goal: WeeklyGoal) {
        GlobalScope.launch {
            weeklyGoalDao.update(goal)
        }
    }

    override fun getWeeklyGoal(): LiveData<WeeklyGoal> = weeklyGoalDao.get()

    override fun getSessionsOfCurrentWeek() = sessionDao.getSessionsOfCurrentWeek()
    override fun getSessionsOfLastWeek() = sessionDao.getSessionsOfLastWeek()
}
