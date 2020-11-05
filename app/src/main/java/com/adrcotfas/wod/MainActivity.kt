package com.adrcotfas.wod

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.adrcotfas.wod.common.currentNavigationFragment
import com.adrcotfas.wod.databinding.ActivityMainBinding
import com.adrcotfas.wod.ui.common.WorkoutTypeFragment
import com.adrcotfas.wod.ui.common.ui.FullscreenHelper
import com.adrcotfas.wod.ui.common.ui.SaveFavoriteDialog

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

        binding.startButton.setOnClickListener{ getMainFragment().onStartWorkout() }
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_save_favorite -> {
                    val fragment = getMainFragment()
                    val session = fragment.getSelectedSession()
                    if (session.duration == 0) {
                        false
                    } else {
                        SaveFavoriteDialog.newInstance(session, fragment).show(supportFragmentManager, "")
                        true
                    }
                }
                else -> false
            }
        }
        binding.workoutMenu.setupWithNavController(navController)
        binding.workoutMenu.menu[4].isEnabled = false
        binding.workoutMenu.setOnNavigationItemReselectedListener {
            // Nothing here to disable reselect
        }
    }

    private fun getMainFragment() =
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
            binding.startButton.background?.setTint(resources.getColor(R.color.green_goodtime_darker))
            binding.startButton.drawable?.setTint(resources.getColor(R.color.green_goodtime))
            favoritesButton?.icon?.setColorFilter(
                resources.getColor(R.color.red_goodtime), PorterDuff.Mode.SRC_ATOP)
        } else {
            binding.startButton.background?.setTint(resources.getColor(R.color.grey1000))
            binding.startButton.drawable?.setTint(resources.getColor(R.color.grey800))
            favoritesButton?.icon?.setColorFilter(
                resources.getColor(R.color.grey800), PorterDuff.Mode.SRC_ATOP)
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
