package com.adrcotfas.wod.common

import android.content.Context
import android.util.TypedValue

class DimensionsUtils {

    companion object {
        fun dpToPx(context: Context, dp: Float): Float {

            return dp * context.resources.displayMetrics.density
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
    }
}