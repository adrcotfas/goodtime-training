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

    companion object {
        private const val IS_FIRST_RUN = "pref_is_first_run"

        fun generatePreWorkoutSession() : SessionSkeleton {
            //TODO: duration according to preferences
            return SessionSkeleton(duration = 5, breakDuration = 5, numRounds = 0, type = SessionType.REST)
        }
    }
}