package com.adrcotfas.wod.ui.common

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.adrcotfas.wod.MainActivity
import com.adrcotfas.wod.common.preferences.PrefUtil
import com.adrcotfas.wod.common.sessionsToString
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.ui.common.ui.SaveFavoriteDialog
import com.adrcotfas.wod.ui.main.MainFragmentDirections
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein

abstract class WorkoutTypeFragment : Fragment(), KodeinAware, SaveFavoriteDialog.Listener {

    override val kodein by closestKodein()
    private var isDurationValid = true

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onResume() {
        super.onResume()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        Log.e("This one is: ", "${this.hashCode()}")
    }

    override fun onPause() {
        super.onPause()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
    }

    fun onStartWorkout() {
        val action = MainFragmentDirections.startWorkoutAction(
            sessionsToString(PrefUtil.generatePreWorkoutSession(),  getSelectedSession())
        )
        view?.findNavController()?.navigate(action)
    }

    abstract fun getSelectedSession() : SessionMinimal

    fun updateMainButtonsState(duration: Int) {
        if (isDurationValid && duration != 0 || !isDurationValid && duration == 0) {
            return
        }
        isDurationValid = duration != 0
        (requireActivity() as MainActivity).setStartButtonState(isDurationValid)
    }
}