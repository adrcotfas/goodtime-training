package com.adrcotfas.wod

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.adrcotfas.wod.common.currentNavigationFragment
import com.adrcotfas.wod.databinding.ActivityMainBinding
import com.adrcotfas.wod.ui.common.ui.SaveFavoriteDialog
import com.adrcotfas.wod.ui.main.MainFragment

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    private lateinit var binding : ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment
    private var favoritesButton: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        setupAppBar()

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val hideButtons =
                destination.label == "LogFragment" ||
                        destination.label == "WorkoutFragment" ||
                        destination.label == "StopWorkoutDialog"
            binding.toolbar.visibility = if (hideButtons) View.GONE else View.VISIBLE
            binding.startButton.visibility = if (hideButtons) View.GONE else View.VISIBLE

            if (destination.label == "WorkoutFragment") {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        binding.startButton.setOnClickListener{
            ((supportFragmentManager.currentNavigationFragment as MainFragment)
                .getFragment()).onStartWorkout()
        }
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_save_favorite -> {
                    val fragment =
                        (supportFragmentManager.currentNavigationFragment as MainFragment)
                            .getFragment()
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
    }

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
}
