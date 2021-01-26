package goodtime.training.wod.timer.data.repository

import androidx.lifecycle.LiveData
import goodtime.training.wod.timer.data.model.CustomWorkoutSkeleton
import goodtime.training.wod.timer.data.model.Session
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType

interface AppRepository {

    // finished sessions
    fun addSession(session: Session)
    fun getSessions(): LiveData<List<Session>>
    fun editSession(session: Session)
    fun removeSession(id: Long)
    fun getCustomSessions(name: String?): LiveData<List<Session>>

    fun addSessionSkeleton(session: SessionSkeleton)
    fun getSessionSkeletons(type: SessionType): LiveData<List<SessionSkeleton>>
    fun removeSessionSkeleton(id: Long)

    fun addCustomWorkoutSkeleton(workout: CustomWorkoutSkeleton)
    fun getCustomWorkoutSkeletons() : LiveData<List<CustomWorkoutSkeleton>>
    fun editCustomWorkoutSkeleton(workout: CustomWorkoutSkeleton)
    fun removeCustomWorkoutSkeleton(name: String)
}
