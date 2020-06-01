package com.adrcotfas.wod.common.preferences

import android.content.Context
import androidx.preference.PreferenceManager
import com.adrcotfas.wod.data.model.SessionMinimal
import com.google.gson.Gson

class PrefUtil(private val context: Context) {
    companion object {
        private const val WORKOUT_STATE_ID = "goodtime.training.WORKOUT_STATE_ID"
        private const val SESSION_LIST = "goodtime.training.SESSION_LIST"
    }

    fun getState() : Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getBoolean(WORKOUT_STATE_ID, false)
    }

    fun setState(state: Boolean) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putBoolean(WORKOUT_STATE_ID, state)
        editor.apply()
    }

    //TODO: replace these two functions with navigation with safeargs between Home and Workout fragments
    fun getSessionList() : String {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(SESSION_LIST, "").toString()
    }

    fun setSessionList(vararg sessions: SessionMinimal) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        val gson = Gson()
        editor.putString(SESSION_LIST, gson.toJson(sessions))
        editor.apply()
    }

}