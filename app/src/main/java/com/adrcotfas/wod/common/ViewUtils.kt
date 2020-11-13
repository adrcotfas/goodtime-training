package com.adrcotfas.wod.common

import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import com.adrcotfas.wod.R
import com.adrcotfas.wod.data.model.SessionType

class ViewUtils {
    companion object {
        fun toDrawable(resources : Resources, type: SessionType) : Drawable {
            return ResourcesCompat.getDrawable(
                resources,
                when (type) {
                    SessionType.AMRAP -> {
                        R.drawable.ic_infinity
                    }
                    SessionType.FOR_TIME -> {
                        R.drawable.ic_flash
                    }
                    SessionType.EMOM -> {
                        R.drawable.ic_status_goodtime
                    }
                    SessionType.TABATA -> {
                        R.drawable.ic_fire2
                    }
                    SessionType.BREAK -> {
                        R.drawable.ic_break
                    }
                }, null
            )!!
        }
    }
}