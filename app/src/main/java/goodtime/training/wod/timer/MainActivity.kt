package goodtime.training.wod.timer

import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import goodtime.training.wod.timer.common.DimensionsUtils.Companion.dpToPx
import goodtime.training.wod.timer.common.Events
import goodtime.training.wod.timer.common.ResourcesHelper
import goodtime.training.wod.timer.common.currentNavigationFragment
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import goodtime.training.wod.timer.databinding.ActivityMainBinding
import goodtime.training.wod.timer.ui.main.CustomBalloonFactory
import goodtime.training.wod.timer.ui.main.FullscreenHelper
import goodtime.training.wod.timer.ui.main.SelectFavoriteDialog
import goodtime.training.wod.timer.ui.main.WorkoutTypeFragment
import goodtime.training.wod.timer.ui.main.custom.CustomWorkoutFragment
import goodtime.training.wod.timer.ui.main.custom.SelectCustomWorkoutDialog
import goodtime.training.wod.timer.ui.stats.EditWeeklyGoalDialog
import goodtime.training.wod.timer.ui.stats.WeeklyGoalViewModel
import goodtime.training.wod.timer.ui.stats.WeeklyGoalViewModelFactory
import kotlinx.android.synthetic.main.content_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance
import java.lang.Integer.min


class MainActivity : AppCompatActivity(), KodeinAware, SharedPreferences.OnSharedPreferenceChangeListener {

    override val kodein by closestKodein()

    private val preferenceHelper by instance<PreferenceHelper>()

    private lateinit var navController: NavController

    private lateinit var binding: ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment

    private lateinit var startButton: FloatingActionButton
    private lateinit var favoritesButton: Chip

    private lateinit var addSessionButton: Chip
    private lateinit var filterButton: Chip

    private lateinit var newCustomWorkoutButton: Chip
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var fullscreenHelper: FullscreenHelper

    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var currentDestination: NavDestination

