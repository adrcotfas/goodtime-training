package goodtime.training.wod.timer.common.preferences

import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import goodtime.training.wod.timer.ui.settings.EncryptedPreferenceDataStore

class PreferenceHelper(val dataStore: EncryptedPreferenceDataStore) {

    companion object {
        private const val IS_FIRST_RUN = "pref_is_first_run"
        private const val SHOW_DELETE_CONFIRMATION_DIALOG = "pref_show_delete_confirmation_dialog"

        const val INVALID_FAVORITE_ID = -1
        private const val AMRAP_FAVORITE_ID = "amrap_favorite_id"
        private const val FOR_TIME_FAVORITE_ID = "for_time_favorite_id"
        private const val EMOM_FAVORITE_ID = "emom_favorite_id"
        private const val HIIT_FAVORITE_ID = "hiit_favorite_id"
        private const val CUSTOM_WORKOUT_FAVORITE_NAME = "custom_workout_favorite_id"

        const val MINIMALIST_MODE_ENABLED = "pref_extra_minimalist"
        private const val THEME = "pref_theme"
        private const val SOUND_PROFILE = "pref_sound_profile"

        const val SOUND_ENABLED = "pref_sound"
        const val VOICE_ENABLED = "pref_voice"
        private const val VIBRATION_ENABLED = "pref_vibration"
        private const val MIDDLE_OF_TRAINING_NOTIFICATION_ENABLED = "pref_mid_training_notification"

        private const val PRE_WORKOUT_COUNTDOWN_SECONDS = "pref_countdown"
        private const val REMINDER_ENABLED = "pref_reminder"
        private const val LOG_INCOMPLETE = "pref_log_incomplete"
        private const val FULLSCREEN_MODE = "pref_fullscreen"
        private const val DND_MODE_ENABLED = "pref_dnd_mode"

        fun generatePreWorkoutSession(seconds: Int) : SessionSkeleton {
            return SessionSkeleton(duration = seconds, breakDuration = 0, numRounds = 0, type = SessionType.REST)
        }
    }

    fun isFirstRun() = dataStore.getBoolean(IS_FIRST_RUN, true)
    fun setIsFirstRun(state: Boolean) = dataStore.putBoolean(IS_FIRST_RUN, state)
    fun showDeleteConfirmationDialog() = dataStore.getBoolean(SHOW_DELETE_CONFIRMATION_DIALOG, true)
    fun setShowDeleteConfirmationDialog(state: Boolean) = dataStore.putBoolean(SHOW_DELETE_CONFIRMATION_DIALOG, state)
    fun setCurrentFavoriteId(sessionType: SessionType, id: Int) = dataStore.putInt(toPrefId(sessionType), id)
    fun getCurrentFavoriteId(sessionType: SessionType) = dataStore.getInt(toPrefId(sessionType), INVALID_FAVORITE_ID)
    fun setCurrentCustomWorkoutFavoriteName(name: String) = dataStore.putString(CUSTOM_WORKOUT_FAVORITE_NAME, name)
    fun getCurrentCustomWorkoutFavoriteName() = dataStore.getString(CUSTOM_WORKOUT_FAVORITE_NAME, null)

    private fun toPrefId(sessionType: SessionType): String {
        return when (sessionType) {
            SessionType.AMRAP -> AMRAP_FAVORITE_ID
            SessionType.FOR_TIME -> FOR_TIME_FAVORITE_ID
            SessionType.EMOM -> EMOM_FAVORITE_ID
            SessionType.HIIT -> HIIT_FAVORITE_ID
            else -> ""
        }
    }

    fun isMinimalistEnabled() = dataStore.getBoolean(MINIMALIST_MODE_ENABLED, false)
    fun getTheme() = dataStore.getInt(THEME, 0)
    fun getSoundProfile() = dataStore.getString(SOUND_PROFILE, "Default")
    fun isSoundEnabled() = dataStore.getBoolean(SOUND_ENABLED, true)
    fun isVoiceEnabled() = dataStore.getBoolean(VOICE_ENABLED, true)
    fun isVibrationEnabled() = dataStore.getBoolean(VIBRATION_ENABLED, false)
    fun isMidNotificationEnabled() = dataStore.getBoolean(MIDDLE_OF_TRAINING_NOTIFICATION_ENABLED, true)
    fun getPreWorkoutCountdown() = dataStore.getInt(PRE_WORKOUT_COUNTDOWN_SECONDS, 10)
    fun isReminderEnabled() = dataStore.getBoolean(REMINDER_ENABLED, false)
    fun logIncompleteSessions() = dataStore.getBoolean(LOG_INCOMPLETE, false)
    fun isFullscreenModeEnabled() = dataStore.getBoolean(FULLSCREEN_MODE, false)
    fun isDndModeEnabled() = dataStore.getBoolean(DND_MODE_ENABLED, false)
}