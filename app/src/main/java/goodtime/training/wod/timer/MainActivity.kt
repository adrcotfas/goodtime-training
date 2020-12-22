package goodtime.training.wod.timer

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import goodtime.training.wod.timer.common.ResourcesHelper
import goodtime.training.wod.timer.common.currentNavigationFragment
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import goodtime.training.wod.timer.databinding.ActivityMainBinding
import goodtime.training.wod.timer.ui.main.FullscreenHelper
import goodtime.training.wod.timer.ui.main.SelectFavoriteDialog
import goodtime.training.wod.timer.ui.main.WorkoutTypeFragment
import goodtime.training.wod.timer.ui.main.custom.CustomWorkoutFragment
import goodtime.training.wod.timer.ui.main.custom.SelectCustomWorkoutDialog
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance

class MainActivity : AppCompatActivity(), KodeinAware {

    override val kodein by closestKodein()

    private val preferenceHelper by instance<PreferenceHelper>()

    private lateinit var navController: NavController

    private lateinit var binding: ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment

    private lateinit var startButton: FloatingActionButton

    private lateinit var favoritesButton: Chip
    private var newCustomWorkoutMenuItem: MenuItem? = null
    private var favoritesMenuItem: MenuItem? = null

    private lateinit var newCustomWorkoutButton: Chip

    private lateinit var fullscreenHelper: FullscreenHelper

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        startButton = binding.contentMain.startButton
        val toolbar = binding.contentMain.toolbar

        setContentView(binding.root)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setSupportActionBar(toolbar)

        fullscreenHelper = FullscreenHelper(binding.contentMain.mainLayout)

        appBarConfiguration = AppBarConfiguration(
                setOf(R.id.nav_amrap, R.id.nav_for_time, R.id.nav_emom, R.id.nav_hiit, R.id.nav_custom),
                binding.drawerLayout)

        val bottomNavigationView = binding.contentMain.bottomNavigationView

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isTopLevel = appBarConfiguration.topLevelDestinations.contains(destination.id)
            if (isTopLevel) toolbar.setNavigationIcon(R.drawable.ic_menu_open)
            else toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
            supportActionBar?.title = if (isTopLevel) null else destination.label

            bottomNavigationView.visibility = if (isTopLevel) View.VISIBLE else View.GONE
            startButton.visibility = if (isTopLevel) View.VISIBLE else View.GONE

            val hideToolbar = destination.label == "WorkoutFragment" ||
                        destination.label == "StopWorkoutDialog"
            toolbar.visibility = if (hideToolbar) View.GONE else View.VISIBLE

            if (preferenceHelper.isFullscreenModeEnabled()) {
                toggleFullscreenMode(hideToolbar)
            }

            if (destination.label == "WorkoutFragment" || destination.label == "StopWorkoutDialog") {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }

            newCustomWorkoutMenuItem?.isVisible = destination.label == "CustomWorkout"
            favoritesMenuItem?.isVisible = isTopLevel
        }

        startButton.setOnClickListener{ getVisibleFragment().onStartWorkout() }
        bottomNavigationView.setupWithNavController(navController)
        bottomNavigationView.setOnNavigationItemReselectedListener {
            // Nothing here to disable reselect
        }

        binding.navView.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun onFavoritesButtonClick() {
        if (supportFragmentManager.findFragmentByTag("SelectFavorite") == null) {
            val fragment = getVisibleFragment()
            val sessions = fragment.getSelectedSessions()
            if (fragment is CustomWorkoutFragment) {
                SelectCustomWorkoutDialog.newInstance(fragment).show(supportFragmentManager, "SelectFavorite")
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main_top, menu)
        favoritesMenuItem = menu!!.findItem(R.id.action_favorites)
        favoritesButton = favoritesMenuItem!!.actionView.findViewById(R.id.root)
        favoritesButton.setOnClickListener{ onFavoritesButtonClick() }

        newCustomWorkoutMenuItem = menu.findItem(R.id.action_new_workout)
        newCustomWorkoutButton = newCustomWorkoutMenuItem!!.actionView.findViewById(R.id.root)
        newCustomWorkoutButton.setOnClickListener { onNewCustomWorkoutButtonClick() }
        return true
    }

    fun setStartButtonState(enabled: Boolean) {
        startButton.isEnabled = enabled
        if (enabled) {
            startButton.background?.setTint(ResourcesHelper.darkerGreen)
            startButton.drawable?.setTint(ResourcesHelper.green)
        } else {
            startButton.background?.setTint(ResourcesHelper.grey1000)
            startButton.drawable?.setTint(ResourcesHelper.grey800)
        }
    }

    private fun toggleFullscreenMode(newState: Boolean) {
        if (newState) {
            fullscreenHelper.enable()
        } else {
            fullscreenHelper.disable()
        }
    }
}
