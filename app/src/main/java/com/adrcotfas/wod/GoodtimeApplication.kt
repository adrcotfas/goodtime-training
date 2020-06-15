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
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.model.SessionType
import com.adrcotfas.wod.data.repository.SessionRepositoryImpl
import com.adrcotfas.wod.data.repository.SessionsRepository
import com.adrcotfas.wod.ui.common.ViewModelFactory
import com.adrcotfas.wod.ui.log.LogViewModelFactory
import com.adrcotfas.wod.ui.workout.WorkoutManager
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.*
import java.util.concurrent.TimeUnit

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
        bind() from provider { ViewModelFactory(instance()) }
    }

    override fun onCreate() {
        super.onCreate()
        val prefUtil: PrefUtil by instance()
        if (prefUtil.isFirstRun()) {
            generateDefaultSessions()
            prefUtil.setIsFirstRun(false)
        }
    }

    private fun generateDefaultSessions() {
        val repo: SessionsRepository by instance()
        repo.addSessionMinimal(
            SessionMinimal(0, TimeUnit.MINUTES.toSeconds(10).toInt(), 0, 1,
                SessionType.AMRAP)
        )
        repo.addSessionMinimal(
            SessionMinimal(0, TimeUnit.MINUTES.toSeconds(15).toInt(), 0, 1,
            SessionType.AMRAP, "Default")
        )
        repo.addSessionMinimal(
            SessionMinimal(0, TimeUnit.MINUTES.toSeconds(20).toInt(), 0, 1,
                SessionType.AMRAP, "Test a long name")
        )

        repo.addSessionMinimal(SessionMinimal(0, TimeUnit.MINUTES.toSeconds(15).toInt(), 0, 1,
            SessionType.FOR_TIME, "Default"))
        repo.addSessionMinimal(SessionMinimal(0, TimeUnit.MINUTES.toSeconds(1).toInt(), 0, 10,
            SessionType.EMOM, "Default"))
        repo.addSessionMinimal(SessionMinimal(0, 20, 10,8,
            SessionType.TABATA, "Default"))
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
