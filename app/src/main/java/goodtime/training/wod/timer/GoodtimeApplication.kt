package goodtime.training.wod.timer

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import goodtime.training.wod.timer.billing.BillingViewModel
import goodtime.training.wod.timer.common.DimensionsUtils.Companion.getWindowHeight
import goodtime.training.wod.timer.common.TimeUtils
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import goodtime.training.wod.timer.common.preferences.reminders.ReminderHelper
import goodtime.training.wod.timer.common.sound_and_vibration.SoundPlayer
import goodtime.training.wod.timer.data.db.CustomWorkoutSkeletonDao
import goodtime.training.wod.timer.data.db.GoodtimeDatabase
import goodtime.training.wod.timer.data.db.SessionDao
import goodtime.training.wod.timer.data.db.SessionSkeletonDao
import goodtime.training.wod.timer.data.db.WeeklyGoalDao
import goodtime.training.wod.timer.data.model.CustomWorkoutSkeleton
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import goodtime.training.wod.timer.data.model.WeeklyGoal
import goodtime.training.wod.timer.data.repository.AppRepository
import goodtime.training.wod.timer.data.repository.AppRepositoryImpl
import goodtime.training.wod.timer.ui.main.amrap_for_time.AmrapViewModelFactory
import goodtime.training.wod.timer.ui.main.amrap_for_time.ForTimeViewModelFactory
import goodtime.training.wod.timer.ui.main.custom.CustomWorkoutViewModelFactory
import goodtime.training.wod.timer.ui.main.hiit.HiitViewModelFactory
import goodtime.training.wod.timer.ui.main.intervals.IntervalsViewModelFactory
import goodtime.training.wod.timer.ui.settings.PreferenceDataStore
import goodtime.training.wod.timer.ui.stats.StatisticsViewModelFactory
import goodtime.training.wod.timer.ui.stats.WeeklyGoalViewModelFactory
import goodtime.training.wod.timer.ui.timer.DNDHandler
import goodtime.training.wod.timer.ui.timer.TimerNotificationHelper
import goodtime.training.wod.timer.ui.timer.TimerViewModelFactory
import goodtime.training.wod.timer.ui.timer.WorkoutManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.bind
import org.kodein.di.generic.eagerSingleton
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton
import java.util.concurrent.TimeUnit

class GoodtimeApplication : Application(), KodeinAware {

    companion object {
        private lateinit var res: Resources
        fun getRes(): Resources {
            return res
        }

        @SuppressLint("StaticFieldLeak") // should be fine in this class
        lateinit var context: Context
            private set

        private lateinit var reminderHelper: ReminderHelper
        fun getReminderHelper(): ReminderHelper {
            return reminderHelper
        }

        fun getDatabase(context: Context) = GoodtimeDatabase.getDatabase(context)

        var windowHeightPortrait: Int = 0
            private set
    }

    override val kodein = Kodein.lazy {
        bind<SessionDao>() with eagerSingleton { getDatabase(this@GoodtimeApplication).sessionsDao() }
        bind<SessionSkeletonDao>() with eagerSingleton { getDatabase(this@GoodtimeApplication).sessionSkeletonDao() }
        bind<CustomWorkoutSkeletonDao>() with eagerSingleton { getDatabase(this@GoodtimeApplication).customWorkoutSkeletonDao() }
        bind<WeeklyGoalDao>() with eagerSingleton { getDatabase(this@GoodtimeApplication).weeklyGoalDao() }
        bind<AppRepository>() with eagerSingleton { AppRepositoryImpl(instance(), instance(), instance(), instance()) }
        bind<PreferenceHelper>() with eagerSingleton { PreferenceHelper(PreferenceDataStore(applicationContext)) }
        bind<SoundPlayer>() with eagerSingleton { SoundPlayer(applicationContext) }
        bind<TimerNotificationHelper>() with eagerSingleton {
            TimerNotificationHelper(
                applicationContext,
                instance(),
                instance()
            )
        }
        bind<WorkoutManager>() with eagerSingleton { WorkoutManager(instance()) }
        bind() from provider { AmrapViewModelFactory(instance()) }
        bind() from provider { ForTimeViewModelFactory(instance()) }
        bind() from provider { IntervalsViewModelFactory(instance()) }
        bind() from provider { HiitViewModelFactory(instance()) }
        bind() from provider { CustomWorkoutViewModelFactory(instance()) }
        bind() from provider { StatisticsViewModelFactory(instance()) }
        bind() from provider { WeeklyGoalViewModelFactory(instance()) }
        bind<DNDHandler>() with singleton { DNDHandler(applicationContext) }
        bind() from provider { TimerViewModelFactory(instance(), instance(), instance(), instance()) }

        bind<BillingViewModel>() with eagerSingleton { BillingViewModel(this@GoodtimeApplication, instance())}
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        res = resources
        windowHeightPortrait = getWindowHeight(applicationContext)
        reminderHelper = ReminderHelper(this)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        val preferenceHelper: PreferenceHelper by instance()
        if (preferenceHelper.getFirstRunTime() == 0L) {
            generateDefaultSessions()
            preferenceHelper.updateFirstRunTime()
        }
    }

    private fun generateDefaultSessions() {
        val repo: AppRepository by instance()

        MainScope().launch {
            repo.addSessionSkeleton(
                SessionSkeleton(0, TimeUnit.MINUTES.toSeconds(10).toInt(), type = SessionType.AMRAP)
            )
            repo.addSessionSkeleton(
                SessionSkeleton(0, TimeUnit.MINUTES.toSeconds(15).toInt(), type = SessionType.AMRAP)
            )
            repo.addSessionSkeleton(
                SessionSkeleton(0, TimeUnit.MINUTES.toSeconds(20).toInt(), type = SessionType.AMRAP)
            )

            repo.addSessionSkeleton(
                SessionSkeleton(
                    0,
                    TimeUnit.MINUTES.toSeconds(15).toInt(),
                    type = SessionType.FOR_TIME
                )
            )
            repo.addSessionSkeleton(
                SessionSkeleton(
                    0, TimeUnit.MINUTES.toSeconds(1).toInt(), 0, 20,
                    SessionType.INTERVALS
                )
            )

            val tabata = SessionSkeleton(
                0, 20, 10, 8,
                SessionType.HIIT
            )
            val rest30Sec = SessionSkeleton(0, 30, 0, 0, SessionType.REST)
            val rest1Min = SessionSkeleton(0, TimeUnit.MINUTES.toSeconds(1).toInt(), 0, 0, SessionType.REST)

            repo.addSessionSkeleton(rest30Sec)
            repo.addSessionSkeleton(rest1Min)

            //Custom workouts

            val intervals5 = SessionSkeleton(0, TimeUnit.MINUTES.toSeconds(1).toInt(), 0, 5, SessionType.INTERVALS)
            repo.addCustomWorkoutSkeleton(
                CustomWorkoutSkeleton(
                    "Power Intervals", arrayListOf(
                        intervals5, rest1Min, intervals5, rest1Min, intervals5
                    )
                )
            )

            repo.addSessionSkeleton(tabata)
            repo.addCustomWorkoutSkeleton(
                CustomWorkoutSkeleton(
                    "4 x Tabata", arrayListOf(
                        tabata, rest1Min, tabata, rest1Min, tabata, rest1Min, tabata
                    )
                )
            )

            repo.updateWeeklyGoal(WeeklyGoal(75, TimeUtils.firstDayOfLastWeekMillis(), 0, 0))
        }
    }
}
