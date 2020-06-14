package com.adrcotfas.wod.common.preferences

import android.content.Context
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.model.SessionType

class PrefUtil(private val context: Context) {

    companion object {
        fun generatePreWorkoutSession() : SessionMinimal {
            //TODO: duration according to preferences
            return SessionMinimal(duration = 5, breakDuration = 0, numRounds = 0, type = SessionType.BREAK)
        }
    }
}