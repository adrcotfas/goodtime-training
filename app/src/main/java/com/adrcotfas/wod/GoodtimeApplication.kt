package com.adrcotfas.wod

import android.app.Application
import android.app.PendingIntent
import android.content.Context
import androidx.navigation.NavDeepLinkBuilder
import androidx.room.Room
import com.adrcotfas.wod.common.preferences.PrefUtil
import com.adrcotfas.wod.data.db.Database
import com.adrcotfas.wod.ui.workout.WorkoutManager
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.bind
import org.kodein.di.generic.eagerSingleton
import org.kodein.di.generic.singleton

class GoodtimeApplication : Application(), KodeinAware {
    override val kodein = Kodein.lazy {
        bind<Database>() with eagerSingleton {
            Room.databaseBuilder(this@GoodtimeApplication, Database::class.java,
                "goodtime-training-db")
                .build() }
        bind<PrefUtil>() with singleton { PrefUtil(applicationContext) }
        bind<WorkoutManager>() with singleton {WorkoutManager() }
    }

    companion object {
        fun getNavigationIntent(context: Context, destId: Int): PendingIntent {
            return NavDeepLinkBuilder(context)
                .setGraph(R.navigation.mobile_navigation)
                .setDestination(destId)
                .createPendingIntent()
        }
    }
}
