package com.adrcotfas.wod.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity
@TypeConverters(TypeConverter::class)
data class Session(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var duration: Int = 0,
    var breakDuration: Int = 0,
    var numRounds: Int = 0,
    var type: SessionType = SessionType.REST,

    var rounds: Int,
    var timestamp: Long = System.currentTimeMillis(),
    var finished: Boolean) {

    companion object {
        fun constructSession(skeleton: SessionSkeleton, timestamp: Long, rounds: ArrayList<Int> = arrayListOf(0), duration : Int = 0) : Session {
            //TODO: use [duration] for FOR_TIME (or maybe for all)
            return Session(0, skeleton.duration, skeleton.breakDuration, skeleton.numRounds, skeleton.type,
                //TODO: but is "finished" important?
                0, timestamp, true)
        }

        fun constructIncompleteSession(
            type : SessionType,
            activeSeconds: Int,
            timestamp: Long,
            rounds: Int = 0) : Session {

            return Session(0, activeSeconds, 0, 0, type, rounds,
                timestamp, false)
        }
    }

}
