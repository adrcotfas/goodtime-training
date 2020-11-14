package goodtime.training.wod.timer.common

import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.data.model.SessionType

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
                    SessionType.REST -> {
                        R.drawable.ic_break
                    }
                }, null
            )!!
        }
    }
}