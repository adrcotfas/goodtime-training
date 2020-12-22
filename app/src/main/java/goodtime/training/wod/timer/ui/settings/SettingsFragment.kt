package goodtime.training.wod.timer.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(key == PreferenceHelper.SOUND_ENABLED) {
            val soundPreference = findPreference<SwitchPreferenceCompat>(PreferenceHelper.SOUND_ENABLED)
            val voicePreference = findPreference<SwitchPreferenceCompat>(PreferenceHelper.VOICE_ENABLED)
            if (!soundPreference!!.isChecked && voicePreference!!.isChecked) {
                voicePreference.isChecked = false
            }
        }
    }
}
