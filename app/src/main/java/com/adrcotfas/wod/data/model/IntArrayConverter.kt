package com.adrcotfas.wod.data.model

import androidx.room.TypeConverter

class IntArrayConverter {

    @TypeConverter
    fun toList(strings: String): List<Int> {
        val list = mutableListOf<Int>()
        if (strings == "") {
            return list
        }

        val array = strings.split(",")
        for (s in array) {
            list.add(s.toInt())
        }
        return list
    }

    @TypeConverter
    fun toString(strings: List<Int>): String {
        var result = ""
        strings.forEachIndexed { index, element ->
            result += element
            if (index != (strings.size - 1)) {
                result += ","
            }
        }
        return result
    }
}