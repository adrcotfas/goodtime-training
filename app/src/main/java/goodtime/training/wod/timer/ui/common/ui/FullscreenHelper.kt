package goodtime.training.wod.timer.ui.common.ui

import android.os.Handler
import android.view.MotionEvent
import android.view.View

internal class FullscreenHelper(
    private val view: View
) {
    private var isVisible = true
    private val hideHandler = Handler()
    private val hidePart2Runnable = Runnable {
        view.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                )
    }

    private val runnable = Runnable { hide() }
    private fun toggle() {
        if (isVisible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        isVisible = false
        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.postDelayed(
            hidePart2Runnable,
            UI_ANIMATION_DELAY.toLong()
        )
    }

    private fun show() {
        // Show the system bar
        view.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        isVisible = true

        // Schedule a runnable to display UI elements after a delay
        hideHandler.removeCallbacks(hidePart2Runnable)
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private fun delayedHide() {
        hideHandler.removeCallbacks(runnable)
        hideHandler.postDelayed(
            runnable,
            AUTO_HIDE_DELAY_MILLIS.toLong()
        )
    }

    fun disable() {
        view.setOnClickListener(null)
        view.setOnTouchListener(null)
        hideHandler.removeCallbacks(runnable)
        hideHandler.removeCallbacks(hidePart2Runnable)
        show()
    }

    companion object {
        private const val AUTO_HIDE_DELAY_MILLIS = 3000
        private const val UI_ANIMATION_DELAY = 300
    }

    fun enable() {
        view.setOnClickListener { toggle() }
        view.setOnTouchListener { _: View?, _: MotionEvent? ->
            delayedHide()
            false
        }
        hide()
    }
}