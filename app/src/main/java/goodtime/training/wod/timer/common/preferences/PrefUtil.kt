package goodtime.training.wod.timer.common.preferences

import android.content.Context
import androidx.preference.PreferenceManager
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType

class PrefUtil(private val context: Context) {

    fun isFirstRun() : Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getBoolean(IS_FIRST_RUN, true)
    }

    fun setIsFirstRun(state: Boolean) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putBoolean(IS_FIRST_RUN, state)
        editor.apply()
    }

    fun showDeleteConfirmationDialog(): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getBoolean(SHOW_DELETE_CONFIRMATION_DIALOG, true)
    }

    fun setShowDeleteConfirmationDialog(state: Boolean) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putBoolean(SHOW_DELETE_CONFIRMATION_DIALOG, state)
        editor.apply()
    }

    fun setCurrentFavoriteId(sessionType: SessionType, id: Int) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putInt(toPrefId(sessionType), id)
        editor.apply()
    }

    fun getCurrentFavoriteId(sessionType: SessionType): Int {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getInt(toPrefId(sessionType), INVALID_FAVORITE_ID)
    }

    fun setCurrentCustomWorkoutFavoriteName(name: String) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putString(CUSTOM_WORKOUT_FAVORITE_NAME, name)
        editor.apply()
    }

    fun getCurrentCustomWorkoutFavoriteName(): String? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(CUSTOM_WORKOUT_FAVORITE_NAME, null)
    }

    private fun toPrefId(sessionType: SessionType): String {
        return when (sessionType) {
            SessionType.AMRAP -> AMRAP_FAVORITE_ID
            SessionType.FOR_TIME -> FOR_TIME_FAVORITE_ID
            SessionType.EMOM -> EMOM_FAVORITE_ID
            SessionType.HIIT -> HIIT_FAVORITE_ID
            else -> ""
        }
    }

    companion object {
        private const val IS_FIRST_RUN = "pref_is_first_run"
        private const val SHOW_DELETE_CONFIRMATION_DIALOG = "pref_show_delete_confirmation_dialog"

        const val INVALID_FAVORITE_ID = -1
        private const val AMRAP_FAVORITE_ID = "amrap_favorite_id"
        private const val FOR_TIME_FAVORITE_ID = "for_time_favorite_id"
        private const val EMOM_FAVORITE_ID = "emom_favorite_id"
        private const val HIIT_FAVORITE_ID = "hiit_favorite_id"

        private const val CUSTOM_WORKOUT_FAVORITE_NAME = "custom_workout_favorite_id"


        fun generatePreWorkoutSession() : SessionSkeleton {
            //TODO: duration according to preferences
            return SessionSkeleton(duration = 5, breakDuration = 5, numRounds = 0, type = SessionType.REST)
        }
    }
}