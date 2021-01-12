package goodtime.training.wod.timer.data.repository

import androidx.lifecycle.LiveData
import goodtime.training.wod.timer.data.db.CustomWorkoutSkeletonDao
import goodtime.training.wod.timer.data.db.SessionDao
import goodtime.training.wod.timer.data.db.SessionSkeletonDao
import goodtime.training.wod.timer.data.model.CustomWorkoutSkeleton
import goodtime.training.wod.timer.data.model.Session
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AppRepositoryImpl(
    private val sessionDao: SessionDao,
    private val sessionSkeletonDao: SessionSkeletonDao,
    private val customWorkoutSkeletonDao: CustomWorkoutSkeletonDao)
    : AppRepository {
    override fun addSession(session: Session) {
        GlobalScope.launch {
            sessionDao.add(session)
        }
    }

    override fun getSessions() : LiveData<List<Session>> = sessionDao.get()
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

    override fun editCustomWorkoutSkeleton(name: String, workout: CustomWorkoutSkeleton) {
        GlobalScope.launch {
            customWorkoutSkeletonDao.edit(name, workout.name, workout.sessions)
        }
    }

    override fun removeCustomWorkoutSkeleton(name: String) {
        GlobalScope.launch {
            customWorkoutSkeletonDao.remove(name)
        }
    }
}
