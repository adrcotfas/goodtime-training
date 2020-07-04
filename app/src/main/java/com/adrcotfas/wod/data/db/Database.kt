package com.adrcotfas.wod.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.adrcotfas.wod.data.model.Session
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.model.SessionTypeConverter

@Database(entities = [Session::class, SessionMinimal::class], version = 1, exportSchema = false)
@TypeConverters(SessionTypeConverter::class)
abstract class Database: RoomDatabase() {
    abstract fun sessionsDao() : SessionDao
    abstract fun sessionMinimalDao() : SessionMinimalDao
}
