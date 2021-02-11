package goodtime.training.wod.timer.data.repository

import androidx.lifecycle.LiveData
import goodtime.training.wod.timer.data.model.*

interface AppRepository {

    // finished sessions
    fun addSession(session: Session)
    fun getSessions(): LiveData<List<Session>>
    fun getSession(id: Long): LiveData<Session>
    fun editSession(session: Session)
    fun removeSession(id: Long)
    fun getCustomSessions(name: String?): LiveData<List<Session>>

    fun addSessionSkeleton(session: SessionSkeleton)
    fun getSessionSkeletons(type: SessionType): LiveData<List<SessionSkeleton>>
    fun removeSessionSkeleton(id: Long)

    fun addCustomWorkoutSkeleton(workout: CustomWorkoutSkeleton)
    fun getCustomWorkoutSkeletons() : LiveData<List<CustomWorkoutSkeleton>>
    fun getCustomWorkoutSkeleton(name: String): LiveData<CustomWorkoutSkeleton>
    fun editCustomWorkoutSkeleton(workout: CustomWorkoutSkeleton)
    fun removeCustomWorkoutSkeleton(name: String)

    fun addWeeklyGoal(goal: WeeklyGoal)
    fun updateWeeklyGoal(goal: WeeklyGoal)
    fun getWeeklyGoal() : LiveData<WeeklyGoal>
    fun getSessionsOfCurrentWeek(): LiveData<List<Session>>
    fun getSessionsOfLastWeek(): LiveData<List<Session>>
}
