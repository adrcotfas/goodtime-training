package com.adrcotfas.wod

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.adrcotfas.wod.common.currentNavigationFragment
import com.adrcotfas.wod.databinding.ActivityMainBinding
import com.adrcotfas.wod.ui.amrap.AmrapFragment
import com.adrcotfas.wod.ui.emom.EmomFragment
import com.adrcotfas.wod.ui.for_time.ForTimeFragment
import com.adrcotfas.wod.ui.tabata.TabataFragment
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.app_bar_main.view.*

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = findNavController(R.id.nav_host_fragment)
        setupAppBar()

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val hideButtons =
                destination.label == "LogFragment" ||
                        destination.label == "WorkoutFragment" ||
                        destination.label == "StopWorkoutDialog"
            binding.mainLayout.buttons.visibility = if (hideButtons) View.GONE else View.VISIBLE
            binding.bottomAppBar.visibility = if (hideButtons) View.GONE else View.VISIBLE
            binding.startButton.visibility = if (hideButtons) View.GONE else View.VISIBLE

            if (destination.label == "WorkoutFragment") {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        binding.startButton.setOnClickListener{
            when (supportFragmentManager.currentNavigationFragment) {
                is AmrapFragment -> {
                    val fragment = supportFragmentManager.currentNavigationFragment as AmrapFragment
                    fragment.onStartWorkout()
                }
                is ForTimeFragment -> {
                    val fragment = supportFragmentManager.currentNavigationFragment as ForTimeFragment
                    fragment.onStartWorkout()
                }
                is EmomFragment -> {
                    val fragment = supportFragmentManager.currentNavigationFragment as EmomFragment
                    fragment.onStartWorkout()
                }
                is TabataFragment -> {
                    val fragment = supportFragmentManager.currentNavigationFragment as TabataFragment
                    fragment.onStartWorkout()
                }
            }
        }

        binding.mainLayout.buttons.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {
                Toast.makeText(this@MainActivity, "Tab was reselected", Toast.LENGTH_SHORT).show()
                binding.mainLayout.buttons.getTabAt(tab.position)?.badge?.number = 3
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) { }
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        navController.navigate(R.id.nav_amrap)
                    }
                    1 -> {
                        navController.navigate(R.id.nav_for_time)
                    }
                    2 -> {
                        navController.navigate(R.id.nav_emom)
                    }
                    3 -> {
                        navController.navigate(R.id.nav_tabata)
                    }
                    4 -> {
                    }
                }
            }
        })
    }

    private fun setupAppBar() {
        binding.bottomAppBar.replaceMenu(R.menu.main)

        binding.bottomAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_save_favorite -> {
                    when (supportFragmentManager.currentNavigationFragment) {
                        is AmrapFragment -> {
                            val fragment =
                                supportFragmentManager.currentNavigationFragment as AmrapFragment
                            fragment.openSaveFavoriteDialog()
                            true
                        }
                        is ForTimeFragment -> {
                            val fragment =
                                supportFragmentManager.currentNavigationFragment as ForTimeFragment
                            //TODO: fragment.openSaveFavoriteDialog()
                            true
                        }
                        is EmomFragment -> {
                            val fragment =
                                supportFragmentManager.currentNavigationFragment as EmomFragment
                            true
                        }
                        is TabataFragment -> {
                            val fragment =
                                supportFragmentManager.currentNavigationFragment as TabataFragment
                            true
                        }
                        else -> false
                    }
                }
                else -> false
            }
        }
        binding.bottomAppBar.setNavigationOnClickListener {
            Toast.makeText(this, "Clicked navigation item", Toast.LENGTH_SHORT).show()
        }
    }
}
