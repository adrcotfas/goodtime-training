package com.adrcotfas.wod.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity
@TypeConverters(SessionTypeConverter::class)
data class Session(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var duration: Int = 0,
    var breakDuration: Int = 0,
    var numRounds: Int = 0,
    var type: SessionType = SessionType.BREAK,

    var rounds: Int,
    var timestamp: Long = System.currentTimeMillis(),
    var finished: Boolean) {

    companion object {
        fun constructSession(minimal: SessionMinimal, timestamp: Long, rounds: Int = 0) : Session {
            return Session(0, minimal.duration, minimal.breakDuration, minimal.numRounds, minimal.type,
                //TODO: but is "finished" important?
                rounds, timestamp, true)
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
