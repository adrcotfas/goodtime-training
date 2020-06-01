package com.adrcotfas.wod.data.model

data class SessionMinimal (
    var duration: Int,
    var breakDuration: Int,
    var numRounds: Int,
    var type: SessionType)