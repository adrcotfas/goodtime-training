package com.adrcotfas.wod

import android.app.Application
import android.app.PendingIntent
import android.content.Context
import androidx.navigation.NavDeepLinkBuilder
import androidx.room.Room
import com.adrcotfas.wod.common.preferences.PrefUtil
import com.adrcotfas.wod.common.soundplayer.SoundPlayer
import com.adrcotfas.wod.data.db.Database
import com.adrcotfas.wod.data.db.SessionDao
import com.adrcotfas.wod.data.db.SessionMinimalDao
import com.adrcotfas.wod.data.repository.SessionRepositoryImpl
import com.adrcotfas.wod.data.repository.SessionsRepository
import com.adrcotfas.wod.ui.log.LogViewModelFactory
import com.adrcotfas.wod.ui.workout.WorkoutManager
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.*

class GoodtimeApplication : Application(), KodeinAware {
    override val kodein = Kodein.lazy {
        bind<Database>() with eagerSingleton {
            Room.databaseBuilder(this@GoodtimeApplication, Database::class.java,
                "goodtime-training-db")
                .build() }
        bind<SessionDao>() with singleton { instance<Database>().sessionsDao() }
        bind<SessionMinimalDao>() with singleton { instance<Database>().sessionMinimalDao() }
        bind<SessionsRepository>() with singleton { SessionRepositoryImpl(instance(), instance()) }
        bind<PrefUtil>() with singleton { PrefUtil(applicationContext) }
        bind<SoundPlayer>() with singleton { SoundPlayer(applicationContext) }
        bind<WorkoutManager>() with singleton {WorkoutManager(instance(), instance()) }
        bind<LogViewModelFactory>() with provider { LogViewModelFactory(instance()) }
    }

    override fun onCreate() {
        super.onCreate()
        // generate default sessionMinimal
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
