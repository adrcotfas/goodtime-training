package goodtime.training.wod.timer.ui.finished_workout

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.DimensionsUtils
import goodtime.training.wod.timer.common.ResourcesHelper
import goodtime.training.wod.timer.common.StringUtils
import goodtime.training.wod.timer.common.toInt
import goodtime.training.wod.timer.data.model.Session
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import goodtime.training.wod.timer.databinding.FragmentFinishedWorkoutBinding
import goodtime.training.wod.timer.ui.timer.IntentWithAction
import goodtime.training.wod.timer.ui.timer.TimerService
import goodtime.training.wod.timer.ui.timer.TimerViewModel
import goodtime.training.wod.timer.ui.timer.TimerViewModelFactory
import nl.dionsegijn.konfetti.emitters.StreamEmitter
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class FinishedWorkoutFragment  : Fragment(), KodeinAware {
    override val kodein by closestKodein()

    private val viewModelFactory: TimerViewModelFactory by instance()
    private lateinit var viewModel: TimerViewModel
    private lateinit var binding: FragmentFinishedWorkoutBinding
    private lateinit var sessionToAdd: Session

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(TimerViewModel::class.java)
        viewModel.setInactive()
        viewModel.prepareSession()
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFinishedWorkoutBinding.inflate(layoutInflater, container, false)

        binding.closeButton.setOnClickListener {
            binding.closeButton.hide()
            requireActivity().onBackPressed()
        }
        setupHandleOnBackPressed()
        drawFinishedScreen()
        return binding.root
    }

    @SuppressLint("FragmentBackPressedCallback")
    private fun setupHandleOnBackPressed() {
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                    val intent = IntentWithAction(requireContext(), TimerService::class.java, TimerService.FINALIZE)
                    ContextCompat.startForegroundService(requireContext(), intent)
                    viewModel.finalize()
                    findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun drawFinishedScreen() {
        binding.congrats.text = StringUtils.generateCongrats()
        sessionToAdd = viewModel.getPreparedSession()

        for (idx in 0 until viewModel.getSessions().size) {
            if (idx == 0) { // skip the pre-workout countdown
                // add the custom workout header if it's the case
                val name = sessionToAdd.name
                if (name != null) {
                    binding.summaryLayout.summaryContainer.addView(createSummaryRowCustomHeader(name))
                }
                continue
            }
            val session = viewModel.getSessions()[idx]
            val duration = viewModel.getDurations()[idx]
            binding.summaryLayout.summaryContainer.addView(createSummaryRow(session, duration))
        }

        val rounds = sessionToAdd.actualRounds
        val totalDuration = sessionToAdd.actualDuration

        binding.summaryLayout.summaryContainer.addView(createSummaryTotalRow(totalDuration))

        if (rounds != 0) {
            binding.summaryLayout.roundsEdit.setText(rounds.toString())
            binding.summaryLayout.repsEdit.setText("0")
        } else {
            //TODO: and show button to add these
//            binding.summaryLayout.roundsEdit.visibility = View.GONE
//            binding.summaryLayout.repsEdit.visibility = View.GONE
        }

        if (!binding.summaryLayout.repsEdit.editableText.isNullOrEmpty()) {
            sessionToAdd.actualReps = toInt(binding.summaryLayout.repsEdit.editableText.toString())
        }

        // listen to edit text changes and update the session to be saved
        binding.summaryLayout.repsEdit.addTextChangedListener {
            if (!binding.summaryLayout.repsEdit.editableText.isNullOrEmpty()) {
                sessionToAdd.actualReps = toInt(binding.summaryLayout.repsEdit.editableText.toString())
            }
        }
        binding.summaryLayout.roundsEdit.addTextChangedListener {
            if (!binding.summaryLayout.roundsEdit.editableText.isNullOrEmpty()) {
                sessionToAdd.actualRounds = toInt(binding.summaryLayout.roundsEdit.editableText.toString())
            }
        }
        binding.summaryLayout.notesEdit.addTextChangedListener {
            if (!binding.summaryLayout.notesEdit.editableText.isNullOrEmpty()) {
                sessionToAdd.notes = binding.summaryLayout.notesEdit.editableText.toString()
            }
        }

        startConfetti()
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

    private fun createSummaryRow(session: SessionSkeleton, duration: Int = 0): ConstraintLayout {
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

    private fun createSummaryRowCustomHeader(name: String): ConstraintLayout {
        val layout = layoutInflater.inflate(R.layout.row_summary_header, null, false) as ConstraintLayout
        val image = layout.findViewById<ImageView>(R.id.summary_drawable)
        val text = layout.findViewById<TextView>(R.id.summary_text)
        image.setImageDrawable(ResourcesHelper.getCustomWorkoutDrawable())

        text.text = name
        return layout
    }

    private fun createSummaryTotalRow(totalSeconds: Int): ConstraintLayout {
        val layout = layoutInflater.inflate(R.layout.row_summary_header, null, false) as ConstraintLayout
        val image = layout.findViewById<ImageView>(R.id.summary_drawable)
        val text = layout.findViewById<TextView>(R.id.summary_text)

        image.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_timer, null))
        text.text = "Total: ${StringUtils.secondsToNiceFormat(totalSeconds)}"

        return layout
    }
}