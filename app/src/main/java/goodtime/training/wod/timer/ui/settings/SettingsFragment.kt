package goodtime.training.wod.timer.ui.settings

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
import android.text.format.DateFormat
import androidx.lifecycle.lifecycleScope
import androidx.preference.*
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.StringUtils
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import goodtime.training.wod.timer.data.db.GoodtimeDatabase
import goodtime.training.wod.timer.data.repository.AppRepository
import goodtime.training.wod.timer.ui.common.TimePickerDialogBuilder
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import java.time.LocalTime

class SettingsFragment :
    PreferenceFragmentCompat(),
    KodeinAware,
    SoundProfileDialog.Listener {

    override val kodein by closestKodein()
    private val preferenceHelper by instance<PreferenceHelper>()
    private val repo: AppRepository by instance()

    private lateinit var timePickerPreference: Preference
    private lateinit var soundProfilePreference: Preference

    companion object {
        private const val IMPORT_BACKUP_REQUEST = 0
        private const val IMPORT_BACKUP_SMART_WOD_REQUEST = 1
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = preferenceHelper.dataStore
        setPreferencesFromResource(R.xml.preferences, rootKey)
        setupReminderPreference()
        setupSoundProfilePreference()
        setupBackupButtons()
    }

    override fun onResume() {
        super.onResume()
        setupDndPreference()
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

    private fun updatePermissionPreferenceSummary(
        pref: Preference,
        notificationPolicyAccessGranted: Boolean
    ) {
        if (notificationPolicyAccessGranted) {
            pref.summary = ""
        } else {
            pref.summary = "Click to grant permission"
        }
    }

    private fun isNotificationPolicyAccessDenied(): Boolean {
        val notificationManager =
            requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return !notificationManager.isNotificationPolicyAccessGranted
    }

    private fun requestNotificationPolicyAccess() {
        if (isNotificationPolicyAccessDenied()) {
            val intent = Intent(ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivity(intent)
        }
    }

    private fun setupReminderPreference() {
        timePickerPreference = findPreference(PreferenceHelper.REMINDER_TIME)!!
        timePickerPreference.setOnPreferenceClickListener {
            val dialog = TimePickerDialogBuilder(requireContext())
                .buildDialog(preferenceHelper.getReminderTime())
            dialog.addOnPositiveButtonClickListener {
                val newValue = LocalTime.of(dialog.hour, dialog.minute).toSecondOfDay()
                preferenceHelper.setReminderTime(newValue)
                updateReminderTimeSummary()
            }
            dialog.show(parentFragmentManager, "MaterialTimePicker")
            true
        }
        updateReminderTimeSummary()
    }

    private fun updateReminderTimeSummary() {
        timePickerPreference.summary = StringUtils.secondsOfDayToTimerFormat(
            preferenceHelper.getReminderTime(), DateFormat.is24HourFormat(context)
        )
    }

    private fun setupSoundProfilePreference() {
        soundProfilePreference = findPreference(PreferenceHelper.SOUND_PROFILE)!!
        soundProfilePreference.setOnPreferenceClickListener {
            val dialog = SoundProfileDialog.newInstance(preferenceHelper.getSoundProfile(), this)
            dialog.show(childFragmentManager, "SoundProfileDialog")
            true
        }
        updateSoundProfileSummary()
    }

    private fun updateSoundProfileSummary() {
        soundProfilePreference.summary =
            resources.getStringArray(R.array.pref_sound_profile_entries)[preferenceHelper.getSoundProfile()]
    }

    override fun onSoundProfileSelected(idx: Int) {
        preferenceHelper.setSoundProfile(idx)
        updateSoundProfileSummary()
    }

    private fun setupBackupButtons() {
        findPreference<Preference>(PreferenceHelper.EXPORT_BACKUP)?.setOnPreferenceClickListener {
            BackupOperations.doExport(lifecycleScope, requireContext())
            true
        }

        findPreference<Preference>(PreferenceHelper.IMPORT_BACKUP)?.setOnPreferenceClickListener {
            val intentType = "application/octet-stream"
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = intentType
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(intentType))
            }
            startActivityForResult(intent, IMPORT_BACKUP_REQUEST)
            true
        }

        findPreference<Preference>(PreferenceHelper.IMPORT_BACKUP_SMART_WOD)?.setOnPreferenceClickListener {
            val intentType = "text/csv"
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = intentType
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(intentType))
            }
            startActivityForResult(intent, IMPORT_BACKUP_SMART_WOD_REQUEST)
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == IMPORT_BACKUP_REQUEST && data != null) {
            val uri = data.data
            if (uri != null && resultCode == Activity.RESULT_OK) {
                GoodtimeDatabase.getDatabase(requireContext())
                BackupOperations.doImport(lifecycleScope, requireContext(), uri)
            }
        } else if (requestCode == IMPORT_BACKUP_SMART_WOD_REQUEST && data != null) {
            val uri = data.data
            if (uri != null && resultCode == Activity.RESULT_OK) {
                GoodtimeDatabase.getDatabase(requireContext())
                SmartWODBackupOperations.doImportSmartWOD(lifecycleScope, repo, requireContext(), uri)
            }
        }
    }
}
