package com.adrcotfas.wod.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class Session(
    @PrimaryKey val uid: Int,
    val duration: Int,
    val breakDuration: Int,
    val numRounds: Int,
    val isCountDown: Boolean,
    val rounds: IntArray,
    val type: SessionType,
    val notes: String) {

    enum class SessionType {
        AMRAP,
        EMOM,
        FOR_TIME,
        TABATA,
        BREAK
    }
}
