package goodtime.training.wod.timer.ui.custom

import androidx.lifecycle.ViewModel
import goodtime.training.wod.timer.data.model.CustomWorkoutSkeleton
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import java.util.concurrent.TimeUnit

class CustomWorkoutViewModel : ViewModel() {
    // TODO: init from repository as the first item OR the last used item (need to persist)
    var customWorkout : CustomWorkoutSkeleton = CustomWorkoutSkeleton("Sample workout",
        arrayListOf(
            SessionSkeleton(0, TimeUnit.MINUTES.toSeconds(10).toInt(), type = SessionType.AMRAP),
            SessionSkeleton(0, TimeUnit.MINUTES.toSeconds(15).toInt(), type = SessionType.FOR_TIME),
            SessionSkeleton(0, TimeUnit.MINUTES.toSeconds(20).toInt(), type = SessionType.AMRAP)
        ))
}
