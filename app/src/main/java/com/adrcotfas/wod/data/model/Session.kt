package com.adrcotfas.wod.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity
data class Session(
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0,
    var duration: Int = 0,
    var breakDuration: Int = 0, // for TABATA
    var numRounds: Int = 0,     // for TABATA
    @TypeConverters(IntArrayConverter::class)
    var rounds: List<Int> = ArrayList(),   // round timestamp for AMRAP and FOR_TIME
    @TypeConverters(SessionTypeConverter::class)
    var type: SessionType = SessionType.INVALID,
    var timestamp: Long = System.currentTimeMillis(),
    var notes: String = "") {

    companion object {
        fun constructSession(minimal: SessionMinimal, timestamp: Long, rounds: List<Int> = ArrayList(), notes: String = "") : Session {
            return Session(0, minimal.duration, minimal.breakDuration, minimal.numRounds, rounds, minimal.type, timestamp, notes)
        }
    }

}
