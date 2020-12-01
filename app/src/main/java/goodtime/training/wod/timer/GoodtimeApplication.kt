package goodtime.training.wod.timer

import android.app.Application
import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import androidx.room.Room
import com.google.android.material.resources.TextAppearanceConfig
import goodtime.training.wod.timer.common.preferences.PrefUtil
import goodtime.training.wod.timer.common.soundplayer.SoundPlayer
import goodtime.training.wod.timer.data.db.CustomWorkoutSkeletonDao
import goodtime.training.wod.timer.data.db.Database
import goodtime.training.wod.timer.data.db.SessionDao
import goodtime.training.wod.timer.data.db.SessionSkeletonDao
import goodtime.training.wod.timer.data.model.CustomWorkoutSkeleton
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import goodtime.training.wod.timer.data.repository.AppRepository
import goodtime.training.wod.timer.data.repository.AppRepositoryImpl
import goodtime.training.wod.timer.ui.main.custom.CustomWorkoutViewModelFactory
import goodtime.training.wod.timer.ui.stats.LogViewModelFactory
import goodtime.training.wod.timer.ui.timer.TimerViewModelFactory
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.bind
import org.kodein.di.generic.eagerSingleton
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import java.util.concurrent.TimeUnit

class GoodtimeApplication : Application(), KodeinAware {

    companion object {
        private lateinit var res: Resources
        fun getRes(): Resources {
            return res
        }
    }

    override val kodein = Kodein.lazy {
        bind<Database>() with eagerSingleton {
            Room.databaseBuilder(this@GoodtimeApplication, Database::class.java,
                "goodtime-training-db")
                .build() }
        bind<SessionDao>() with eagerSingleton { instance<Database>().sessionsDao() }
        bind<SessionSkeletonDao>() with eagerSingleton { instance<Database>().sessionSkeletonDao() }
        bind<CustomWorkoutSkeletonDao>() with eagerSingleton { instance<Database>().customWorkoutSkeletonDao() }
        bind<AppRepository>() with eagerSingleton { AppRepositoryImpl(instance(), instance(), instance()) }
        bind<PrefUtil>() with eagerSingleton { PrefUtil(applicationContext) }
        bind<SoundPlayer>() with eagerSingleton { SoundPlayer(applicationContext) }
        bind() from provider { CustomWorkoutViewModelFactory(instance()) }
        bind() from provider { LogViewModelFactory(instance()) }
        bind() from provider { TimerViewModelFactory(instance(), instance()) }
    }

    override fun onCreate() {
        super.onCreate()
        res = resources
        TextAppearanceConfig.setShouldLoadFontSynchronously(true)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        val prefUtil: PrefUtil by instance()
        if (prefUtil.isFirstRun()) {
            generateDefaultSessions()
            prefUtil.setIsFirstRun(false)
        }
    }

    private fun generateDefaultSessions() {
        val repo: AppRepository by instance()
        repo.addSessionSkeleton(
            SessionSkeleton(0, TimeUnit.MINUTES.toSeconds(10).toInt(), type = SessionType.AMRAP)
        )
        repo.addSessionSkeleton(
            SessionSkeleton(0, TimeUnit.MINUTES.toSeconds(15).toInt(), type = SessionType.AMRAP)
        )
        repo.addSessionSkeleton(
            SessionSkeleton(0, TimeUnit.MINUTES.toSeconds(20).toInt(), type = SessionType.AMRAP)
        )

        repo.addSessionSkeleton(SessionSkeleton(0, TimeUnit.MINUTES.toSeconds(15).toInt(), type = SessionType.FOR_TIME))
        repo.addSessionSkeleton(SessionSkeleton(0, TimeUnit.MINUTES.toSeconds(1).toInt(), 0, 20,
            SessionType.EMOM))

        val tabata = SessionSkeleton(
            0, 20, 10, 8,
            SessionType.HIIT
        )
        val rest30Sec = SessionSkeleton(0, 30, 30, 0, SessionType.REST)
        val rest1Min = SessionSkeleton(0, TimeUnit.MINUTES.toSeconds(1).toInt(), TimeUnit.MINUTES.toSeconds(1).toInt(), 0, SessionType.REST)

        //TODO: remove before release
        val dummyAmrap = SessionSkeleton(0, 5, type = SessionType.AMRAP)
        val dummyForTime = SessionSkeleton(0, 5, type = SessionType.FOR_TIME)
        val dummyHiit = SessionSkeleton(
            0, 3, 3, 3,
            SessionType.TABATA
        )
        val dummyEmom = SessionSkeleton(
            0, 3, 0, 2,
            SessionType.EMOM
        )
        val dummyRest = SessionSkeleton(0, 30, 5, 0, SessionType.REST)
        repo.addCustomWorkoutSkeleton(CustomWorkoutSkeleton("Dummy",
            arrayListOf(
                dummyAmrap,
                dummyForTime,
                dummyHiit,
                dummyRest,
                dummyEmom,
                dummyRest,
                dummyAmrap)))

        repo.addSessionSkeleton(rest30Sec)
        repo.addSessionSkeleton(rest1Min)

        //Custom workouts
        repo.addSessionSkeleton(tabata)
        repo.addCustomWorkoutSkeleton(CustomWorkoutSkeleton("3 x Tabata", arrayListOf(
            tabata, rest30Sec, tabata, rest30Sec, tabata)))

        val emom5 = SessionSkeleton(0, TimeUnit.MINUTES.toSeconds(1).toInt(), 0, 5, SessionType.EMOM)
        repo.addCustomWorkoutSkeleton(CustomWorkoutSkeleton("Power Intervals", arrayListOf(
            emom5, rest1Min, emom5, rest1Min, emom5)))
    }
}
