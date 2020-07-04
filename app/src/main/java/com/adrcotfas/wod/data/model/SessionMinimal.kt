package com.adrcotfas.wod.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity
@TypeConverters(SessionTypeConverter::class)
data class SessionMinimal (
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var duration: Int,
    var breakDuration: Int = 0,
    var numRounds: Int = 0,
    var type: SessionType
) {
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        other as SessionMinimal
        return (this.duration == other.duration &&
                this.breakDuration == other.breakDuration &&
                this.numRounds == other.numRounds &&
                this.type == other.type)
    }
}
