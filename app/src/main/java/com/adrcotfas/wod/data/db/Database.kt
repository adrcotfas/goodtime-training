package com.adrcotfas.wod.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.adrcotfas.wod.data.model.Session

@Database(entities = [Session::class], version = 1)
abstract class Database: RoomDatabase() {
    abstract fun sessionsDao() : SessionDao
}
