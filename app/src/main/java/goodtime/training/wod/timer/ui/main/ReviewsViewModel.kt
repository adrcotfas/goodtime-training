package goodtime.training.wod.timer.ui.main

import androidx.annotation.Keep
import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class ReviewsViewModel @Keep constructor(
    private val reviewManager: ReviewManager,
    private val preferenceHelper: PreferenceHelper
) : ViewModel() {

    private var shouldAskForReview = false
    private var reviewInfo: Deferred<ReviewInfo>? = null

    /**
     * Start requesting the review info that will be needed later in advance if:
     * - 7 days since app install and at least 5 workouts were completed.
     * - 30 days have passed since the last review request
     */
    @MainThread
    fun preWarmReviewIfNeeded() {
        shouldAskForReview =
                // 7 days since install and 5 completed workouts
            (!preferenceHelper.askedForReviewInitial()
                    && (System.currentTimeMillis() - preferenceHelper.getFirstRunTime() >= TimeUnit.DAYS.toMillis(7))
                    && (preferenceHelper.getCompletedWorkoutsForReview() >= 5))
                    ||
                    (preferenceHelper.askedForReviewInitial()
                            && (System.currentTimeMillis() - preferenceHelper.getAskedForReviewTime() >= TimeUnit.DAYS.toMillis(30)))

        if (shouldAskForReview && reviewInfo == null) {
            reviewInfo = viewModelScope.async {
                reviewManager.requestReview()
            }
        }
    }

    /**
     * Only return ReviewInfo object if the pre-warming has already completed,
     * i.e. if the review can be launched immediately.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun obtainReviewInfo(): ReviewInfo? = withContext(Dispatchers.Main.immediate) {
        if (reviewInfo?.isCompleted == true && reviewInfo?.isCancelled == false) {
            reviewInfo?.getCompleted().also {
                reviewInfo = null
            }
        } else null
    }

    /**
     * The view should call this to let the ViewModel know that an attempt to show the review dialog
     * was made.
     *
     * @see shouldAskForReview
     */
    fun notifyAskedForReview() {
        if (!preferenceHelper.askedForReviewInitial()) {
            preferenceHelper.setAskedForReviewInitial(true)
        } else {
            preferenceHelper.updateAskedForReviewTime()
        }
        shouldAskForReview = false
    }
}

//TODO: use this pattern for other factories
class ReviewsViewModelFactory(
    private val manager: ReviewManager,
    private val preferenceHelper: PreferenceHelper
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(ReviewManager::class.java, PreferenceHelper::class.java)
            .newInstance(manager, preferenceHelper)
    }
}