package com.adrcotfas.wod.data.repository

import androidx.lifecycle.LiveData
import com.adrcotfas.wod.data.db.CustomWorkoutSkeletonDao
import com.adrcotfas.wod.data.db.SessionDao
import com.adrcotfas.wod.data.db.SessionSkeletonDao
import com.adrcotfas.wod.data.model.CustomWorkoutSkeleton
import com.adrcotfas.wod.data.model.Session
import com.adrcotfas.wod.data.model.SessionSkeleton
import com.adrcotfas.wod.data.model.SessionType
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

    override fun addSessionSkeleton(session: SessionSkeleton) {
        GlobalScope.launch {
            sessionSkeletonDao.add(session)
        }
    }

    override fun getSessionSkeletons(type: SessionType): LiveData<List<SessionSkeleton>>
        = sessionSkeletonDao.get(type)

    override fun removeSessionSkeleton(id: Int) {
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
