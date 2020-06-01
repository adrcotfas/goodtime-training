package com.adrcotfas.wod.data.model

import androidx.room.TypeConverter

class SessionTypeConverter {

    @TypeConverter
    fun getExerciseTypeFromInt(data: Int): SessionType {
        for (type in SessionType.values()) {
            if (type.value == data) {
                return type
            }
        }
        return SessionType.INVALID
    }

    @TypeConverter
    fun getIntFromExerciseType(type: SessionType): Int {
        return type.value
    }
}