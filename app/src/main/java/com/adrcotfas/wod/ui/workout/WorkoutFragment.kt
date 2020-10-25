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
import com.adrcotfas.wod.data.model.SessionType
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

    //TODO: pause the timer if it is not visible on screen
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
            val total = viewModel.getCurrentSessionDuration().toFloat()
            val secondsToShow = seconds + 1
            if ( viewModel.getCurrentSessionType() != SessionType.BREAK
                && (seconds == total.toInt())) {
                binding.timer.text = "" // or "REST"
            } else {
                binding.timer.text = StringUtils.secondsToTimerFormat(secondsToShow)
            }
            val newProgress = (total - seconds) / total
            binding.circleProgress.onTick(newProgress)
        })

        viewModel.isResting.observe(viewLifecycleOwner, Observer { isResting ->
            val color =
                resources.getColor(if (isResting) R.color.red_goodtime else R.color.green_goodtime)
            binding.circleProgress.setColor(isResting)
            binding.timer.setTextColor(color)
            binding.round.setTextColor(color)
            binding.workoutImage.setColorFilter(color)
        })

        viewModel.currentSessionIdx.observe(viewLifecycleOwner, Observer {
            val type = viewModel.getCurrentSessionType()
            binding.finishButton.visibility =
                if (type != SessionType.FOR_TIME) View.GONE
                else View.VISIBLE
            binding.round.visibility =
                if (type != SessionType.TABATA && type != SessionType.EMOM) View.GONE
                else View.VISIBLE

            binding.round.text = "${viewModel.currentRoundIdx.value!! + 1}/${viewModel.getTotalRounds()}"
            binding.workoutImage.setImageDrawable(resources.getDrawable(
                when (type) {
                    SessionType.AMRAP-> {
                        R.drawable.ic_infinity
                    }
                    SessionType.FOR_TIME -> {
                        R.drawable.ic_flash
                    }
                    SessionType.EMOM -> {
                        R.drawable.ic_status_goodtime
                    }
                    SessionType.TABATA -> {
                        R.drawable.ic_fire
                    }
                    SessionType.BREAK -> {
                        R.drawable.ic_break
                    }
                }, null))
        })

        viewModel.currentRoundIdx.observe(viewLifecycleOwner, Observer { currentRoundIdx ->
            binding.round.text = "${currentRoundIdx + 1}/${viewModel.getTotalRounds()}"
        })

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
