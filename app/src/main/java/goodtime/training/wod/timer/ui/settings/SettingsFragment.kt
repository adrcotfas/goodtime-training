package goodtime.training.wod.timer.ui.settings

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
import android.text.format.DateFormat
import androidx.preference.*
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.StringUtils
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class SettingsFragment : PreferenceFragmentCompat(), KodeinAware, ReminderTimePreferenceDialogBuilder.Listener {

    override val kodein by closestKodein()
    private val preferenceHelper by instance<PreferenceHelper>()

    private lateinit var countdownPreference: SeekBarPreference
    private lateinit var timePickerPreference: Preference
    private lateinit var enableSoundPreference: SwitchPreferenceCompat
    private lateinit var enableVoicePreference: SwitchPreferenceCompat

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = preferenceHelper.dataStore
        setPreferencesFromResource(R.xml.preferences, rootKey)
        setupSoundPreference()
        setupCountdownPreference()
        setupReminderPreference()
    }

    override fun onResume() {
        super.onResume()
        setupDndPreference()
    }

    private fun setupReminderPreference() {
        timePickerPreference = findPreference(PreferenceHelper.REMINDER_TIME)!!
        timePickerPreference.setOnPreferenceClickListener {
            val dialog = ReminderTimePreferenceDialogBuilder(requireContext(), this)
                    .buildDialog(preferenceHelper.getReminderTime())
            dialog.show(parentFragmentManager, "MaterialTimePicker")
            true
        }
        updateReminderTimeSummary()
    }

    private fun setupSoundPreference() {
        enableSoundPreference = findPreference(PreferenceHelper.SOUND_ENABLED)!!
        enableVoicePreference = findPreference(PreferenceHelper.VOICE_ENABLED)!!
        enableSoundPreference.setOnPreferenceClickListener{
            if (!enableSoundPreference.isChecked && enableVoicePreference.isChecked) {
                enableVoicePreference.isChecked = false
            }
            true
        }
    }

    private fun setupCountdownPreference() {
        countdownPreference = findPreference(PreferenceHelper.PRE_WORKOUT_COUNTDOWN_SECONDS)!!
        countdownPreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, _ ->
            updateCountdownPreferenceSummary()
            true
        }
        updateCountdownPreferenceSummary()
    }

    private fun updateCountdownPreferenceSummary() {
        countdownPreference.summary = "${countdownPreference.value} seconds before starting a workout"
    }

    private fun setupDndPreference() {
        val dndPref = findPreference<CheckBoxPreference>(PreferenceHelper.DND_MODE_ENABLED)!!
        if (dndPref.isEnabled) {
            if (isNotificationPolicyAccessDenied()) {
                updatePermissionPreferenceSummary(dndPref, false)
                dndPref.isChecked = false
                dndPref.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    requestNotificationPolicyAccess()
                    false
                }
            } else {
                updatePermissionPreferenceSummary(dndPref, true)
            }
        } else {
            dndPref.summary = ""
        }
    }

    private fun updatePermissionPreferenceSummary(pref: Preference, notificationPolicyAccessGranted: Boolean) {
        if (notificationPolicyAccessGranted) {
            pref.summary = ""
        } else {
            pref.summary = "Click to grant permission"
        }
    }

    private fun isNotificationPolicyAccessDenied(): Boolean {
        val notificationManager = requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return !notificationManager.isNotificationPolicyAccessGranted
    }

    private fun requestNotificationPolicyAccess() {
        if (isNotificationPolicyAccessDenied()) {
            val intent = Intent(ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivity(intent)
        }
    }

    private fun updateReminderTimeSummary() {
        timePickerPreference.summary = StringUtils.secondsOfDayToTimerFormat(
                preferenceHelper.getReminderTime(), DateFormat.is24HourFormat(context))
    }

    override fun onReminderTimeSet(secondOfDay: Int) {
        preferenceHelper.setReminderTime(secondOfDay)
        updateReminderTimeSummary()
    }
}
