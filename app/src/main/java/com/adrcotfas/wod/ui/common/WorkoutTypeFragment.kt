package com.adrcotfas.wod.ui.common

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.adrcotfas.wod.common.preferences.PrefUtil
import com.adrcotfas.wod.common.sessionsToString
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.ui.main.MainFragmentDirections
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein

abstract class WorkoutTypeFragment : Fragment(), KodeinAware {

    override val kodein by closestKodein()

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
}