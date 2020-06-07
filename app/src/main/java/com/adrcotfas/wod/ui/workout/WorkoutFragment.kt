package com.adrcotfas.wod.ui.workout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.adrcotfas.wod.common.TimerUtils
import com.adrcotfas.wod.databinding.FragmentWorkoutBinding
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein

class WorkoutFragment : Fragment(), KodeinAware {
    override val kodein by closestKodein()

    private lateinit var workViewModel : WorkoutViewModel
    private lateinit var binding: FragmentWorkoutBinding

    private val args: WorkoutFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        workViewModel = ViewModelProvider(requireActivity()).get(WorkoutViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()
        //TODO: handle states and screen lock
        //TODO: make sure we have the args available
        workViewModel.init(args.sessions)
        workViewModel.startWorkout()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentWorkoutBinding.inflate(layoutInflater, container, false)
        workViewModel.currentTick.observe( viewLifecycleOwner, Observer { seconds ->
            binding.timer.text = TimerUtils.secondsToTimerFormat(seconds)
        })

        return binding.root
    }
}
