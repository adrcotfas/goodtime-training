package com.adrcotfas.wod.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.adrcotfas.wod.data.model.CustomWorkoutSkeleton
import com.adrcotfas.wod.data.model.Session
import com.adrcotfas.wod.data.model.SessionSkeleton
import com.adrcotfas.wod.data.model.TypeConverter

@Database(entities = [Session::class, SessionSkeleton::class, CustomWorkoutSkeleton::class],
    version = 1, exportSchema = false)
@TypeConverters(TypeConverter::class)
abstract class Database: RoomDatabase() {
    abstract fun sessionsDao(): SessionDao
    abstract fun sessionSkeletonDao(): SessionSkeletonDao
    abstract fun customWorkoutSkeletonDao(): CustomWorkoutSkeletonDao
}
