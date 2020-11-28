package goodtime.training.wod.timer.ui.main.amrap_for_time

import androidx.lifecycle.ViewModel
import goodtime.training.wod.timer.data.model.SessionSkeleton

abstract class MinutesAndSecondsViewModel(val minutes: Int, val seconds: Int) : ViewModel() {
    val timeData = TimeSpinnerData(minutes, seconds)

    lateinit var session : SessionSkeleton

    val minutesPickerData = ArrayList<Int>().apply{ addAll(0..60)}
    val secondsPickerData = ArrayList<Int>().apply { addAll(0..59)}
}