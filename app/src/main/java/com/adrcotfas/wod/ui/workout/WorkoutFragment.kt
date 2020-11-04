package com.adrcotfas.wod.ui.workout

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.drawable.Drawable
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import com.adrcotfas.wod.R
import com.adrcotfas.wod.common.DimensionsUtils
import com.adrcotfas.wod.common.StringUtils
import com.adrcotfas.wod.common.notifications.NotificationHelper
import com.adrcotfas.wod.data.model.SessionMinimal
import com.adrcotfas.wod.data.model.SessionType
import com.adrcotfas.wod.data.workout.TimerState
import com.adrcotfas.wod.databinding.FragmentWorkoutBinding
import nl.dionsegijn.konfetti.emitters.StreamEmitter
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
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

        if (viewModel.timerState.value == TimerState.INACTIVE) {
            viewModel.init(args.sessions)
            viewModel.startWorkout()
        }

        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.timerState.value != TimerState.FINISHED) {
                    NavHostFragment.findNavController(this@WorkoutFragment)
                        .navigate(R.id.nav_dialog_stop_workout)
                } else {
                    viewModel.timerState.value = TimerState.INACTIVE
                    NavHostFragment.findNavController(this@WorkoutFragment)
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
    ): View? {
        binding = FragmentWorkoutBinding.inflate(layoutInflater, container, false)
        viewModel.secondsUntilFinished.observe( viewLifecycleOwner, Observer { seconds ->
            val type = viewModel.getCurrentSessionType()
            val total = viewModel.getCurrentSessionDuration()

            val secondsToShow =
                if (type != SessionType.FOR_TIME) seconds + 1
                else total - seconds
            if (type  != SessionType.BREAK
                && (seconds == total)) {
                binding.timer.text = "" // or "REST"
            } else {
                binding.timer.text = StringUtils.secondsToTimerFormat(secondsToShow)
            }
            val newProgress = (total - seconds).toFloat() / total.toFloat()
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                binding.circleProgress?.onTick(newProgress)
            }
        })

        viewModel.isResting.observe(viewLifecycleOwner, Observer { isResting ->
            val color =
                resources.getColor(if (isResting) R.color.red_goodtime else R.color.green_goodtime)
            val darkColor = resources.getColor(if (isResting) R.color.red_goodtime_dark else R.color.green_goodtime_darker)

            binding.timer.setTextColor(color)
            binding.round.setTextColor(color)
            binding.workoutImage.setColorFilter(color)

            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                binding.circleProgress?.setColor(color, darkColor)
            }
        })

        viewModel.currentSessionIdx.observe(viewLifecycleOwner, Observer {
            val type = viewModel.getCurrentSessionType()
            binding.finishButton.visibility =
                if (type != SessionType.FOR_TIME) View.GONE
                else View.VISIBLE
            binding.roundCounterButtonContainer.visibility =
                if (type != SessionType.FOR_TIME && type != SessionType.AMRAP) View.GONE
                else View.VISIBLE
            binding.round.visibility =
                if (type != SessionType.TABATA && type != SessionType.EMOM) View.GONE
                else View.VISIBLE

            binding.round.text = "${viewModel.currentRoundIdx.value!! + 1}/${viewModel.getTotalRounds()}"
            binding.workoutImage.setImageDrawable(toDrawable(type))
        })

        viewModel.currentRoundIdx.observe(viewLifecycleOwner, Observer { currentRoundIdx ->
            binding.round.text = "${currentRoundIdx + 1}/${viewModel.getTotalRounds()}"
        })

        viewModel.timerState.observe(viewLifecycleOwner, Observer {  timerState ->
            val handler = Handler()
            when (timerState) {
                TimerState.PAUSED -> {
                    handler.postDelayed({
                        binding.timer.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.blink))
                    }, 100)
                }
                TimerState.ACTIVE -> {
                    handler.post { binding.timer.clearAnimation() }
                }
                TimerState.FINISHED -> {
                    binding.circleProgress?.visibility = View.GONE
                    binding.inProgressContainer.visibility = View.GONE
                    binding.finishedWorkoutContainer.visibility = View.VISIBLE

                    var totalDuration = 0
                    for (idx in 0 until viewModel.sessions.size) {
                        if (idx == 0) { // skip the pre-workout countdown
                            continue
                        }
                        binding.summaryContainer.addView(createSummaryRow(viewModel.sessions[idx], viewModel.durations[idx]))
                        totalDuration += viewModel.durations[idx]
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

        binding.roundCounterButton.setOnClickListener{
            viewModel.countedRounds.value = viewModel.countedRounds.value?.plus(1)
        }

        binding.closeButton.setOnClickListener{
            binding.closeButton.hide()
            requireActivity().onBackPressed()
        }

        viewModel.countedRounds.observe(viewLifecycleOwner, Observer {rounds ->
            binding.roundCounterText.visibility = if (rounds == 0) View.GONE else View.VISIBLE
            binding.roundCounterButton.drawable.alpha = if (rounds == 0) 255 else 0
            binding.roundCounterText.text = rounds.toString()
        })

        binding.timer.setOnClickListener{ viewModel.toggleTimer()}

        // TODO: observe for session finished and cancel the StopWorkoutDialog
        return binding.root
    }

    private fun toDrawable(type : SessionType) : Drawable {
        return ResourcesCompat.getDrawable(resources,
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
                    R.drawable.ic_fire2
                }
                SessionType.BREAK -> {
                    R.drawable.ic_break
                }
            }, null)!!
    }

    private fun startConfetti() {
        binding.konfetti.build()
            .addColors(
                resources.getColor(R.color.red_goodtime_dark),
                resources.getColor(R.color.red_goodtime),
                resources.getColor(R.color.green_goodtime),
                resources.getColor(R.color.green_goodtime_dark),
                resources.getColor(R.color.grey500),
                resources.getColor(R.color.grey800)
            )
            .setDirection(0.0, 359.0)
            .setSpeed(1f, 5f)
            .setFadeOutEnabled(true)
            .setTimeToLive(3500)
            .addShapes(Shape.Square, Shape.Circle)
            .addSizes(Size(6))
            .setPosition(-50f,
                DimensionsUtils.getScreenResolution(requireContext()).first + 50f,
                -50f,
                -50f)
            .streamFor(50, StreamEmitter.INDEFINITE)
    }

    private fun createSummaryRow(session : SessionMinimal, duration : Int) : ConstraintLayout {
        val layout = layoutInflater.inflate(R.layout.row_summary_view, null, false) as ConstraintLayout
        val image = layout.findViewById<ImageView>(R.id.summary_drawable)
        val text = layout.findViewById<TextView>(R.id.summary_text)
        image.setImageDrawable(toDrawable(session.type))

        if (session.type == SessionType.FOR_TIME) {
            text.text = "${StringUtils.toString(session.type)}  ${StringUtils.toFavoriteFormat(session)} " +
                    "(${StringUtils.secondsToNiceFormat(duration)})"
        } else {
            text.text = "${StringUtils.toString(session.type)}  ${StringUtils.toFavoriteFormat(session)}"
        }
        return layout
    }

    private fun createSummaryTotalRow(totalSeconds : Int) : ConstraintLayout {
        val layout = layoutInflater.inflate(R.layout.row_summary_view, null, false) as ConstraintLayout
        val image = layout.findViewById<ImageView>(R.id.summary_drawable)
        val text = layout.findViewById<TextView>(R.id.summary_text)

        image.setImageDrawable(resources.getDrawable(R.drawable.ic_timer))
        text.text = "Total: ${StringUtils.secondsToNiceFormat(totalSeconds)}"

        return layout
    }
}
