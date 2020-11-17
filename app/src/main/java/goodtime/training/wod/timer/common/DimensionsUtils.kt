package goodtime.training.wod.timer.common

import android.content.Context
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowManager
import kotlin.math.roundToInt

class DimensionsUtils {

    companion object {
        fun dpToPx(context: Context, dp: Float): Int {
            return (dp * context.resources.displayMetrics.density).roundToInt()
        }

        fun pxToDp(context: Context, px: Float): Float {
            return px / context.resources.displayMetrics.density
        }

        fun spToPx(context: Context, sp: Float): Float {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, sp,
                context.resources.displayMetrics
            )
        }

        fun pxToSp(context: Context, px: Float): Float {
            return px / context.resources.displayMetrics.scaledDensity
        }

        fun getScreenResolution(context: Context): Pair<Int, Int> {
            val wm =
                context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = wm.defaultDisplay
            val metrics = DisplayMetrics()
            display.getMetrics(metrics)
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            return Pair(width, height)
        }
    }
}