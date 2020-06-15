package com.adrcotfas.wod.common.preferences

import android.content.Context
import androidx.preference.PreferenceManager
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.model.SessionType

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

        fun generatePreWorkoutSession() : SessionMinimal {
            //TODO: duration according to preferences
            return SessionMinimal(duration = 5, breakDuration = 0, numRounds = 0, type = SessionType.BREAK)
        }
    }
}