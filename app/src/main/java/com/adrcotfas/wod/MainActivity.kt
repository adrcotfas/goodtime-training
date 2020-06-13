package com.adrcotfas.wod

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.adrcotfas.wod.common.currentNavigationFragment
import com.adrcotfas.wod.databinding.ActivityMainBinding
import com.adrcotfas.wod.ui.amrap.AmrapFragment
import com.adrcotfas.wod.ui.emom.EmomFragment
import com.adrcotfas.wod.ui.for_time.ForTimeFragment
import com.adrcotfas.wod.ui.tabata.TabataFragment
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.app_bar_main.view.*

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = binding.drawerLayout.toolbar
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val hideButtons =
                destination.label == "LogFragment" ||
                        destination.label == "WorkoutFragment" ||
                        destination.label == "StopWorkoutDialog"
            binding.drawerLayout.buttons.visibility = if (hideButtons) View.GONE else View.VISIBLE

            if (destination.label == "WorkoutFragment") {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_amrap, R.id.nav_for_time, R.id.nav_emom, R.id.nav_tabata
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val amrapButton : TextView = binding.drawerLayout.amrap_button
        amrapButton.setOnClickListener {
            if (supportFragmentManager.currentNavigationFragment !is AmrapFragment) {
                navController.navigate(R.id.nav_amrap)
            } }

        val forTimeButton : TextView = binding.drawerLayout.for_time_button
        forTimeButton.setOnClickListener {
            if (supportFragmentManager.currentNavigationFragment !is ForTimeFragment) {
                navController.navigate(R.id.nav_for_time)
            } }

        val emomButton : TextView = binding.drawerLayout.emom_button
        emomButton.setOnClickListener {
            if (supportFragmentManager.currentNavigationFragment !is EmomFragment) {
            navController.navigate(R.id.nav_emom)
        } }

        val tabataButton : TextView = binding.drawerLayout.tabata_button
        tabataButton.setOnClickListener {
            if (supportFragmentManager.currentNavigationFragment !is TabataFragment) {
                navController.navigate(R.id.nav_tabata)
            } }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        val destination = navController.currentDestination

        return if (destination != null && destination.id == R.id.nav_workout) {
            navController.navigate(R.id.nav_dialog_stop_workout)
            false
        } else {
            navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
        }
    }
}
