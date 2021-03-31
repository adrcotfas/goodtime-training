package goodtime.training.wod.timer

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
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
import com.alphelios.iap.DataWrappers
import com.alphelios.iap.IapConnector
import com.alphelios.iap.InAppEventsListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import goodtime.training.wod.timer.common.Events
import goodtime.training.wod.timer.common.ResourcesHelper
import goodtime.training.wod.timer.common.currentNavigationFragment
import goodtime.training.wod.timer.common.openStorePage
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import goodtime.training.wod.timer.databinding.ActivityMainBinding
import goodtime.training.wod.timer.ui.main.FullscreenHelper
import goodtime.training.wod.timer.ui.main.SelectFavoriteDialog
import goodtime.training.wod.timer.ui.main.WorkoutTypeFragment
import goodtime.training.wod.timer.ui.main.custom.CustomWorkoutFragment
import goodtime.training.wod.timer.ui.main.custom.SelectCustomWorkoutDialog
import goodtime.training.wod.timer.ui.stats.EditWeeklyGoalDialog
import goodtime.training.wod.timer.ui.stats.WeeklyGoalViewModel
import goodtime.training.wod.timer.ui.stats.WeeklyGoalViewModelFactory
import goodtime.training.wod.timer.ui.upgrade.UpgradeDialog
import kotlinx.android.synthetic.main.activity_main.view.*
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

    private val iapConnector: IapConnector by instance()

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
        iapConnector.getAllPurchases()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        preferenceHelper.dataStore.preferences.registerOnSharedPreferenceChangeListener(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        weeklyGoalViewModel = ViewModelProvider(this, weeklyGoalViewModelFactory).get(WeeklyGoalViewModel::class.java)

        setupWeeklyGoalButton()
        setupTopLevelButtons()
        setupStatisticsButtons()

        val toolbar = binding.contentMain.toolbar
        setContentView(binding.root)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

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
        toggleMinimalistMode(preferenceHelper.isMinimalistEnabled())

        navController.addOnDestinationChangedListener { _, destination, _ ->
            currentDestination = destination
            val isTopLevel = appBarConfiguration.topLevelDestinations.contains(destination.id)
            toolbar.setNavigationIcon(if (isTopLevel) R.drawable.ic_menu_open else R.drawable.ic_arrow_back)
            binding.drawerLayout.setDrawerLockMode(if (isTopLevel) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

            supportActionBar?.title = if (isTopLevel || destination.label == "Statistics") null else destination.label
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

        bottomNavigationView.setupWithNavController(navController)
        bottomNavigationView.setOnNavigationItemReselectedListener {
            // Nothing here to disable reselect
        }

        binding.navView.setupWithNavController(navController)

        setupDrawer()
        refreshDrawerUpgradeButton()
        setupIAP()
    }

    private fun setupTopLevelButtons() {
        startButton = binding.contentMain.startButton
        startButton.setOnClickListener { getVisibleFragment().onStartWorkout() }

        favoritesButton = binding.contentMain.buttonFavorites.root
        favoritesButton.setOnClickListener {
            if (currentDestination.label != "CustomWorkout" && !preferenceHelper.isPro()) {
                EventBus.getDefault().post(Events.Companion.ShowUpgradeDialog())
            } else {
                onFavoritesButtonClick()
            }
        }

        newCustomWorkoutButton = binding.contentMain.buttonNew.root
        newCustomWorkoutButton.setOnClickListener { onNewCustomWorkoutButtonClick() }
    }

    private fun setupStatisticsButtons() {
        filterButton = binding.contentMain.buttonFilter.root // used on the statistics page
        filterButton.setOnClickListener {
            EventBus.getDefault().post(Events.Companion.FilterButtonClickEvent())
        }
        filterButton.setOnCloseIconClickListener {
            EventBus.getDefault().post(Events.Companion.FilterClearButtonClickEvent())
        }

        addSessionButton = binding.contentMain.buttonAddSession.root
        addSessionButton.setOnClickListener {
            if (!preferenceHelper.isPro()) {
                EventBus.getDefault().post(Events.Companion.ShowUpgradeDialog())
            } else {
                EventBus.getDefault().post(Events.Companion.AddToStatisticsClickEvent())
            }
        }
    }

    private fun refreshDrawerUpgradeButton() {
        if (!preferenceHelper.isPro()) {
            binding.drawerLayout.button_pro.isVisible = true
            binding.drawerLayout.button_pro.setOnClickListener {
                EventBus.getDefault().post(Events.Companion.ShowUpgradeDialog())
            }
        } else {
            binding.drawerLayout.button_pro.isVisible = false
        }
    }

    private fun setupDrawer() {
        binding.drawerLayout.privacy_policy_button.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://goodtimetraining.policytrail.com/privacy-policy.html")
                )
            )
        }
        binding.drawerLayout.app_version_button.text =
            "version ${BuildConfig.VERSION_NAME} ${if (preferenceHelper.isPro()) "PRO" else ""}"
        binding.drawerLayout.app_version_button.setOnClickListener { openStorePage(this) }

        binding.buttonSettings.root.setOnClickListener {
            navController.navigate(MobileNavigationDirections.toSettings())
            binding.drawerLayout.closeDrawers()
        }
        binding.buttonStatistics.root.setOnClickListener {
            navController.navigate(MobileNavigationDirections.toStats())
            binding.drawerLayout.closeDrawers()
        }
    }

    private fun setupWeeklyGoalButton() {
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

        //TODO: fix and re-enable this later
        // current issue is that top right buttons don't refresh to minimalist mode
        // because of data binding or at least that's what I think

        //        if (enabled) {
        //            val startPadding = dpToPx(this, 10f).toFloat()
        //            favoritesButton.text = ""
        //            favoritesButton.chipEndPadding = 0f
        //            favoritesButton.chipStartPadding = startPadding
        //
        //            newCustomWorkoutButton.text = ""
        //            newCustomWorkoutButton.chipEndPadding = 0f
        //            newCustomWorkoutButton.chipStartPadding = startPadding
        //        } else {
        //            val padding = dpToPx(this, 4f).toFloat()
        //            favoritesButton.text = getString(R.string.favorites)
        //            favoritesButton.chipEndPadding = padding
        //            favoritesButton.chipStartPadding = padding
        //
        //            newCustomWorkoutButton.text = getString(R.string.new_title)
        //            newCustomWorkoutButton.chipEndPadding = padding
        //            newCustomWorkoutButton.chipStartPadding = padding
        //        }
    }

    private fun setupIAP() {
        iapConnector.setOnInAppEventsListener(object : InAppEventsListener {
            override fun onInAppProductsFetched(skuDetailsList: List<DataWrappers.SkuInfo>) {
                iapConnector.getAllPurchases()
            }

            override fun onNoOwnedProductsFound() {
                Log.i(TAG, "onNoPurchasedProductsFound")
                if (preferenceHelper.isPro()) {
                    handleRefund()
                }
            }

            override fun onNotOwnedProductFound(sku: String) {
                Log.i(TAG, "onNotOwnedProductFound: $sku")
                if (preferenceHelper.isPro()) {
                    handleRefund()
                }
            }

            override fun onPurchaseAcknowledged(purchase: DataWrappers.PurchaseInfo) {
                Log.i(TAG, "onPurchaseAcknowledged: $purchase")
                if (purchase.sku == "pro") {
                    if (!preferenceHelper.isPro()) {
                        preferenceHelper.setPro(true)
                        Toast.makeText(
                            this@MainActivity,
                            "Enjoy the PRO version!",
                            Toast.LENGTH_LONG
                        ).show()
                        refreshDrawerUpgradeButton()
                    }
                }
            }

            override fun onProductsPurchased(purchases: List<DataWrappers.PurchaseInfo>) {
                purchases.forEach {
                    Log.i(TAG, "onProductsPurchased: $it")
                }
            }

            override fun onError(inAppConnector: IapConnector, result: DataWrappers.BillingResponse?) {
                Log.i(TAG, "IAP error: $result")
            }
        })
    }

    private fun handleRefund() {
        Log.i(TAG, "Purchase was cancelled")
        preferenceHelper.setPro(false)
        preferenceHelper.resetPreferencesOnRefund()
        refreshDrawerUpgradeButton()
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: Events.Companion.ShowUpgradeDialog) {
        if (supportFragmentManager.findFragmentByTag("UpgradeDialog") == null) {
            UpgradeDialog.newInstance(object : UpgradeDialog.Listener {
                override fun onUpgradeButtonClicked() {
                    iapConnector.makePurchase(this@MainActivity, "pro")
                }
            }).show(supportFragmentManager, "UpgradeDialog")
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
