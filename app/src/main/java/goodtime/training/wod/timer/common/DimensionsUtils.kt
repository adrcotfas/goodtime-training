package goodtime.training.wod.timer.common

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
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

        inline val Fragment.windowHeight: Int
            get() = getWindowProperty(WindowProperty.HEIGHT, requireContext())

        inline val Fragment.windowWidth: Int
            get() = getWindowProperty(WindowProperty.WIDTH, requireContext())

        enum class WindowProperty {
            WIDTH,
            HEIGHT
        }

        fun getWindowProperty(property: WindowProperty, context: Context): Int {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val metrics = windowManager.currentWindowMetrics
                val insets = metrics.windowInsets.getInsets(WindowInsets.Type.systemBars())
                if (property == WindowProperty.WIDTH) metrics.bounds.width() - insets.left - insets.right
                else metrics.bounds.height() - insets.bottom - insets.top
            } else {
                val displayMetrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(displayMetrics)
                return if (property == WindowProperty.WIDTH) displayMetrics.widthPixels else displayMetrics.heightPixels
            }
        }

        fun getHeight2(activity: Activity): Int {
            val windowManager = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val metrics = windowManager.currentWindowMetrics
                val insets = metrics.windowInsets.getInsets(WindowInsets.Type.systemBars())
                metrics.bounds.height() - insets.bottom - insets.top
            } else {
                // for some reason, the else of the above function fails for FinishedWorkoutFragmnet at orientation change
                //TODO: investigate later
                val view = activity.window.decorView
                val insets = WindowInsetsCompat.toWindowInsetsCompat(view.rootWindowInsets).systemWindowInsets
                activity.resources.displayMetrics.heightPixels - insets.bottom - insets.top
            }
        }
    }
}