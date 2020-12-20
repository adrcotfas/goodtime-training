package goodtime.training.wod.timer.common

import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import goodtime.training.wod.timer.GoodtimeApplication
import goodtime.training.wod.timer.R


class ResourcesHelper {
    companion object {
        val green = getColor(R.color.green_goodtime)
        val darkGreen = getColor(R.color.green_goodtime_dark)
        val darkerGreen = getColor(R.color.green_goodtime_darker)
        val red = getColor(R.color.red_goodtime)
        val darkRed = getColor(R.color.red_goodtime_dark)
        val grey200 = getColor(R.color.grey200)
        val grey500 = getColor(R.color.grey500)
        val grey800 = getColor(R.color.grey800)
        val grey1000 = getColor(R.color.grey1000)
        val grey1200 = getColor(R.color.grey1200)

        fun getColorFilter(@ColorInt color: Int) =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(color, BlendModeCompat.SRC_ATOP)

        private fun getColor(color: Int) =
            ResourcesCompat.getColor(GoodtimeApplication.getRes(), color, null)
    }
}