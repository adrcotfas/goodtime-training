package com.adrcotfas.wod.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.adrcotfas.wod.data.model.IntArrayConverter
import com.adrcotfas.wod.data.model.Session
import com.adrcotfas.wod.data.model.SessionTypeConverter

@Database(entities = [Session::class], version = 1, exportSchema = false)
@TypeConverters(IntArrayConverter::class, SessionTypeConverter::class)
abstract class Database: RoomDatabase() {
    abstract fun sessionsDao() : SessionDao
}
