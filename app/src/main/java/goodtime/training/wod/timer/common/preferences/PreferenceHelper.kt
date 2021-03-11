package goodtime.training.wod.timer.common.preferences

import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import goodtime.training.wod.timer.ui.settings.PreferenceDataStore
import java.time.DayOfWeek
import java.time.LocalTime

class PreferenceHelper(val dataStore: PreferenceDataStore) {

    companion object {
        private const val IS_FIRST_RUN = "pref_is_first_run"
        private const val SHOW_DELETE_CONFIRMATION_DIALOG = "pref_show_delete_confirmation_dialog"

        const val INVALID_FAVORITE_ID = -1L
        private const val AMRAP_FAVORITE_ID = "amrap_favorite_id"
        private const val FOR_TIME_FAVORITE_ID = "for_time_favorite_id"
        private const val INTERVALS_FAVORITE_ID = "intervals_favorite_id"
        private const val HIIT_FAVORITE_ID = "hiit_favorite_id"
        private const val CUSTOM_WORKOUT_FAVORITE_NAME = "custom_workout_favorite_id"

        const val MINIMALIST_MODE_ENABLED = "pref_extra_minimalist"
        private const val THEME = "pref_theme"
        const val SOUND_PROFILE = "pref_sound_profile"

        const val SOUND_ENABLED = "pref_sound"
        const val VOICE_ENABLED = "pref_voice"
        private const val VIBRATION_ENABLED = "pref_vibration"
        const val FLASH_ENABLED = "pref_flash"
        private const val MIDDLE_OF_TRAINING_NOTIFICATION_ENABLED = "pref_mid_training_notification"
        const val PRE_WORKOUT_COUNTDOWN_SECONDS = "pref_countdown"

        const val REMINDER_TIME = "pref_reminder_time"
        const val REMINDER_DAYS = "pref_reminder_days"

        private const val LOG_INCOMPLETE = "pref_log_incomplete"
        private const val FULLSCREEN_MODE = "pref_fullscreen"
        const val DND_MODE_ENABLED = "pref_dnd_mode"

        private const val SHOW_MAIN_BALLOONS = "show_main_balloons"
        private const val SHOW_FOR_TIME_BALLOONS = "show_for_time_balloons"
        private const val SHOW_INTERVALS_BALLOONS = "show_intervals_balloons"
        private const val SHOW_HIIT_BALLOONS = "show_hiit_balloons"
        private const val SHOW_CUSTOM_BALLOONS = "show_custom_balloons"

        private const val KILLED_WHILE_TRAINING = "killed_while_training"

        const val EXPORT_BACKUP = "pref_export_backup"
        const val IMPORT_BACKUP = "pref_import_backup"
        const val IMPORT_BACKUP_SMART_WOD = "pref_import_backup_smart_wod"

        fun generatePreWorkoutSession(seconds: Int): SessionSkeleton {
            return SessionSkeleton(duration = seconds, breakDuration = 0, numRounds = 0, type = SessionType.REST)
        }
    }

    fun isFirstRun() = dataStore.getBoolean(IS_FIRST_RUN, true)
    fun setIsFirstRun(state: Boolean) = dataStore.putBoolean(IS_FIRST_RUN, state)
    fun showDeleteConfirmationDialog() = dataStore.getBoolean(SHOW_DELETE_CONFIRMATION_DIALOG, true)
    fun setShowDeleteConfirmationDialog(state: Boolean) = dataStore.putBoolean(SHOW_DELETE_CONFIRMATION_DIALOG, state)
    fun setCurrentFavoriteId(sessionType: SessionType, id: Long) = dataStore.putLong(toPrefId(sessionType), id)
    fun getCurrentFavoriteId(sessionType: SessionType) = dataStore.getLong(toPrefId(sessionType), INVALID_FAVORITE_ID)
    fun setCurrentCustomWorkoutFavoriteName(name: String) = dataStore.putString(CUSTOM_WORKOUT_FAVORITE_NAME, name)
    fun getCurrentCustomWorkoutFavoriteName() = dataStore.getString(CUSTOM_WORKOUT_FAVORITE_NAME, null)

    private fun toPrefId(sessionType: SessionType): String {
        return when (sessionType) {
            SessionType.AMRAP -> AMRAP_FAVORITE_ID
            SessionType.FOR_TIME -> FOR_TIME_FAVORITE_ID
            SessionType.INTERVALS -> INTERVALS_FAVORITE_ID
            SessionType.HIIT -> HIIT_FAVORITE_ID
            else -> ""
        }
    }

