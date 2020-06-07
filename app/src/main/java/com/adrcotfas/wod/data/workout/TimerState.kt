package com.adrcotfas.wod.data.workout

enum class TimerState(
    val index : Int
) {
    INACTIVE(0),
    ACTIVE(1),
    PAUSED(2),
    FINISHED(3)
}