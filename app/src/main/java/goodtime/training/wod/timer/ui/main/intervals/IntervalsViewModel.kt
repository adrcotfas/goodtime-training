package goodtime.training.wod.timer.ui.main.intervals

import androidx.lifecycle.ViewModel
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import goodtime.training.wod.timer.data.repository.AppRepository

class IntervalsViewModel(private val repo: AppRepository) : ViewModel() {

    lateinit var data : IntervalsSpinnerData
    lateinit var session : SessionSkeleton

    val minutesPickerData = ArrayList<Int>().apply { addAll(0..10) }
    val secondsPickerData = ArrayList<Int>().apply { addAll(0..59) }
    val roundsPickerData = ArrayList<Int>().apply { addAll(1..60) }

    fun getFavorites() = repo.getSessionSkeletons(SessionType.INTERVALS)
}