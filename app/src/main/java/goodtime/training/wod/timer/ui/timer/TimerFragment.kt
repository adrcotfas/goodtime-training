package goodtime.training.wod.timer.ui.timer

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.DimensionsUtils
import goodtime.training.wod.timer.common.ResourcesHelper
import goodtime.training.wod.timer.common.StringUtils
import goodtime.training.wod.timer.common.notifications.NotificationHelper
import goodtime.training.wod.timer.common.toInt
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import goodtime.training.wod.timer.data.workout.TimerState
import goodtime.training.wod.timer.databinding.FragmentWorkoutBinding
import nl.dionsegijn.konfetti.emitters.StreamEmitter
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class TimerFragment : Fragment(), KodeinAware {
    override val kodein by closestKodein()

    private val viewModelFactory : TimerViewModelFactory by instance()
    private lateinit var viewModel : TimerViewModel
    private lateinit var binding: FragmentWorkoutBinding

    private val args: TimerFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(TimerViewModel::class.java)

        if (viewModel.timerState.value == TimerState.INACTIVE) {
            viewModel.init(args.sessions)
            viewModel.startWorkout()
            viewModel.sessionToAdd.name = args.name
        }

        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.timerState.value != TimerState.FINISHED) {
                    NavHostFragment.findNavController(this@TimerFragment)
                            .navigate(R.id.nav_dialog_stop_workout)
                } else {
                    viewModel.timerState.value = TimerState.INACTIVE
                    NavHostFragment.findNavController(this@TimerFragment)
                            .popBackStack()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onPause() {
        super.onPause()
        val state = viewModel.timerState.value
        if (state != TimerState.INACTIVE && state != TimerState.FINISHED) {
            NotificationHelper.showNotification(requireContext())
        }
    }

    override fun onResume() {
        super.onResume()
        NotificationHelper.hideNotification(requireContext())
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentWorkoutBinding.inflate(layoutInflater, container, false)
        viewModel.secondsUntilFinished.observe(viewLifecycleOwner, { seconds ->
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
        })

        viewModel.isResting.observe(viewLifecycleOwner, { isResting ->
            val color = if (isResting) ResourcesHelper.red else ResourcesHelper.green
            val darkColor = if (isResting) ResourcesHelper.darkRed else ResourcesHelper.darkerGreen

            binding.timer.setTextColor(color)
            binding.round.setTextColor(color)
            binding.workoutImage.setColorFilter(color)

            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                binding.circleProgress?.setColor(color, darkColor)
            }
        })

        viewModel.currentSessionIdx.observe(viewLifecycleOwner, {
            val type = viewModel.getCurrentSessionType()
            binding.finishButton.isVisible = type == SessionType.FOR_TIME
            binding.roundCounterButtonContainer.isVisible = type == SessionType.FOR_TIME || type == SessionType.AMRAP
            binding.round.isVisible = type == SessionType.HIIT || type == SessionType.EMOM
            binding.round.text =
                    "${viewModel.currentRoundIdx.value!! + 1}/${viewModel.getTotalRounds()}"
            binding.workoutImage.setImageDrawable(ResourcesHelper.getDrawableFor(type))

            refreshCounterButton()
        })

        viewModel.currentRoundIdx.observe(viewLifecycleOwner, { currentRoundIdx ->
            binding.round.text = "${currentRoundIdx + 1}/${viewModel.getTotalRounds()}"
        })

        viewModel.timerState.observe(viewLifecycleOwner, Observer { timerState ->
            val handler = Handler()
            when (timerState) {
                TimerState.PAUSED -> {
                    handler.postDelayed({
                        binding.timer.startAnimation(
                                AnimationUtils.loadAnimation(
                                        requireContext(),
                                        R.anim.blink
                                )
                        )
                    }, 100)
                }
                TimerState.ACTIVE -> {
                    handler.post { binding.timer.clearAnimation() }
                }
                TimerState.FINISHED -> {
                    drawFinishedScreen()
                }
                else -> return@Observer // do nothing
            }
        })

        binding.finishButton.setOnClickListener{
            viewModel.finishCurrentSession()
        }

        binding.closeButton.setOnClickListener{
            binding.closeButton.hide()
            requireActivity().onBackPressed()
            viewModel.handleCompletion()
        }

        setupCounter()

        binding.timer.setOnClickListener{ viewModel.toggleTimer()}
        return binding.root
    }

    // TODO: clean-up the multiple responsibilities
    private fun drawFinishedScreen() {
        binding.circleProgress?.isVisible = false
        binding.inProgressContainer.isVisible = false
        binding.finishedWorkoutContainer.isVisible = true
        binding.congrats.text = StringUtils.generateCongrats()

        for (idx in 0 until viewModel.sessions.size) {
            if (idx == 0) { // skip the pre-workout countdown
                // add the custom workout header if it's the case
                if (viewModel.sessionToAdd.name != null) {
                    binding.summaryLayout.summaryContainer.addView(createSummaryRowCustomHeader(viewModel.sessionToAdd.name!!))
                }
                continue
            }
            val session = viewModel.sessions[idx]
            val duration = viewModel.durations[idx]
            binding.summaryLayout.summaryContainer.addView(createSummaryRow(session, duration))
        }

        viewModel.prepareSessionToAdd()
        val rounds = viewModel.sessionToAdd.actualRounds
        val totalDuration = viewModel.sessionToAdd.actualDuration
        val notes = viewModel.sessionToAdd.notes

        binding.summaryLayout.summaryContainer.addView(createSummaryTotalRow(totalDuration))

        if (!viewModel.isCustomWorkout) {
            // we don't need no skeleton for custom workouts
            viewModel.sessionToAdd.skeleton = viewModel.sessions[1]
        }

        if (rounds != 0) {
            binding.summaryLayout.roundsEdit.setText(rounds.toString())
            binding.summaryLayout.repsEdit.setText("0")
        } else {
            //TODO: and show button to add these
//            binding.summaryLayout.roundsEdit.visibility = View.GONE
//            binding.summaryLayout.repsEdit.visibility = View.GONE
        }
        viewModel.sessionToAdd.actualRounds = rounds
        viewModel.sessionToAdd.actualDuration = totalDuration
        if (!binding.summaryLayout.repsEdit.editableText.isNullOrEmpty()) {
            viewModel.sessionToAdd.actualReps = toInt(binding.summaryLayout.repsEdit.editableText.toString())
        }
        viewModel.sessionToAdd.notes = notes

        // listen to edit text changes and update the session to be saved
        binding.summaryLayout.repsEdit.addTextChangedListener {
            if (!binding.summaryLayout.repsEdit.editableText.isNullOrEmpty()) {
                viewModel.sessionToAdd.actualReps = toInt(binding.summaryLayout.repsEdit.editableText.toString())
            }
        }
        binding.summaryLayout.roundsEdit.addTextChangedListener {
            if (!binding.summaryLayout.roundsEdit.editableText.isNullOrEmpty()) {
                viewModel.sessionToAdd.actualRounds = toInt(binding.summaryLayout.roundsEdit.editableText.toString())
            }
        }
        binding.summaryLayout.notesEdit.addTextChangedListener {
            if (!binding.summaryLayout.notesEdit.editableText.isNullOrEmpty()) {
                viewModel.sessionToAdd.notes = binding.summaryLayout.notesEdit.editableText.toString()
            }
        }

        startConfetti()
    }

    private fun setupCounter() {
        binding.roundCounterButton.setOnClickListener{
            viewModel.addRound()
            refreshCounterButton()
        }
        refreshCounterButton()
    }

    private fun refreshCounterButton() {
        val numCountedRounds = viewModel.getNumCurrentSessionRounds()
        binding.roundCounterText.isVisible = numCountedRounds != 0
        binding.roundCounterButton.drawable.alpha = if (numCountedRounds == 0) 255 else 0

        if (numCountedRounds != 0) {
            binding.roundCounterText.text = numCountedRounds.toString()
        }
    }

    private fun startConfetti() {
        binding.konfetti.build()
                .addColors(
                        ResourcesHelper.red,
                        ResourcesHelper.darkRed,
                        ResourcesHelper.green,
                        ResourcesHelper.darkGreen,
                        ResourcesHelper.darkerGreen,
                        ResourcesHelper.grey500,
                        ResourcesHelper.grey800
                )
                .setDirection(0.0, 359.0)
                .setSpeed(1f, 5f)
                .setFadeOutEnabled(true)
                .setTimeToLive(3500)
                .addShapes(Shape.Square, Shape.Circle)
                .addSizes(Size(6))
                .setPosition(
                        -50f,
                        DimensionsUtils.getScreenResolution(requireContext()).first + 50f,
                        -50f,
                        -50f
                )
                .streamFor(50, StreamEmitter.INDEFINITE)
    }

    private fun createSummaryRow(session: SessionSkeleton, duration: Int = 0) : ConstraintLayout {
        val layout = layoutInflater.inflate(R.layout.row_summary_header, null, false) as ConstraintLayout
        val image = layout.findViewById<ImageView>(R.id.summary_drawable)
        val text = layout.findViewById<TextView>(R.id.summary_text)
        image.setImageDrawable(ResourcesHelper.getDrawableFor(session.type))

        text.text = "${StringUtils.toString(session.type)}  ${StringUtils.toFavoriteFormat(session)}"
        if (session.type == SessionType.FOR_TIME) {
            text.text = "${text.text} (${StringUtils.secondsToNiceFormat(duration)})"
        }
        return layout
    }

    private fun createSummaryRowCustomHeader(name: String) : ConstraintLayout {
        val layout = layoutInflater.inflate(R.layout.row_summary_header, null, false) as ConstraintLayout
        val image = layout.findViewById<ImageView>(R.id.summary_drawable)
        val text = layout.findViewById<TextView>(R.id.summary_text)
        image.setImageDrawable(ResourcesHelper.getCustomWorkoutDrawable())

        text.text = name
        return layout
    }

    private fun createSummaryTotalRow(totalSeconds: Int) : ConstraintLayout {
        val layout = layoutInflater.inflate(R.layout.row_summary_header, null, false) as ConstraintLayout
        val image = layout.findViewById<ImageView>(R.id.summary_drawable)
        val text = layout.findViewById<TextView>(R.id.summary_text)

        image.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_timer, null))
        text.text = "Total: ${StringUtils.secondsToNiceFormat(totalSeconds)}"

        return layout
    }
}
