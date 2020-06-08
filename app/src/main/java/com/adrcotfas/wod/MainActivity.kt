package com.adrcotfas.wod

import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.widget.TextView
import androidx.navigation.NavController
import com.adrcotfas.wod.common.currentNavigationFragment
import com.adrcotfas.wod.ui.amrap.AmrapFragment
import com.adrcotfas.wod.ui.emom.EmomFragment
import com.adrcotfas.wod.ui.for_time.ForTimeFragment
import com.adrcotfas.wod.ui.tabata.TabataFragment

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_amrap, R.id.nav_for_time, R.id.nav_emom, R.id.nav_tabata
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val amrapButton : TextView = findViewById(R.id.amrap_button)
        amrapButton.setOnClickListener {
            if (supportFragmentManager.currentNavigationFragment !is AmrapFragment) {
                navController.navigate(R.id.nav_amrap)
            } }

        val forTimeButton : TextView = findViewById(R.id.for_time_button)
        forTimeButton.setOnClickListener {
            if (supportFragmentManager.currentNavigationFragment !is ForTimeFragment) {
                navController.navigate(R.id.nav_for_time)
            } }

        val emomButton : TextView = findViewById(R.id.emom_button)
        emomButton.setOnClickListener {
            if (supportFragmentManager.currentNavigationFragment !is EmomFragment) {
            navController.navigate(R.id.nav_emom)
        } }

        val tabataButton : TextView = findViewById(R.id.tabata_button)
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
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun getCurrentDestinationId(): Int? {
        return navController.currentDestination?.id
    }
}