    private val weeklyGoalViewModelFactory: WeeklyGoalViewModelFactory by instance()
    private lateinit var weeklyGoalViewModel: WeeklyGoalViewModel

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onResume() {
        super.onResume()
        showBalloonsIfNeeded()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferenceHelper.dataStore.preferences.registerOnSharedPreferenceChangeListener(this)

        binding = ActivityMainBinding.inflate(layoutInflater)

        weeklyGoalViewModel = ViewModelProvider(this, weeklyGoalViewModelFactory).get(WeeklyGoalViewModel::class.java)
        setupWeeklyGoal()

        startButton = binding.contentMain.startButton
        favoritesButton = binding.contentMain.buttonFavorites.root
        newCustomWorkoutButton = binding.contentMain.buttonNew.root

        filterButton = binding.contentMain.buttonFilter.root // used on the statistics page
        filterButton.setOnClickListener {
            EventBus.getDefault().post(Events.Companion.FilterButtonClickEvent())
        }
        filterButton.setOnCloseIconClickListener {
            EventBus.getDefault().post(Events.Companion.FilterClearButtonClickEvent())
        }

        addSessionButton = binding.contentMain.buttonAddSession.root
        addSessionButton.setOnClickListener {
            EventBus.getDefault().post(Events.Companion.AddToStatisticsClickEvent())
        }

        val toolbar = binding.contentMain.toolbar

        setContentView(binding.root)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        if (preferenceHelper.wasKilledDuringWorkout()) {
            preferenceHelper.setKilledDuringWorkout(false)
            navController.navigate(R.id.to_workout)
        }

        setSupportActionBar(toolbar)

        fullscreenHelper = FullscreenHelper(binding.contentMain.mainLayout)

        appBarConfiguration = AppBarConfiguration(
                setOf(
                        R.id.nav_amrap,
                        R.id.nav_for_time,
                        R.id.nav_intervals,
                        R.id.nav_hiit,
                        R.id.nav_custom
                ),
                binding.drawerLayout
        )

        bottomNavigationView = binding.contentMain.bottomNavigationView

        navController.addOnDestinationChangedListener { _, destination, _ ->
            currentDestination = destination
            val isTopLevel = appBarConfiguration.topLevelDestinations.contains(destination.id)
            toolbar.setNavigationIcon(if (isTopLevel) R.drawable.ic_menu_open else R.drawable.ic_arrow_back)
            binding.drawerLayout.setDrawerLockMode(if (isTopLevel) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

            supportActionBar?.title = if (isTopLevel || destination.label == "Statistics") null else destination.label
            toggleMinimalistMode(preferenceHelper.isMinimalistEnabled())
            bottomNavigationView.isVisible = isTopLevel
            startButton.apply { if (isTopLevel) show() else hide() }

            //TODO: extract fragment labels to untranslatable strings
            val hideToolbar = destination.label == "TimerFragment" ||
                    destination.label == "StopWorkoutDialog" || destination.label == "FinishedWorkoutFragment"
            toolbar.isVisible = !hideToolbar

            if (preferenceHelper.isFullscreenModeEnabled()) {
                toggleFullscreenMode(hideToolbar)
            }

            if (destination.label == "TimerFragment" || destination.label == "StopWorkoutDialog") {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }

            favoritesButton.isVisible = isTopLevel
            newCustomWorkoutButton.isVisible = destination.label == "CustomWorkout"
            filterButton.isVisible = currentDestination.label == "Statistics"
            addSessionButton.isVisible = currentDestination.label == "Statistics"
        }

        startButton.setOnClickListener { getVisibleFragment().onStartWorkout() }
        bottomNavigationView.setupWithNavController(navController)
        bottomNavigationView.setOnNavigationItemReselectedListener {
            // Nothing here to disable reselect
        }

        binding.navView.setupWithNavController(navController)

        binding.contentMain.buttonFavorites.root.setOnClickListener { onFavoritesButtonClick() }
        binding.contentMain.buttonNew.root.setOnClickListener { onNewCustomWorkoutButtonClick() }

        binding.buttonSettings.root.setOnClickListener {
            navController.navigate(MobileNavigationDirections.toSettings())
            binding.drawerLayout.closeDrawers()
        }
        binding.buttonStatistics.root.setOnClickListener {
            navController.navigate(MobileNavigationDirections.toStats())
            binding.drawerLayout.closeDrawers()
        }
    }

    private fun setupWeeklyGoal() {
        binding.weeklyGoalSection.root.setOnClickListener {
            EditWeeklyGoalDialog().show(supportFragmentManager, "EditWeeklyGoalDialog")
        }
        weeklyGoalViewModel.getWeeklyGoalData().observe(this, {
            val thereIsNoGoal = it.goal.minutes == 0

            binding.weeklyGoalSection.description.isVisible = !thereIsNoGoal
            binding.weeklyGoalSection.progressBar.isVisible = !thereIsNoGoal

            if (!thereIsNoGoal) {
                val progress = (it.minutesThisWeek * 100 / it.goal.minutes).toInt()
                binding.weeklyGoalSection.progressBar.progress = min(100, progress)
                binding.weeklyGoalSection.description.text =
                    "${progress}% (${it.minutesThisWeek} minutes / ${it.goal.minutes} minutes)"
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun onFavoritesButtonClick() {
        if (supportFragmentManager.findFragmentByTag("SelectFavorite") == null) {
            val fragment = getVisibleFragment()
            val sessions = fragment.getSelectedSessions()
            if (fragment is CustomWorkoutFragment) {
                SelectCustomWorkoutDialog.newInstance(fragment).show(
                        supportFragmentManager,
                        "SelectFavorite"
                )
            } else {
                SelectFavoriteDialog.newInstance(sessions[0], fragment)
                        .show(supportFragmentManager, "SelectFavorite")
            }
        }
    }

    private fun onNewCustomWorkoutButtonClick() {
        val fragment = getVisibleFragment()
        if (fragment is CustomWorkoutFragment) {
            fragment.onNewCustomWorkoutButtonClick()
        }
    }

    private fun getVisibleFragment() =
            (supportFragmentManager.currentNavigationFragment as WorkoutTypeFragment)

    private fun setStartButtonStateWithColor(enabled: Boolean) {
        setStartButtonState(enabled)
        if (enabled) {
            startButton.background?.setTint(ResourcesHelper.darkerGreen)
            startButton.drawable?.setTint(ResourcesHelper.green)
        } else {
            startButton.background?.setTint(ResourcesHelper.grey1000)
            startButton.drawable?.setTint(ResourcesHelper.grey800)
        }
    }

    private fun setStartButtonState(enabled: Boolean) {
        startButton.isEnabled = enabled
    }

    private fun toggleFullscreenMode(newState: Boolean) {
        if (newState) {
            fullscreenHelper.enable()
        } else {
            fullscreenHelper.disable()
        }
    }

    override fun onSharedPreferenceChanged(preference: SharedPreferences, key: String) {
        if (key == PreferenceHelper.MINIMALIST_MODE_ENABLED) {
            val enabled = preferenceHelper.isMinimalistEnabled()
            toggleMinimalistMode(enabled)
        }
    }

    private fun toggleMinimalistMode(enabled: Boolean) {
        bottomNavigationView.labelVisibilityMode =
                if (enabled) LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED
                else LabelVisibilityMode.LABEL_VISIBILITY_LABELED

        if (enabled) {
            val startPadding = dpToPx(this, 10f).toFloat()
            favoritesButton.text = ""
            favoritesButton.chipEndPadding = 0f
            favoritesButton.chipStartPadding = startPadding

            newCustomWorkoutButton.text = ""
            newCustomWorkoutButton.chipEndPadding = 0f
            newCustomWorkoutButton.chipStartPadding = startPadding
        } else {
            val padding = dpToPx(this, 4f).toFloat()
            favoritesButton.text = getString(R.string.favorites)
            favoritesButton.chipEndPadding = padding
            favoritesButton.chipStartPadding = padding

            newCustomWorkoutButton.text = getString(R.string.new_title)
            newCustomWorkoutButton.chipEndPadding = padding
            newCustomWorkoutButton.chipStartPadding = padding
        }
    }

    private fun showBalloonsIfNeeded() {
        if (preferenceHelper.showMainBalloons()) {
            preferenceHelper.setMainBalloons(false)
            binding.root.post {
                val bottomMenuBalloon = CustomBalloonFactory.create(
                        this, this,
                        "Use the bottom menu to change the workout type."
                )
                val amrapBalloon = CustomBalloonFactory.create(
                        this, this,
                        "The goal for AMRAP workouts is to complete as many rounds as possible in the allocated time."
                )
                val timePickersBalloon = CustomBalloonFactory.create(
                        this, this,
                        "Use the time pickers to change the duration.",
                        false, 0.5f
                )
                val favoriteButtonBalloon = CustomBalloonFactory.create(
                        this, this,
                        "Use the favorites section to save, remove and load timer presets.",
                        true, 0.83f
                )
                val startButtonBalloon = CustomBalloonFactory.create(
                        this, this,
                        "Press the action button to start the workout using the current selection.",
                        false, 0.5f
                )
                bottomMenuBalloon.relayShowAlignBottom(amrapBalloon, bottomNavigationView, 0, 12)
                        .relayShowAlignBottom(timePickersBalloon, toolbar, 0, 12)
                        .relayShowAlignBottom(favoriteButtonBalloon, favoritesButton, 0, 12)
                        .relayShowAlignTop(startButtonBalloon, startButton, 0, -12)
                bottomMenuBalloon.showAlignTop(bottomNavigationView, 0, -12)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: Events.Companion.FilterSelectedEvent) {
        filterButton.text = event.name
        filterButton.isCloseIconVisible = true
        filterButton.isChipIconVisible = false
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: Events.Companion.FilterClearButtonClickEvent) {
        filterButton.text = getString(R.string.filter)
        filterButton.isCloseIconVisible = false
        filterButton.isChipIconVisible = true
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: Events.Companion.SetStartButtonState) {
        setStartButtonState(event.enabled)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: Events.Companion.SetStartButtonStateWithColor) {
        setStartButtonStateWithColor(event.enabled)
    }
}
