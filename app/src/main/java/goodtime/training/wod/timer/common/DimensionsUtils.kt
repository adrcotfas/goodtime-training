package goodtime.training.wod.timer.common

import android.content.Context
import android.os.Build
import android.util.TypedValue
import android.view.WindowInsets
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
            get() {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val metrics = requireActivity().windowManager.currentWindowMetrics
                    val insets = metrics.windowInsets.getInsets(WindowInsets.Type.systemBars())
                    metrics.bounds.height() - insets.bottom - insets.top
                } else {
                    val view = requireActivity().window.decorView
                    val insets = WindowInsetsCompat.toWindowInsetsCompat(view.rootWindowInsets).systemWindowInsets
                    resources.displayMetrics.heightPixels - insets.bottom - insets.top
                }
            }

        inline val Fragment.windowWidth: Int
            get() {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val metrics = requireActivity().windowManager.currentWindowMetrics
                    val insets = metrics.windowInsets.getInsets(WindowInsets.Type.systemBars())
                    metrics.bounds.width() - insets.left - insets.right
                } else {
                    val view = requireActivity().window.decorView
                    val insets = WindowInsetsCompat.toWindowInsetsCompat(view.rootWindowInsets).systemWindowInsets
                    resources.displayMetrics.widthPixels - insets.left - insets.right
                }
            }
    }
}