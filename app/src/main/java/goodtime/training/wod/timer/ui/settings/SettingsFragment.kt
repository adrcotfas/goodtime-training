package goodtime.training.wod.timer.ui.settings

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class SettingsFragment : PreferenceFragmentCompat(), KodeinAware, SharedPreferences.OnSharedPreferenceChangeListener {

    override val kodein by closestKodein()
    private val preferenceHelper by instance<PreferenceHelper>()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = preferenceHelper.dataStore
        setPreferencesFromResource(R.xml.preferences, rootKey)
        preferenceHelper.dataStore.preferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        setupDndPreference()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(key == PreferenceHelper.SOUND_ENABLED) {
            val soundPreference = findPreference<SwitchPreferenceCompat>(PreferenceHelper.SOUND_ENABLED)
            val voicePreference = findPreference<SwitchPreferenceCompat>(PreferenceHelper.VOICE_ENABLED)
            if (!soundPreference!!.isChecked && voicePreference!!.isChecked) {
                voicePreference.isChecked = false
            }
        }
    }

    private fun setupDndPreference() {
        val dndPref = findPreference<CheckBoxPreference>(PreferenceHelper.DND_MODE_ENABLED)!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isNotificationPolicyAccessDenied()) {
            updateDndSummary(dndPref, false)
            dndPref.isChecked = false
            dndPref.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                requestNotificationPolicyAccess()
                false
            }
        } else {
            updateDndSummary(dndPref, true)
        }
    }

    private fun isNotificationPolicyAccessDenied(): Boolean {
        val notificationManager = requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return !notificationManager.isNotificationPolicyAccessGranted
    }
    private fun updateDndSummary(pref: CheckBoxPreference, notificationPolicyAccessGranted: Boolean) {
        if (notificationPolicyAccessGranted) {
            pref.summary = ""
        } else {
            pref.summary = "Click to grant permission"
        }
    }

    private fun requestNotificationPolicyAccess() {
        if (isNotificationPolicyAccessDenied()) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivity(intent)
        }
    }
}
