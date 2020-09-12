package com.adrcotfas.wod.ui.workout

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import com.adrcotfas.wod.R
import com.adrcotfas.wod.common.StringUtils
import com.adrcotfas.wod.common.notifications.NotificationHelper
import com.adrcotfas.wod.data.workout.TimerState
import com.adrcotfas.wod.databinding.FragmentWorkoutBinding
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class WorkoutFragment : Fragment(), KodeinAware {
    override val kodein by closestKodein()

    private val viewModelFactory : WorkoutViewModelFactory by instance()
    private lateinit var viewModel : WorkoutViewModel
    private lateinit var binding: FragmentWorkoutBinding

    private val args: WorkoutFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(WorkoutViewModel::class.java)
        //TODO: handle states and screen lock
        //TODO: make sure we have the args available
        if (viewModel.state.value == TimerState.INACTIVE) {
            viewModel.init(args.sessions)
            viewModel.startWorkout()
        }
    }

    override fun onPause() {
        super.onPause()
        val state = viewModel.state.value
        if (state != TimerState.INACTIVE && state != TimerState.FINISHED) {
            NotificationHelper.showNotification(requireContext())
        }
    }

    override fun onResume() {
        super.onResume()
        NotificationHelper.hideNotification(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWorkoutBinding.inflate(layoutInflater, container, false)
        viewModel.secondsUntilFinished.observe( viewLifecycleOwner, Observer { seconds ->
            binding.timer.text = StringUtils.secondsToTimerFormat(seconds)
        })

        viewModel.currentSessionIdx.observe(viewLifecycleOwner, Observer {
            binding.workoutDuration.text = viewModel.getDurationString()
            binding.workoutType.text = viewModel.getCurrentSessionType().toString()
            binding.round.text = "${viewModel.currentRoundIdx.value!! + 1} / ${viewModel.getTotalRounds()}"
        })

        viewModel.currentRoundIdx.observe(viewLifecycleOwner, Observer { currentRoundIdx ->
            binding.round.text = "${currentRoundIdx + 1} / ${viewModel.getTotalRounds()}"
        })

        binding.fabFinish.setOnClickListener {
            NavHostFragment.findNavController(this@WorkoutFragment)
                .navigate(R.id.nav_dialog_stop_workout)
        }

        // TODO: observe for session finished and cancel the StopWorkoutDialog
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                NavHostFragment.findNavController(this@WorkoutFragment)
                    .navigate(R.id.nav_dialog_stop_workout)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }
}