    fun isMinimalistEnabled() = dataStore.getBoolean(MINIMALIST_MODE_ENABLED, false)
    fun getTheme() = dataStore.getInt(THEME, 0)

    fun setSoundProfile(idx: Int) = dataStore.putInt(SOUND_PROFILE, idx)
    fun getSoundProfile() = dataStore.getInt(SOUND_PROFILE, 0)

    fun isSoundEnabled() = dataStore.getBoolean(SOUND_ENABLED, true)
    fun isVoiceEnabled() = dataStore.getBoolean(VOICE_ENABLED, true)
    fun isVibrationEnabled() = dataStore.getBoolean(VIBRATION_ENABLED, false)
    fun isFlashEnabled() = dataStore.getBoolean(FLASH_ENABLED, false)
    fun isMidNotificationEnabled() = dataStore.getBoolean(MIDDLE_OF_TRAINING_NOTIFICATION_ENABLED, true)
    fun getPreWorkoutCountdown() = dataStore.getInt(PRE_WORKOUT_COUNTDOWN_SECONDS, 10)
    fun logIncompleteSessions() = dataStore.getBoolean(LOG_INCOMPLETE, false)
    fun isFullscreenModeEnabled() = dataStore.getBoolean(FULLSCREEN_MODE, false)
    fun isDndModeEnabled() = dataStore.getBoolean(DND_MODE_ENABLED, false)

    fun isReminderEnabled() = getReminderDays().contains(true)
    fun isReminderEnabledFor(dayOfWeek: DayOfWeek) = getReminderDays()[dayOfWeek.ordinal]
    fun getReminderDays() = dataStore.getBooleanArray(REMINDER_DAYS, 7)
    fun getReminderTime() = dataStore.getInt(REMINDER_TIME, LocalTime.of(9, 0).toSecondOfDay())
    fun setReminderTime(secondOfDay: Int) = dataStore.putInt(REMINDER_TIME, secondOfDay)

    fun showMainBalloons() = dataStore.getBoolean(SHOW_MAIN_BALLOONS, true)
    fun showForTimeBalloons() = dataStore.getBoolean(SHOW_FOR_TIME_BALLOONS, true)
    fun showIntervalsBalloons() = dataStore.getBoolean(SHOW_INTERVALS_BALLOONS, true)
    fun showHiitBalloons() = dataStore.getBoolean(SHOW_HIIT_BALLOONS, true)
    fun showCustomBalloons() = dataStore.getBoolean(SHOW_CUSTOM_BALLOONS, true)

    fun setMainBalloons(enabled: Boolean) = dataStore.putBoolean(SHOW_MAIN_BALLOONS, enabled)
    fun setForTimeBalloons(enabled: Boolean) = dataStore.putBoolean(SHOW_FOR_TIME_BALLOONS, enabled)
    fun setIntervalsBalloons(enabled: Boolean) = dataStore.putBoolean(SHOW_INTERVALS_BALLOONS, enabled)
    fun setHiitBalloons(enabled: Boolean) = dataStore.putBoolean(SHOW_HIIT_BALLOONS, enabled)
    fun setCustomBalloons(enabled: Boolean) = dataStore.putBoolean(SHOW_CUSTOM_BALLOONS, enabled)

    /**
     * Detect when the user closed the app from the recent apps list while the service is in foreground.
     * When the notification is clicked, the user should get back to the TimerFragment
     */
    fun setKilledDuringWorkout(killed: Boolean) = dataStore.putBoolean(KILLED_WHILE_TRAINING, killed)
    fun wasKilledDuringWorkout() = dataStore.getBoolean(KILLED_WHILE_TRAINING, false)

    fun setBalloons(enabled: Boolean) = run {
        setMainBalloons(enabled)
        setForTimeBalloons(enabled)
        setIntervalsBalloons(enabled)
        setHiitBalloons(enabled)
        setCustomBalloons(enabled)
    }

    //TODO: implement these after internal testing
    fun setAskedForReviewTime(currentTimeMillis: Long) {
    }

    fun getAskedForReviewTime() : Long {
        return 0
    }

    fun resetCompletedWorkoutsForReview() {

    }

    fun incrementCompletedWorkoutsForReview() {

    }

    fun getCompletedWorkoutsForReview(): Int {
        return 0
    }
}