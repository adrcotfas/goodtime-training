package goodtime.training.wod.timer.data.repository

import androidx.lifecycle.LiveData
import goodtime.training.wod.timer.data.model.*

interface AppRepository {

    // finished sessions
    suspend fun addSession(session: Session)
    fun getSessions(): LiveData<List<Session>>
    fun getSession(id: Long): LiveData<Session>
    suspend fun editSession(session: Session)
    suspend fun removeSession(id: Long)
    fun getCustomSessions(name: String?): LiveData<List<Session>>

    suspend fun addSessionSkeleton(session: SessionSkeleton)
    fun getSessionSkeletons(type: SessionType): LiveData<List<SessionSkeleton>>
    suspend fun removeSessionSkeleton(id: Long)

    suspend fun addCustomWorkoutSkeleton(workout: CustomWorkoutSkeleton)
    fun getCustomWorkoutSkeletons() : LiveData<List<CustomWorkoutSkeleton>>
    fun getCustomWorkoutSkeleton(name: String): LiveData<CustomWorkoutSkeleton>
    suspend fun editCustomWorkoutSkeleton(workout: CustomWorkoutSkeleton)
    suspend fun removeCustomWorkoutSkeleton(name: String)

    suspend fun addWeeklyGoal(goal: WeeklyGoal)
    suspend fun updateWeeklyGoal(goal: WeeklyGoal)
    fun getWeeklyGoal() : LiveData<WeeklyGoal>
    fun getSessionsOfCurrentWeek(): LiveData<List<Session>>
    fun getSessionsOfLastWeek(): LiveData<List<Session>>
}
