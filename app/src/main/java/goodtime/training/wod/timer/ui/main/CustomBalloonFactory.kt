package goodtime.training.wod.timer.ui.main

import android.content.Context
import android.view.Gravity
import androidx.lifecycle.LifecycleOwner
import com.skydoves.balloon.*
import com.skydoves.balloon.overlay.BalloonOverlayCircle
import goodtime.training.wod.timer.R

class CustomBalloonFactory {
    companion object {
        private fun create(
            context: Context,
            lifecycle: LifecycleOwner,
            text: String,
            enableArrow: Boolean = false,
            withTopArrow: Boolean = false,
            arrowPosition: Float = 0.0f
        ): Balloon {
            return createBalloon(context) {
                setIsVisibleArrow(enableArrow)
                setArrowOrientation(if (withTopArrow) ArrowOrientation.TOP else ArrowOrientation.BOTTOM)
                setArrowPosition(arrowPosition)
                setWidth(BalloonSizeSpec.WRAP)
                setHeight(BalloonSizeSpec.WRAP)
                setWidthRatio(1f)
                setDismissWhenTouchOutside(false)
                setPadding(12)
                setAlpha(0.9f)
                setText(text)
                setTextSize(16f)
                setMarginLeft(24)
                setMarginRight(24)
                setTextGravity(Gravity.START)
                setTextColorResource(android.R.color.black)
                setBackgroundColorResource(android.R.color.white)
                setDismissWhenClicked(true)
                setBalloonAnimation(BalloonAnimation.FADE)
                setLifecycleOwner(lifecycle)
                setOverlayShape(BalloonOverlayCircle(0f))
                setOverlayColorResource(R.color.white_30percent)
                setIsVisibleOverlay(true)
            }
        }

        fun create(context: Context, lifecycle: LifecycleOwner, text: String): Balloon =
            create(
                context,
                lifecycle,
                text,
                false)

        fun create(context: Context, lifecycle: LifecycleOwner, text: String, withTopArrow: Boolean,
                   arrowPosition: Float) =
            create(
                context,
                lifecycle,
                text,
                true, withTopArrow, arrowPosition)
    }
}