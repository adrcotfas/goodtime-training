package com.adrcotfas.wod.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters


@Entity
data class Session(
    @PrimaryKey(autoGenerate = true)
    val uid: Int = 0,
    var duration: Int = 0,
    var breakDuration: Int = 0, // for TABATA
    var numRounds: Int = 0,     // for TABATA
    @TypeConverters(IntArrayConverter::class)
    var rounds: List<Int> = ArrayList(),   // round timestamp for AMRAP and FOR_TIME
    @TypeConverters(SessionTypeConverter::class)
    var type: SessionType = SessionType.INVALID,
    var notes: String = "") {

    enum class SessionType(val value: Int) {
        INVALID(-1),
        AMRAP(0),
        EMOM(1),
        FOR_TIME(2),
        TABATA(3),
        BREAK(4)
    }
}
