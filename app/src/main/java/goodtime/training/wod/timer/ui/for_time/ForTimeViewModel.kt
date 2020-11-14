package goodtime.training.wod.timer.ui.for_time

import androidx.lifecycle.ViewModel
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.ui.common.TimeSpinnerData

class ForTimeViewModel : ViewModel() {
    val timeData = TimeSpinnerData(15, 0)

    fun setDuration(duration: Pair<Int, Int>) {
        timeData.setMinutes(duration.first)
        timeData.setSeconds(duration.second)
    }

    lateinit var session : SessionSkeleton

    val minutesPickerData = ArrayList<Int>().apply{ addAll(0..60)}
    val secondsPickerData = ArrayList<Int>().apply { addAll(0..59)}
}
