package goodtime.training.wod.timer.data.repository

import androidx.lifecycle.LiveData
import goodtime.training.wod.timer.data.db.CustomWorkoutSkeletonDao
import goodtime.training.wod.timer.data.db.SessionDao
import goodtime.training.wod.timer.data.db.SessionSkeletonDao
import goodtime.training.wod.timer.data.db.WeeklyGoalDao
import goodtime.training.wod.timer.data.model.*

class AppRepositoryImpl(
    private val sessionDao: SessionDao,
    private val sessionSkeletonDao: SessionSkeletonDao,
    private val customWorkoutSkeletonDao: CustomWorkoutSkeletonDao,
    private val weeklyGoalDao: WeeklyGoalDao
) : AppRepository {

    override suspend fun addSession(session: Session) {
        sessionDao.add(session)
    }

    override fun getSessions(): LiveData<List<Session>> = sessionDao.get()

    override fun getSession(id: Long) = sessionDao.get(id)

    override suspend fun editSession(session: Session) {
        sessionDao.edit(session)
    }

    override suspend fun removeSession(id: Long) {
        sessionDao.remove(id)
    }

    override fun getCustomSessions(name: String?) = sessionDao.get(name)

    override suspend fun addSessionSkeleton(session: SessionSkeleton) {
        sessionSkeletonDao.add(session)
    }

    override fun getSessionSkeletons(type: SessionType): LiveData<List<SessionSkeleton>> =
        sessionSkeletonDao.get(type)

    override suspend fun removeSessionSkeleton(id: Long) {
        sessionSkeletonDao.remove(id)
    }

    override suspend fun addCustomWorkoutSkeleton(workout: CustomWorkoutSkeleton) {
        customWorkoutSkeletonDao.add(workout)
    }

    override fun getCustomWorkoutSkeletons() = customWorkoutSkeletonDao.get()
    override fun getCustomWorkoutSkeleton(name: String) = customWorkoutSkeletonDao.get(name)

    override suspend fun editCustomWorkoutSkeleton(workout: CustomWorkoutSkeleton) {
        customWorkoutSkeletonDao.add(workout)
    }

    override suspend fun removeCustomWorkoutSkeleton(name: String) {
        customWorkoutSkeletonDao.remove(name)
    }

    override suspend fun addWeeklyGoal(goal: WeeklyGoal) {
        weeklyGoalDao.add(goal)
    }

    override suspend fun updateWeeklyGoal(goal: WeeklyGoal) {
        weeklyGoalDao.update(goal)
    }

    override fun getWeeklyGoal(): LiveData<WeeklyGoal> = weeklyGoalDao.get()

    override fun getSessionsOfCurrentWeek() = sessionDao.getSessionsOfCurrentWeek()
    override fun getSessionsOfLastWeek() = sessionDao.getSessionsOfLastWeek()
}
