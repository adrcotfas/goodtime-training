package goodtime.training.wod.timer

import android.app.Application
import android.content.Context
import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.resources.TextAppearanceConfig
import goodtime.training.wod.timer.common.TimeUtils
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import goodtime.training.wod.timer.common.preferences.reminders.ReminderHelper
import goodtime.training.wod.timer.common.sound_and_vibration.SoundPlayer
import goodtime.training.wod.timer.data.db.*
import goodtime.training.wod.timer.data.model.CustomWorkoutSkeleton
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import goodtime.training.wod.timer.data.model.WeeklyGoal
import goodtime.training.wod.timer.data.repository.AppRepository
import goodtime.training.wod.timer.data.repository.AppRepositoryImpl
import goodtime.training.wod.timer.ui.main.amrap_for_time.AmrapViewModelFactory
import goodtime.training.wod.timer.ui.main.amrap_for_time.ForTimeViewModelFactory
import goodtime.training.wod.timer.ui.main.custom.CustomWorkoutViewModelFactory
import goodtime.training.wod.timer.ui.main.intervals.IntervalsViewModelFactory
import goodtime.training.wod.timer.ui.main.hiit.HiitViewModelFactory
import goodtime.training.wod.timer.ui.settings.EncryptedPreferenceDataStore
import goodtime.training.wod.timer.ui.stats.StatisticsViewModelFactory
import goodtime.training.wod.timer.ui.stats.WeeklyGoalViewModelFactory
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

        lateinit var context: Context
            private set

        private lateinit var reminderHelper: ReminderHelper
        fun getReminderHelper(): ReminderHelper {
            return reminderHelper
        }

        fun getDatabase(context: Context) = GoodtimeDatabase.getDatabase(context)
    }

    override val kodein = Kodein.lazy {
        bind<SessionDao>() with eagerSingleton { getDatabase(this@GoodtimeApplication).sessionsDao() }
        bind<SessionSkeletonDao>() with eagerSingleton { getDatabase(this@GoodtimeApplication).sessionSkeletonDao() }
        bind<CustomWorkoutSkeletonDao>() with eagerSingleton { getDatabase(this@GoodtimeApplication).customWorkoutSkeletonDao() }
        bind<WeeklyGoalDao>() with eagerSingleton { getDatabase(this@GoodtimeApplication).weeklyGoalDao() }
        bind<AppRepository>() with eagerSingleton { AppRepositoryImpl(instance(), instance(), instance(), instance()) }
        bind<PreferenceHelper>() with eagerSingleton { PreferenceHelper(EncryptedPreferenceDataStore(applicationContext)) }
        bind<SoundPlayer>() with eagerSingleton { SoundPlayer(applicationContext) }
        bind() from provider { AmrapViewModelFactory(instance()) }
        bind() from provider { ForTimeViewModelFactory(instance()) }
        bind() from provider { IntervalsViewModelFactory(instance()) }
        bind() from provider { HiitViewModelFactory(instance()) }
        bind() from provider { CustomWorkoutViewModelFactory(instance()) }
        bind() from provider { StatisticsViewModelFactory(instance()) }
        bind() from provider { WeeklyGoalViewModelFactory(instance())}
        bind() from provider { TimerViewModelFactory(applicationContext, instance(), instance(), instance()) }
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        res = resources
        reminderHelper = ReminderHelper(this)

        TextAppearanceConfig.setShouldLoadFontSynchronously(true)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        val preferenceHelper: PreferenceHelper by instance()
        if (preferenceHelper.isFirstRun()) {
            generateDefaultSessions()
            preferenceHelper.setIsFirstRun(false)
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
            SessionType.INTERVALS))

        val tabata = SessionSkeleton(
            0, 20, 10, 8,
            SessionType.HIIT
        )
        val rest30Sec = SessionSkeleton(0, 30, 0, 0, SessionType.REST)
        val rest1Min = SessionSkeleton(0, TimeUnit.MINUTES.toSeconds(1).toInt(), 0, 0, SessionType.REST)

        //TODO: remove before release
        val dummyAmrap = SessionSkeleton(0, 5, type = SessionType.AMRAP)
        val dummyForTime = SessionSkeleton(0, 5, type = SessionType.FOR_TIME)
        val dummyHiit = SessionSkeleton(
            0, 3, 3, 3,
            SessionType.HIIT
        )
        val dummyIntervals = SessionSkeleton(
            0, 3, 0, 2,
            SessionType.INTERVALS
        )
        val dummyRest = SessionSkeleton(0, 30, 5, 0, SessionType.REST)
        repo.addCustomWorkoutSkeleton(CustomWorkoutSkeleton("Dummy",
            arrayListOf(
                dummyAmrap,
                dummyForTime,
                dummyHiit,
                dummyRest,
                dummyIntervals,
                dummyRest,
                dummyAmrap)))

        repo.addSessionSkeleton(rest30Sec)
        repo.addSessionSkeleton(rest1Min)

        //Custom workouts
        repo.addSessionSkeleton(tabata)
        repo.addCustomWorkoutSkeleton(CustomWorkoutSkeleton("3 x Tabata", arrayListOf(
            tabata, rest30Sec, tabata, rest30Sec, tabata)))

        val intervals5 = SessionSkeleton(0, TimeUnit.MINUTES.toSeconds(1).toInt(), 0, 5, SessionType.INTERVALS)
        repo.addCustomWorkoutSkeleton(CustomWorkoutSkeleton("Power Intervals", arrayListOf(
            intervals5, rest1Min, intervals5, rest1Min, intervals5)))

        repo.addWeeklyGoal(WeeklyGoal(75, TimeUtils.firstDayOfLastWeekMillis(), 0, 0))
    }
}
