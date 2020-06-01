package com.adrcotfas.wod.data.model

enum class SessionType(val value: Int) {
    INVALID(-1),
    AMRAP(0),
    EMOM(1),
    FOR_TIME(2),
    TABATA(3),
    BREAK(4)
}