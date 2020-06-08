package com.adrcotfas.wod.ui.workout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.adrcotfas.wod.common.TimerUtils
import com.adrcotfas.wod.common.notifications.NotificationHelper
import com.adrcotfas.wod.data.workout.TimerState
import com.adrcotfas.wod.databinding.FragmentWorkoutBinding
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class WorkoutFragment : Fragment(), KodeinAware {
    override val kodein by closestKodein()

    private val workoutManager : WorkoutManager by instance()
    private lateinit var binding: FragmentWorkoutBinding

    private val args: WorkoutFragmentArgs by navArgs()

    override fun onPause() {
        super.onPause()
        val state = workoutManager.state.value
        if (state != TimerState.INACTIVE && state != TimerState.FINISHED) {
            NotificationHelper.showNotification(requireContext())
        }
    }

    override fun onResume() {
        super.onResume()
        //TODO: handle states and screen lock
        //TODO: make sure we have the args available
        if (workoutManager.state.value == TimerState.INACTIVE) {
            workoutManager.init(args.sessions)
            workoutManager.startWorkout()
        }
        NotificationHelper.hideNotification(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWorkoutBinding.inflate(layoutInflater, container, false)
        workoutManager.currentTick.observe( viewLifecycleOwner, Observer { seconds ->
            binding.timer.text = TimerUtils.secondsToTimerFormat(seconds)
        })
        return binding.root
    }
}
