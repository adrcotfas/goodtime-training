package com.adrcotfas.wod

import android.app.Application
import android.app.PendingIntent
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
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
import com.adrcotfas.wod.ui.amrap.AmrapViewModelFactory
import com.adrcotfas.wod.ui.emom.EmomViewModelFactory
import com.adrcotfas.wod.ui.for_time.ForTimeViewModelFactory
import com.adrcotfas.wod.ui.log.LogViewModelFactory
import com.adrcotfas.wod.ui.tabata.TabataViewModelFactory
import com.adrcotfas.wod.ui.workout.WorkoutViewModel
import com.adrcotfas.wod.ui.workout.WorkoutViewModelFactory
import com.google.android.material.resources.TextAppearanceConfig
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
        bind<LogViewModelFactory>() with provider { LogViewModelFactory(instance()) }
        bind() from provider { AmrapViewModelFactory(instance()) }
        bind() from provider { ForTimeViewModelFactory(instance()) }
        bind() from provider { EmomViewModelFactory(instance()) }
        bind() from provider { TabataViewModelFactory(instance()) }
        bind() from provider { WorkoutViewModelFactory(instance(), instance()) }
    }

    override fun onCreate() {
        super.onCreate()
        TextAppearanceConfig.setShouldLoadFontSynchronously(true)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        val prefUtil: PrefUtil by instance()
        if (prefUtil.isFirstRun()) {
            generateDefaultSessions()
            prefUtil.setIsFirstRun(false)
        }
    }

    private fun generateDefaultSessions() {
        val repo: SessionsRepository by instance()
        repo.addSessionMinimal(
            SessionMinimal(0, TimeUnit.MINUTES.toSeconds(10).toInt(), type = SessionType.AMRAP)
        )
        repo.addSessionMinimal(
            SessionMinimal(0, TimeUnit.MINUTES.toSeconds(15).toInt(), type = SessionType.AMRAP)
        )
        repo.addSessionMinimal(
            SessionMinimal(0, TimeUnit.MINUTES.toSeconds(20).toInt(), type = SessionType.AMRAP)
        )

        repo.addSessionMinimal(SessionMinimal(0, TimeUnit.MINUTES.toSeconds(15).toInt(), type = SessionType.FOR_TIME))
        repo.addSessionMinimal(SessionMinimal(0, TimeUnit.MINUTES.toSeconds(1).toInt(), 0, 20,
            SessionType.EMOM))
        repo.addSessionMinimal(SessionMinimal(0, 20, 10,8,
            SessionType.TABATA))
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
