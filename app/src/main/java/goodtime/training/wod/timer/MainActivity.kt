package goodtime.training.wod.timer

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.chip.Chip
import goodtime.training.wod.timer.common.ResourcesHelper
import goodtime.training.wod.timer.common.currentNavigationFragment
import goodtime.training.wod.timer.databinding.ActivityMainBinding
import goodtime.training.wod.timer.ui.main.WorkoutTypeFragment
import goodtime.training.wod.timer.ui.main.FullscreenHelper
import goodtime.training.wod.timer.ui.main.custom.SelectCustomWorkoutDialog
import goodtime.training.wod.timer.ui.main.SelectFavoriteDialog
import goodtime.training.wod.timer.ui.main.custom.CustomWorkoutFragment

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    private lateinit var binding : ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment

    private lateinit var favoritesButton: Chip
    private var newCustomWorkoutMenuItem: MenuItem? = null
    private lateinit var newCustomWorkoutButton: Chip

    private lateinit var fullscreenHelper : FullscreenHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        setupAppBar()
        fullscreenHelper = FullscreenHelper(binding.mainLayout)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val hideButtons =
                destination.label == "LogFragment" ||
                        destination.label == "WorkoutFragment" ||
                        destination.label == "StopWorkoutDialog"
            binding.toolbar.visibility = if (hideButtons) View.GONE else View.VISIBLE
            binding.workoutMenu.visibility = if (hideButtons) View.GONE else View.VISIBLE
            if (hideButtons) binding.startButton.hide() else binding.startButton.show()
            //TODO: maybe activate later with a setting
            //toggleFullscreenMode(hideButtons)

            if (destination.label == "WorkoutFragment" || destination.label == "StopWorkoutDialog") {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }

            newCustomWorkoutMenuItem?.isVisible = destination.label == "CustomWorkout"
        }

        binding.startButton.setOnClickListener{ getVisibleFragment().onStartWorkout() }
        binding.workoutMenu.setupWithNavController(navController)
        binding.workoutMenu.setOnNavigationItemReselectedListener {
            // Nothing here to disable reselect
        }
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
        menuInflater.inflate(R.menu.menu_main, menu)
        val favoritesMenuItem = menu!!.findItem(R.id.action_favorites)
        favoritesButton = favoritesMenuItem.actionView.findViewById(R.id.root)
        favoritesButton.setOnClickListener{ onFavoritesButtonClick() }

        newCustomWorkoutMenuItem = menu.findItem(R.id.action_new_workout)
        newCustomWorkoutButton = newCustomWorkoutMenuItem!!.actionView.findViewById(R.id.root)
        newCustomWorkoutButton.setOnClickListener { onNewCustomWorkoutButtonClick() }
        return super.onCreateOptionsMenu(menu)
    }

    private fun setupAppBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = null

        binding.toolbar.setNavigationOnClickListener {
            Toast.makeText(this, "Clicked navigation item", Toast.LENGTH_SHORT).show()
        }
    }

    fun setStartButtonState(enabled: Boolean) {
        binding.startButton.isEnabled = enabled
        if (enabled) {
            binding.startButton.background?.setTint(ResourcesHelper.darkerGreen)
            binding.startButton.drawable?.setTint(ResourcesHelper.green)
        } else {
            binding.startButton.background?.setTint(ResourcesHelper.grey1000)
            binding.startButton.drawable?.setTint(ResourcesHelper.grey800)
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
