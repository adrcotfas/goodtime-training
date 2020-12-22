package goodtime.training.wod.timer.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class SettingsFragment : PreferenceFragmentCompat(), KodeinAware {

    override val kodein by closestKodein()
    private val preferenceHelper by instance<PreferenceHelper>()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = preferenceHelper.dataStore
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}
