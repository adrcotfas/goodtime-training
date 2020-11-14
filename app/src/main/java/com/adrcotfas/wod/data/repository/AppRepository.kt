package com.adrcotfas.wod.data.repository

import androidx.lifecycle.LiveData
import com.adrcotfas.wod.data.model.CustomWorkoutSkeleton
import com.adrcotfas.wod.data.model.Session
import com.adrcotfas.wod.data.model.SessionSkeleton
import com.adrcotfas.wod.data.model.SessionType

interface AppRepository {

    // finished sessions
    fun addSession(session: Session)
    fun getSessions(): LiveData<List<Session>>

    fun addSessionSkeleton(session: SessionSkeleton)
    fun getSessionSkeletons(type: SessionType): LiveData<List<SessionSkeleton>>
    fun removeSessionSkeleton(id: Int)

    fun addCustomWorkoutSkeleton(workout: CustomWorkoutSkeleton)
    fun getCustomWorkoutSkeletons() : LiveData<List<CustomWorkoutSkeleton>>
    fun editCustomWorkoutSkeleton(name: String, workout: CustomWorkoutSkeleton)
    fun removeCustomWorkoutSkeleton(name: String)
}
