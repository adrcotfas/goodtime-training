package com.adrcotfas.wod.ui.common

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.adrcotfas.wod.MainActivity
import com.adrcotfas.wod.data.model.SessionSkeleton
import com.adrcotfas.wod.ui.common.ui.SelectCustomWorkoutDialog
import com.adrcotfas.wod.ui.common.ui.SelectFavoriteDialog
import com.adrcotfas.wod.ui.workout.FADE_ANIMATION_DURATION
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein

abstract class WorkoutTypeFragment:
    Fragment(),
    KodeinAware,
    SelectFavoriteDialog.Listener,
    SelectCustomWorkoutDialog.Listener {

    override val kodein by closestKodein()
    private var isDurationValid = true

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onResume() {
        super.onResume()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        Log.e("This one is: ", "${this.hashCode()}")
        showContent()
    }

    override fun onPause() {
        super.onPause()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        hideContent()
    }

    abstract fun onStartWorkout()

    abstract fun getSelectedSessions() : ArrayList<SessionSkeleton>

    fun updateMainButtonsState(duration: Int) {
        if (isDurationValid && duration != 0 || !isDurationValid && duration == 0) {
            return
        }
        isDurationValid = duration != 0
        (requireActivity() as MainActivity).setStartButtonState(isDurationValid)
    }

    private fun hideContent() {
        view?.apply {
            alpha = 1f
            visibility = View.VISIBLE
            animate()
                .alpha(0f)
                .setDuration(FADE_ANIMATION_DURATION)
                .setListener(null)
        }
    }

    private fun showContent() {
        view?.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate()
                .alpha(1f)
                .setDuration(FADE_ANIMATION_DURATION)
                .setListener(null)
        }
    }
}