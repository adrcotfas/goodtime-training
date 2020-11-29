package goodtime.training.wod.timer

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
    private var favoritesButton: MenuItem? = null
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

            if (destination.label == "WorkoutFragment") {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        binding.startButton.setOnClickListener{ getVisibleFragment().onStartWorkout() }
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_save_favorite -> {
                    onFavoritesButtonClick()
                }
                else -> false
            }
        }
        binding.workoutMenu.setupWithNavController(navController)
        binding.workoutMenu.setOnNavigationItemReselectedListener {
            // Nothing here to disable reselect
        }
    }

    private fun onFavoritesButtonClick(): Boolean {
        val fragment = getVisibleFragment()
        val sessions = fragment.getSelectedSessions()
        return if (fragment is CustomWorkoutFragment) {
            SelectCustomWorkoutDialog.newInstance(fragment).show(supportFragmentManager, "")
            true
        } else {
            SelectFavoriteDialog.newInstance(sessions[0], fragment)
                .show(supportFragmentManager, "")
            true
        }
    }

    private fun getVisibleFragment() =
        (supportFragmentManager.currentNavigationFragment as WorkoutTypeFragment)

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        favoritesButton = menu!!.findItem(R.id.action_save_favorite)
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
