package goodtime.training.wod.timer.data.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.collections.ArrayList

class TypeConverter {

    @TypeConverter
    fun fromInt(data: Int): SessionType {
        for (type in SessionType.values()) {
            if (type.value == data) {
                return type
            }
        }
        return SessionType.REST
    }

    @TypeConverter
    fun toInt(type: SessionType): Int {
        return type.value
    }

    @TypeConverter
    fun toString(sessions: ArrayList<SessionSkeleton>): String {
        return Gson().toJson(sessions)
    }

    @TypeConverter
    fun toSessionSkeletons(string: String): ArrayList<SessionSkeleton> {
        val gson = Gson()
        val typeToken = object : TypeToken<ArrayList<SessionSkeleton>>() {}
        return gson.fromJson(string, typeToken.type)
    }

    @TypeConverter
    fun toString(session: SessionSkeleton): String {
        val gson = Gson()
        return gson.toJson(session)
    }

    fun toString(vararg session: SessionSkeleton): String {
        val gson = Gson()
        return gson.toJson(session)
    }

    @TypeConverter
    fun toSessionSkeleton(data: String): SessionSkeleton {
        val gson = Gson()
        val typeToken = object : TypeToken<SessionSkeleton>() {}
        return gson.fromJson(data, typeToken.type)
    }

    @TypeConverter
    fun intToString(ints: ArrayList<Int>): String {
        return Gson().toJson(ints)
    }

    @TypeConverter
    fun stringToInts(string: String): ArrayList<Int> {
        val gson = Gson()
        val typeToken = object : TypeToken<ArrayList<Int>>() {}
        return gson.fromJson(string, typeToken.type)
    }

    //TODO: remove this duplication
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
}