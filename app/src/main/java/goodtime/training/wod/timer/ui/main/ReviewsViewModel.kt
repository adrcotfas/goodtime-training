package goodtime.training.wod.timer.ui.main

import android.util.Log
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

class ReviewsViewModel @Keep constructor(
    private val reviewManager: ReviewManager,
    private val preferenceHelper: PreferenceHelper
) : ViewModel() {

    private var shouldAskForReview = false
    private var reviewInfo: Deferred<ReviewInfo>? = null

    /**
     * Start requesting the review info that will be needed later in advance
     * only if 7 days have passed since the last review request (or 7 days since app install)
     * and if at least 5 workouts were completed.
     */
    @MainThread
    fun preWarmReview() {
        Log.i("ReviewsViewModel", "preWarmReview")
        preferenceHelper.incrementCompletedWorkoutsForReview()
        shouldAskForReview = true
        //TODO: implement this after internal track testing
//            (System.currentTimeMillis() - preferenceHelper.getAskedForReviewTime() > TimeUnit.DAYS.toMillis(7)) &&
//                (preferenceHelper.getCompletedWorkoutsForReview() >= 5)

        if (shouldAskForReview && reviewInfo == null) {
            reviewInfo = viewModelScope.async {
                Log.i("ReviewsViewModel", "requestReview")
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
        Log.i("ReviewsViewModel", "obtainReviewInfo")
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
        Log.i("ReviewsViewModel", "notifyAskedForReview")
        preferenceHelper.setAskedForReviewTime(System.currentTimeMillis())
        preferenceHelper.resetCompletedWorkoutsForReview()
        shouldAskForReview = false
    }
}

//TODO: use this pattern for other factories
class ReviewsViewModelFactory(
    private val manager: ReviewManager,
    private val preferenceHelper: PreferenceHelper
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(ReviewManager::class.java, PreferenceHelper::class.java).newInstance(manager, preferenceHelper)
    }
}