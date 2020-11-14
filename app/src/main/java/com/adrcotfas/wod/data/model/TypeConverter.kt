package com.adrcotfas.wod.data.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class TypeConverter {

    @TypeConverter
    fun fromInt(data: Int): SessionType {
        for (type in SessionType.values()) {
            if (type.value == data) {
                return type
            }
        }
        //TODO: log error for invalid input
        return SessionType.REST
    }

    @TypeConverter
    fun toInt(type: SessionType): Int {
        return type.value
    }

    companion object {
        fun toString(vararg sessions: SessionSkeleton) : String {
            val gson = Gson()
            return gson.toJson(listOf(*sessions))
        }

        fun toSessionSkeletons(string : String) : ArrayList<SessionSkeleton> {
            val gson = Gson()
            val typeToken = object : TypeToken<ArrayList<SessionSkeleton>>() {}
            return gson.fromJson(string, typeToken.type)
        }
    }

    @TypeConverter
    fun toString(sessions: ArrayList<SessionSkeleton>) : String {
        return Gson().toJson(sessions)
    }

    @TypeConverter
    fun toSessionSkeletons(string : String) : ArrayList<SessionSkeleton> {
        val gson = Gson()
        val typeToken = object : TypeToken<ArrayList<SessionSkeleton>>() {}
        return gson.fromJson(string, typeToken.type)
    }
}