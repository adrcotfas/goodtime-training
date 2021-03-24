package goodtime.training.wod.timer.ui.settings

import android.app.Activity
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
import android.text.format.DateFormat
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.*
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import goodtime.training.wod.timer.BuildConfig
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.DeviceInfo
import goodtime.training.wod.timer.common.Events
import goodtime.training.wod.timer.common.StringUtils
import goodtime.training.wod.timer.common.openStorePage
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import goodtime.training.wod.timer.common.preferences.PreferenceHelper.Companion.UNLOCK_FEATURES
import goodtime.training.wod.timer.data.db.GoodtimeDatabase
import goodtime.training.wod.timer.data.repository.AppRepository
import goodtime.training.wod.timer.ui.common.TimePickerDialogBuilder
import org.greenrobot.eventbus.EventBus
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import java.time.LocalTime

class SettingsFragment :
    PreferenceFragmentCompat(),
    KodeinAware {

    override val kodein by closestKodein()
    private val preferenceHelper by instance<PreferenceHelper>()
    private val repo: AppRepository by instance()

    private lateinit var timePickerPref: Preference
    private lateinit var soundProfilePref: Preference
    private lateinit var voiceProfilePref: Preference
    private lateinit var unlockFeaturesPref: Preference

    companion object {
        private const val IMPORT_BACKUP_REQUEST = 0
        private const val IMPORT_BACKUP_SMART_WOD_REQUEST = 1
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = preferenceHelper.dataStore
        setPreferencesFromResource(R.xml.preferences, rootKey)

        setupReminderPreference()
        setupSoundProfilePreference()
        setupProVersionPrefs()
        setupHelpAndFeedbackSection()
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
        timePickerPref = findPreference(PreferenceHelper.REMINDER_TIME)!!
        timePickerPref.setOnPreferenceClickListener {
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
        timePickerPref.summary = StringUtils.secondsOfDayToTimerFormat(
            preferenceHelper.getReminderTime(), DateFormat.is24HourFormat(context)
        )
    }

    private fun setupSoundProfilePreference() {
        soundProfilePref = findPreference(PreferenceHelper.SOUND_PROFILE)!!
        soundProfilePref.setOnPreferenceClickListener {
            val dialog = SoundProfileDialog.newInstance(preferenceHelper.getSoundProfile(), object : SoundProfileDialog.Listener {
                override fun onSoundProfileSelected(idx: Int) {
                    preferenceHelper.setSoundProfile(idx)
                    updateSoundProfileSummary()
                }
            })
            dialog.show(childFragmentManager, "SoundProfileDialog")
            true
        }
        updateSoundProfileSummary()

        voiceProfilePref = findPreference(PreferenceHelper.VOICE_PROFILE)!!
        voiceProfilePref.setOnPreferenceClickListener {
            val dialog = SoundProfileDialog.newInstance(preferenceHelper.getVoiceProfile(), object : SoundProfileDialog.Listener {
                override fun onSoundProfileSelected(idx: Int) {
                    preferenceHelper.setVoiceProfile(idx)
                    updateVoiceProfileSummary()
                }
            }, true)
            dialog.show(childFragmentManager, "VoiceProfileDialog")
            true
        }
        updateVoiceProfileSummary()
    }

    private fun updateSoundProfileSummary() {
        soundProfilePref.summary =
            resources.getStringArray(R.array.pref_sound_profile_entries)[preferenceHelper.getSoundProfile()]
    }

    private fun updateVoiceProfileSummary() {
        voiceProfilePref.summary =
            resources.getStringArray(R.array.pref_voice_profile_entries)[preferenceHelper.getVoiceProfile()]
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

    private fun setupHelpAndFeedbackSection() {
        findPreference<Preference>("tutorial_button")!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            preferenceHelper.setBalloons(true)
            findNavController().popBackStack()
            true
        }
        findPreference<Preference>("feedback_button")!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            openFeedback()
            true
        }
        findPreference<Preference>("rate_this_app_button")!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            openStorePage(requireContext())
            true
        }
        findPreference<Preference>("open_source_licences")!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            startActivity(Intent(requireContext(), OssLicensesMenuActivity::class.java))
            true
        }
    }

    private fun openFeedback() {
        val email = Intent(Intent.ACTION_SENDTO)
        email.data = Uri.Builder().scheme("mailto").build()
        email.putExtra(Intent.EXTRA_EMAIL, arrayOf("goodtime-app@googlegroups.com"))
        email.putExtra(Intent.EXTRA_SUBJECT, "[Goodtime Training] Feedback")
        email.putExtra(
            Intent.EXTRA_TEXT, "\nMy device info: \n" + DeviceInfo.deviceInfo + "\nApp version: " + BuildConfig.VERSION_NAME
                    + if (preferenceHelper.isPro()) "PRO" else ""
        )
        try {
            startActivity(Intent.createChooser(email, "Send Feedback"))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "There are no email clients installed.", Toast.LENGTH_SHORT).show()
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

    private fun setupProVersionPrefs() {

        val vibrationPref = findPreference<SwitchPreferenceCompat>(PreferenceHelper.VIBRATION_ENABLED)!!
        val flashPref = findPreference<SwitchPreferenceCompat>(PreferenceHelper.FLASH_ENABLED)!!
        val minimalistModePref = findPreference<CheckBoxPreference>(PreferenceHelper.MINIMALIST_MODE_ENABLED)!!
        val fullscreenModePref = findPreference<CheckBoxPreference>(PreferenceHelper.FULLSCREEN_MODE)!!
        val dndPref = findPreference<CheckBoxPreference>(PreferenceHelper.DND_MODE_ENABLED)!!
        val logIncompletePref = findPreference<CheckBoxPreference>(PreferenceHelper.LOG_INCOMPLETE)!!
        val exportBackupPref = findPreference<Preference>(PreferenceHelper.EXPORT_BACKUP)!!
        val importBackupPref = findPreference<Preference>(PreferenceHelper.IMPORT_BACKUP)!!
        val importBackupSmartWODPref = findPreference<Preference>(PreferenceHelper.IMPORT_BACKUP_SMART_WOD)!!
        val voteForNextFeaturesPref = findPreference<Preference>("vote_for_next_features")!!

        unlockFeaturesPref = findPreference(UNLOCK_FEATURES)!!

        if (preferenceHelper.isPro()) {
            unlockFeaturesPref.isVisible = false
            vibrationPref.isEnabled = true
            flashPref.isEnabled = true
            soundProfilePref.isEnabled = true
            voiceProfilePref.isEnabled = true
            minimalistModePref.isEnabled = true
            fullscreenModePref.isEnabled = true
            dndPref.isEnabled = true
            logIncompletePref.isEnabled = true
            exportBackupPref.isEnabled = true
            importBackupPref.isEnabled = true
            importBackupSmartWODPref.isEnabled = true
            voteForNextFeaturesPref.isEnabled = true
            voteForNextFeaturesPref.setOnPreferenceClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://forms.gle/W9XRcYAjoxJsU9j78")
                    )
                )
                true
            }
            setupBackupButtons()
        } else {
            unlockFeaturesPref.isVisible = true
            unlockFeaturesPref.setOnPreferenceClickListener {
                EventBus.getDefault().post(Events.Companion.ShowUpgradeDialog())
                true
            }
            vibrationPref.isEnabled = false
            flashPref.isEnabled = false
            soundProfilePref.isEnabled = false
            voiceProfilePref.isEnabled = false
            minimalistModePref.isEnabled = false
            fullscreenModePref.isEnabled = false
            dndPref.isEnabled = false
            logIncompletePref.isEnabled = false
            exportBackupPref.isEnabled = false
            importBackupPref.isEnabled = false
            importBackupSmartWODPref.isEnabled = false
            voteForNextFeaturesPref.isEnabled = false
        }
    }
}
