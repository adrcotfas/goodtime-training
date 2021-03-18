package goodtime.training.wod.timer.ui.main

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import goodtime.training.wod.timer.common.Events
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.ui.main.custom.SelectCustomWorkoutDialog
import org.greenrobot.eventbus.EventBus
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

abstract class WorkoutTypeFragment :
    Fragment(),
    KodeinAware,
    SelectFavoriteDialog.Listener,
    SelectCustomWorkoutDialog.Listener {

    override val kodein by closestKodein()
    private var isDurationValid = true
    private val preferenceHelper: PreferenceHelper by instance()

    private lateinit var reviewManager: ReviewManager
    private lateinit var reviewViewModel: ReviewsViewModel

    override fun onResume() {
        super.onResume()
        showContent()
    }

    override fun onPause() {
        super.onPause()
        hideContent()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reviewViewModel =
            ViewModelProvider(requireActivity(), ReviewsViewModelFactory(reviewManager, preferenceHelper)).get(ReviewsViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        reviewManager = ReviewManagerFactory.create(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            val reviewInfo = reviewViewModel.obtainReviewInfo()
            if (reviewInfo != null) {
                reviewManager.launchReview(requireActivity(), reviewInfo)
                reviewViewModel.notifyAskedForReview()
            }
        }
    }

    abstract fun onStartWorkout()

    abstract fun getSelectedSessions(): ArrayList<SessionSkeleton>

    fun updateMainButtonsState(duration: Int) {
        if (isDurationValid && duration != 0 || !isDurationValid && duration == 0) {
            return
        }
        isDurationValid = duration != 0
        EventBus.getDefault().post(Events.Companion.SetStartButtonStateWithColor(isDurationValid))
    }

    private fun hideContent() {
        view?.apply {
            alpha = 1f
            visibility = View.VISIBLE
            animate()
                .alpha(0f)
                .setDuration(FADE_ANIMATION_DURATION)
                .setListener(null)
        }
    }

    private fun showContent() {
        view?.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate()
                .alpha(1f)
                .setDuration(FADE_ANIMATION_DURATION)
                .setListener(null)
        }
    }

    companion object {
        const val FADE_ANIMATION_DURATION = 150L
    }
}