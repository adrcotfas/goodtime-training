package com.adrcotfas.wod.data.model

import androidx.room.TypeConverter

class SessionTypeConverter {

    @TypeConverter
    fun getExerciseTypeFromInt(data: Int): Session.SessionType {
        for (type in Session.SessionType.values()) {
            if (type.value == data) {
                return type
            }
        }
        return Session.SessionType.INVALID
    }

    @TypeConverter
    fun getIntFromExerciseType(type: Session.SessionType): Int {
        return type.value
    }
}