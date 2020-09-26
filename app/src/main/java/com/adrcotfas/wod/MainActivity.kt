package com.adrcotfas.wod

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.adrcotfas.wod.common.currentNavigationFragment
import com.adrcotfas.wod.databinding.ActivityMainBinding
import com.adrcotfas.wod.ui.common.WorkoutTypeFragment
import com.adrcotfas.wod.ui.main.MainFragment

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    private lateinit var binding : ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment

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
            binding.bottomAppBar.visibility = if (hideButtons) View.GONE else View.VISIBLE
            binding.startButton.visibility = if (hideButtons) View.GONE else View.VISIBLE

            if (destination.label == "WorkoutFragment") {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        binding.startButton.setOnClickListener{
            ((supportFragmentManager.currentNavigationFragment as MainFragment)
                .getFragment() as WorkoutTypeFragment).onStartWorkout()
        }
        binding.bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_save_favorite -> {
                    Toast.makeText(this, "Open favorites dialog", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupAppBar() {
        binding.bottomAppBar.setNavigationOnClickListener {
            Toast.makeText(this, "Clicked navigation item", Toast.LENGTH_SHORT).show()
        }
    }

    fun setStartButtonState(enabled: Boolean) {
        binding.startButton.isEnabled = enabled
    }
}
