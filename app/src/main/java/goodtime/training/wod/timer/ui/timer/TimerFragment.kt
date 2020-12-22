package goodtime.training.wod.timer.ui.timer

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.DimensionsUtils
import goodtime.training.wod.timer.common.ResourcesHelper
import goodtime.training.wod.timer.common.StringUtils
import goodtime.training.wod.timer.common.ViewUtils
import goodtime.training.wod.timer.common.notifications.NotificationHelper
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
            binding.finishButton.visibility =
                if (type != SessionType.FOR_TIME) View.GONE
                else View.VISIBLE
            binding.roundCounterButtonContainer.visibility =
                if (type != SessionType.FOR_TIME && type != SessionType.AMRAP) View.GONE
                else View.VISIBLE
            binding.round.visibility =
                if (type != SessionType.HIIT && type != SessionType.EMOM) View.GONE
                else View.VISIBLE

            binding.round.text =
                "${viewModel.currentRoundIdx.value!! + 1}/${viewModel.getTotalRounds()}"
            binding.workoutImage.setImageDrawable(ViewUtils.toDrawable(resources, type))

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
                    binding.circleProgress?.visibility = View.GONE
                    binding.inProgressContainer.visibility = View.GONE
                    binding.finishedWorkoutContainer.visibility = View.VISIBLE

                    binding.congrats.text = StringUtils.generateCongrats()

                    var totalDuration = 0
                    for (idx in 0 until viewModel.sessions.size) {
                        if (idx == 0) { // skip the pre-workout countdown
                            continue
                        }
                        val session = viewModel.sessions[idx]
                        val duration = viewModel.durations[idx]
                        if (session.type == SessionType.FOR_TIME || session.type == SessionType.AMRAP) {
                            binding.summaryContainer.addView(
                                createSummarySectionWithRounds(
                                    session,
                                    duration,
                                    viewModel.getRounds(idx)
                                )
                            )
                        } else {
                            binding.summaryContainer.addView(createSummaryRow(session))
                        }
                        totalDuration += duration
                    }
                    binding.summaryContainer.addView(createSummaryTotalRow(totalDuration))
                    startConfetti()
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

    private fun setupCounter() {
        binding.roundCounterButton.setOnClickListener{
            viewModel.addRound()
            refreshCounterButton()
        }
        refreshCounterButton()
    }

    private fun refreshCounterButton() {
        val numCountedRounds = viewModel.getNumCurrentSessionRounds()
        binding.roundCounterText.visibility =
            if (numCountedRounds == 0) View.GONE else View.VISIBLE
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

    private fun createSummaryRow(session: SessionSkeleton) : ConstraintLayout {
        val layout = layoutInflater.inflate(R.layout.row_summary_header, null, false) as ConstraintLayout
        val image = layout.findViewById<ImageView>(R.id.summary_drawable)
        val text = layout.findViewById<TextView>(R.id.summary_text)
        image.setImageDrawable(ViewUtils.toDrawable(resources, session.type))

        text.text = "${StringUtils.toString(session.type)}  ${StringUtils.toFavoriteFormat(session)}"
        return layout
    }

    private fun createSummarySectionWithRounds(session: SessionSkeleton, duration: Int, rounds : ArrayList<Int>) : ConstraintLayout {
        val layout = layoutInflater.inflate(R.layout.row_summary_view_for_time, null, false) as ConstraintLayout
        val headerImage = layout.findViewById<ImageView>(R.id.summary_drawable)
        val headerText = layout.findViewById<TextView>(R.id.summary_text)

        headerImage.setImageDrawable(ViewUtils.toDrawable(resources, session.type))
        headerText.text = "${StringUtils.toString(session.type)}  ${StringUtils.toFavoriteFormat(
            session
        )}"

        val roundsText = layout.findViewById<TextView>(R.id.rounds_text)
        val roundsContainer = layout.findViewById<LinearLayout>(R.id.rounds_container)
        val durationText = layout.findViewById<TextView>(R.id.duration_text)

        roundsText.visibility = if (rounds.isEmpty()) View.GONE else View.VISIBLE
        roundsText.paintFlags = roundsText.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        roundsText.text = "${rounds.size} rounds"
        if (session.type == SessionType.AMRAP) {
            // no need to show the time it took like for FOR_TIME sessions
            durationText.visibility = View.GONE
        } else {
            durationText.text = "(${StringUtils.secondsToNiceFormat(duration)})"
        }

        roundsText.setOnClickListener{roundsContainer.visibility =
            if (roundsContainer.visibility == View.VISIBLE) View.GONE else View.VISIBLE}

        for (i in 0 until rounds.size) {
            val roundRow = layoutInflater.inflate(R.layout.row_summary_view_round_detail, null, false) as ConstraintLayout
            val roundNumber = roundRow.findViewById<TextView>(R.id.round_number)
            val roundTime = roundRow.findViewById<TextView>(R.id.round_time)
            val roundDelta = roundRow.findViewById<TextView>(R.id.round_delta)

            roundNumber.text = "#${i+1}"
            roundTime.text = StringUtils.secondsToNiceFormat(rounds[i])
            if (i > 0) {
                val deltaSeconds = rounds[i] - rounds[i - 1]
                val color = if (deltaSeconds > 0) ResourcesHelper.red else ResourcesHelper.green
                roundDelta.setTextColor(color)
                roundDelta.text = "${if (deltaSeconds > 0) "+" else if (deltaSeconds == 0) " " else "-"} ${StringUtils.secondsToNiceFormat(kotlin.math.abs(deltaSeconds))}"
            }
            roundsContainer.addView(roundRow)
        }
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
