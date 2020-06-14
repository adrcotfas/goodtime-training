package com.adrcotfas.wod.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity
@TypeConverters(SessionTypeConverter::class)
data class SessionMinimal (
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0,
    var duration: Int,
    var breakDuration: Int,
    var numRounds: Int = 0,
    var type: SessionType,
    var name: String = "")
