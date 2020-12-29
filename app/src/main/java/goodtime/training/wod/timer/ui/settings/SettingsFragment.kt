package goodtime.training.wod.timer.ui.settings

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
import android.text.format.DateFormat
import androidx.preference.*
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.StringUtils
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import java.time.LocalTime

class SettingsFragment : PreferenceFragmentCompat(), KodeinAware, SharedPreferences.OnSharedPreferenceChangeListener {

    override val kodein by closestKodein()
    private val preferenceHelper by instance<PreferenceHelper>()

    private lateinit var countdownPreference: SeekBarPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = preferenceHelper.dataStore
        setPreferencesFromResource(R.xml.preferences, rootKey)
        preferenceHelper.dataStore.preferences.registerOnSharedPreferenceChangeListener(this)
        setupCountdownPreference()
        setupReminderPreference()
    }

    override fun onResume() {
        super.onResume()
        setupDndPreference()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when(key) {
            PreferenceHelper.SOUND_ENABLED -> {
                val soundPreference = findPreference<SwitchPreferenceCompat>(PreferenceHelper.SOUND_ENABLED)!!
                val voicePreference = findPreference<SwitchPreferenceCompat>(PreferenceHelper.VOICE_ENABLED)!!
                if (!soundPreference.isChecked && voicePreference.isChecked) {
                    voicePreference.isChecked = false
                }
            }
            PreferenceHelper.PRE_WORKOUT_COUNTDOWN_SECONDS -> updateCountdownPreferenceSummary()
            else -> {}
        }
    }

    private fun setupReminderPreference() {
        val timePickerPreference = findPreference<Preference>(PreferenceHelper.REMINDER_TIME)!!
        val is24HourFormat = DateFormat.is24HourFormat(context)
        timePickerPreference.summary = StringUtils.secondsOfDayToTimerFormat(preferenceHelper.getReminderTime(), is24HourFormat)

        timePickerPreference.setOnPreferenceClickListener {
            val time : LocalTime = LocalTime.ofSecondOfDay(preferenceHelper.getReminderTime().toLong())
            val dialog = MaterialTimePicker.Builder()
                    .setHour(time.hour)
                    .setMinute(time.minute)
                    .setTimeFormat(if (is24HourFormat) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
                    .setInputMode(INPUT_MODE_CLOCK)
                    .build()
            dialog.addOnPositiveButtonClickListener{
                val newValue = LocalTime.of(dialog.hour, dialog.minute).toSecondOfDay()
                preferenceHelper.setReminderTime(newValue)
                timePickerPreference.summary = StringUtils.secondsOfDayToTimerFormat(newValue, is24HourFormat)
            }
            dialog.show(parentFragmentManager, "MaterialTimePicker")
            true
        }
    }

    private fun setupCountdownPreference() {
        countdownPreference = findPreference(PreferenceHelper.PRE_WORKOUT_COUNTDOWN_SECONDS)!!
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
}
