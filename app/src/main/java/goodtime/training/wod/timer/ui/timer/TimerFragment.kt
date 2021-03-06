package goodtime.training.wod.timer.ui.timer

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat.startForegroundService
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.ResourcesHelper
import goodtime.training.wod.timer.common.StringUtils
import goodtime.training.wod.timer.data.model.SessionType
import goodtime.training.wod.timer.data.workout.TimerState
import goodtime.training.wod.timer.databinding.FragmentTimerBinding
import goodtime.training.wod.timer.ui.timer.TimerService.Companion.FOR_TIME_COMPLETE
import goodtime.training.wod.timer.ui.timer.TimerService.Companion.START
import goodtime.training.wod.timer.ui.timer.TimerService.Companion.TOGGLE
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class TimerFragment : Fragment(), KodeinAware {
    override val kodein by closestKodein()

    private val viewModelFactory: TimerViewModelFactory by instance()
    private lateinit var viewModel: TimerViewModel
    private lateinit var binding: FragmentTimerBinding

    private val args: TimerFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(TimerViewModel::class.java)
        if (viewModel.getTimerState().value == TimerState.INACTIVE) {
            // Not the most elegant but should do for now
            // Initializing the WorkoutManager through the ViewModel because its LiveData would not be ready to be observed otherwise
            viewModel.init(args.sessions, args.name)
            val intent = IntentWithAction(requireContext(), TimerService::class.java, START)
            startForegroundService(requireContext(), intent)
        }
        setupHandleOnBackPressed()
    }

    private fun setupHandleOnBackPressed() {
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.getTimerState().value != TimerState.FINISHED) {
                    findNavController().navigate(R.id.nav_dialog_stop_workout)
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTimerBinding.inflate(layoutInflater, container, false)

        viewModel.getSecondsUntilFinished().observe(viewLifecycleOwner, { seconds ->
            onTick(seconds)
        })

        viewModel.getIsResting().observe(viewLifecycleOwner, { isResting ->
            onIsRestingChanged(isResting)
        })

        viewModel.getCurrentSessionIdx().observe(viewLifecycleOwner, {
            onCurrentSessionChanged()
        })

        viewModel.getCurrentRoundIdx().observe(viewLifecycleOwner, { currentRoundIdx ->
            binding.round.text = "${currentRoundIdx + 1}/${viewModel.getCurrentSessionTotalRounds()}"
        })

        viewModel.getTimerState().observe(viewLifecycleOwner, { timerState ->
            onTimerStateChanged(timerState)
        })

        binding.finishButton.setOnClickListener {
            val intent = IntentWithAction(requireContext(), TimerService::class.java, FOR_TIME_COMPLETE)
            startForegroundService(requireContext(), intent)
        }

        setupCounter()

        binding.timer.setOnClickListener {
            val intent = IntentWithAction(requireContext(), TimerService::class.java, TOGGLE)
            startForegroundService(requireContext(), intent)
        }
        return binding.root
    }

    private fun onCurrentSessionChanged() {
        val type = viewModel.getCurrentSessionType()
        binding.finishButton.isVisible = type == SessionType.FOR_TIME
        binding.roundCounterButtonContainer.isVisible = type == SessionType.FOR_TIME || type == SessionType.AMRAP
        binding.round.isVisible = type == SessionType.HIIT || type == SessionType.INTERVALS
        binding.round.text =
            "${viewModel.getCurrentRoundIdx().value!! + 1}/${viewModel.getCurrentSessionTotalRounds()}"
        binding.workoutImage.setImageDrawable(ResourcesHelper.getDrawableFor(type))

        refreshCounterButton()
    }

    private fun onTick(seconds: Int) {
        val type = viewModel.getCurrentSessionType()
        val total = viewModel.getCurrentSessionDuration()

        val secondsToShow =
            if (type != SessionType.FOR_TIME) seconds + 1
            else total - seconds
        binding.timer.text = StringUtils.secondsToTimerFormat(secondsToShow)
        val newProgress = (total - seconds).toFloat() / total.toFloat()
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.circleProgress?.onTick(newProgress)
        }
    }

    private fun onIsRestingChanged(isResting: Boolean) {
        val color = if (isResting) ResourcesHelper.red else ResourcesHelper.green
        val darkColor = if (isResting) ResourcesHelper.darkRed else ResourcesHelper.darkerGreen

        binding.timer.setTextColor(color)
        binding.round.setTextColor(color)
        binding.workoutImage.setColorFilter(color)

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.circleProgress?.setColor(color, darkColor)
        }
    }

    private fun onTimerStateChanged(timerState: TimerState?) {
        when (timerState) {
            TimerState.PAUSED -> {
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.timer.startAnimation(
                        AnimationUtils.loadAnimation(
                            requireContext(),
                            R.anim.blink
                        )
                    )
                }, 100)
            }
            TimerState.ACTIVE -> {
                Handler(Looper.getMainLooper()).post { binding.timer.clearAnimation() }
            }
            TimerState.FINISHED -> {
                findNavController().navigate(R.id.action_nav_timer_to_nav_finished_workout)
            }
            else -> return // do nothing
        }
    }

    private fun setupCounter() {
        binding.roundCounterButton.setOnClickListener {
            viewModel.addRound()
            refreshCounterButton()
        }
        refreshCounterButton()
    }

    private fun refreshCounterButton() {
        val numCountedRounds = viewModel.getCurrentSessionCountedRounds()
        binding.roundCounterText.isVisible = numCountedRounds != 0
        binding.roundCounterButton.drawable.alpha = if (numCountedRounds == 0) 255 else 0

        if (numCountedRounds != 0) {
            binding.roundCounterText.text = numCountedRounds.toString()
        }
    }
}
