package goodtime.training.wod.timer.ui.main.amrap_for_time

import androidx.lifecycle.ViewModel
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import goodtime.training.wod.timer.data.repository.AppRepository

abstract class MinutesAndSecondsViewModel(
    private val repo: AppRepository,
    private val sessionType: SessionType) : ViewModel() {

    lateinit var timeData :  TimeSpinnerData
    lateinit var session : SessionSkeleton

    val minutesPickerData = ArrayList<Int>().apply{ addAll(0..60)}
    val secondsPickerData = ArrayList<Int>().apply { addAll(0..59)}

    fun getFavorites() = repo.getSessionSkeletons(sessionType)
}