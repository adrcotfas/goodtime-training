package goodtime.training.wod.timer.ui.finished_workout

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.DimensionsUtils.Companion.windowWidth
import goodtime.training.wod.timer.common.ResourcesHelper
import goodtime.training.wod.timer.common.StringUtils
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import goodtime.training.wod.timer.common.toInt
import goodtime.training.wod.timer.data.model.Session
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.databinding.FragmentFinishedWorkoutBinding
import goodtime.training.wod.timer.ui.main.ReviewsViewModel
import goodtime.training.wod.timer.ui.main.ReviewsViewModelFactory
import goodtime.training.wod.timer.ui.timer.TimerViewModel
import goodtime.training.wod.timer.ui.timer.TimerViewModelFactory
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class FinishedWorkoutFragment : Fragment(), KodeinAware {
    override val kodein by closestKodein()

    private val viewModelFactory: TimerViewModelFactory by instance()
    private lateinit var viewModel: TimerViewModel
    private lateinit var binding: FragmentFinishedWorkoutBinding
    private lateinit var sessionToAdd: Session

    private val preferenceHelper: PreferenceHelper by instance()

    private lateinit var reviewManager: ReviewManager
    private lateinit var reviewViewModel: ReviewsViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        reviewManager = ReviewManagerFactory.create(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // required for the EditText to adjust when typing
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(TimerViewModel::class.java)
        viewModel.setInactive()
        viewModel.prepareSession()

        reviewViewModel =
            ViewModelProvider(requireActivity(), ReviewsViewModelFactory(reviewManager, preferenceHelper)).get(ReviewsViewModel::class.java)
        preferenceHelper.incrementCompletedWorkoutsForReview()
        reviewViewModel.preWarmReviewIfNeeded()
    }

    override fun onDestroy() {
        // required for the time pickers to not become messed up
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        super.onDestroy()
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
                viewModel.storeWorkout()
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun drawFinishedScreen() {
        binding.congrats.text = StringUtils.generateCongrats()
        sessionToAdd = viewModel.getPreparedSession()

        if (viewModel.getPreparedSession().isCustom()) {
            val name = sessionToAdd.name
            if (name != null) {
                binding.summaryContainer.addView(createSummaryRowCustomHeader(name))
            }
        } else {
            // 0 corresponds to the pre-workout countdown
            val session = viewModel.getSessions()[1]
            val duration = viewModel.getDurations()[1]
            binding.summaryContainer.addView(createSummaryRow(session, duration))
        }

        val rounds = sessionToAdd.actualRounds
        val totalDuration = sessionToAdd.actualDuration

        binding.summaryContainer.addView(createSummaryTotalRow(totalDuration))

        val repsEditText = binding.summaryLayout.repsLayout.editText
        val roundsEditText = binding.summaryLayout.roundsLayout.editText

        if (rounds != 0) {
            roundsEditText.setText(rounds.toString())
            repsEditText.setText("0")
        }

        if (!repsEditText.editableText.isNullOrEmpty()) {
            sessionToAdd.actualReps = toInt(repsEditText.editableText.toString())
        }

        repsEditText.addTextChangedListener {
            if (!repsEditText.editableText.isNullOrEmpty()) {
                sessionToAdd.actualReps = toInt(repsEditText.editableText.toString())
            }
        }
        roundsEditText.addTextChangedListener {
            if (!roundsEditText.editableText.isNullOrEmpty()) {
                sessionToAdd.actualRounds = toInt(roundsEditText.editableText.toString())
            }
        }

        val notesEditText = binding.summaryLayout.notesLayout.editText
        notesEditText.addTextChangedListener {
            if (!notesEditText.editableText.isNullOrEmpty()) {
                sessionToAdd.notes = notesEditText.editableText.toString()
            }
        }
    }

    override fun onResume() {
        super.onResume()
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
                windowWidth + 50f,
                -50f,
                -50f
            )
            .streamFor(50, 3000)
    }

    private fun createSummaryRow(session: SessionSkeleton, duration: Int = 0): ConstraintLayout {
        val layout = layoutInflater.inflate(R.layout.row_summary, null, false) as ConstraintLayout
        val image = layout.findViewById<ImageView>(R.id.summary_drawable)
        val text = layout.findViewById<TextView>(R.id.summary_text)
        image.setImageDrawable(ResourcesHelper.getDrawableFor(session.type))

        text.text = "${StringUtils.toString(session.type)}  ${StringUtils.toFavoriteFormat(session)}"
        return layout
    }

    private fun createSummaryRowCustomHeader(name: String): ConstraintLayout {
        val layout = layoutInflater.inflate(R.layout.row_summary, null, false) as ConstraintLayout
        val image = layout.findViewById<ImageView>(R.id.summary_drawable)
        val text = layout.findViewById<TextView>(R.id.summary_text)
        image.setImageDrawable(ResourcesHelper.getCustomWorkoutDrawable())

        text.text = name
        return layout
    }

    private fun createSummaryTotalRow(totalSeconds: Int): ConstraintLayout {
        val layout = layoutInflater.inflate(R.layout.row_summary, null, false) as ConstraintLayout
        val image = layout.findViewById<ImageView>(R.id.summary_drawable)
        val text = layout.findViewById<TextView>(R.id.summary_text)

        image.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_timer, null))
        text.text = "Total: ${StringUtils.secondsToNiceFormat(totalSeconds)}"

        return layout
    }
}