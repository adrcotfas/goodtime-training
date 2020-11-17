package goodtime.training.wod.timer.ui.emom

import androidx.lifecycle.ViewModel
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.ui.common.EmomSpinnerData

class EmomViewModel : ViewModel() {
    val emomData = EmomSpinnerData()

    fun setEmomData(duration: Pair<Int, Int>, rounds: Int) {
        emomData.setMinutes(duration.first)
        emomData.setSeconds(duration.second)
        emomData.setRounds(rounds)
    }

    lateinit var session : SessionSkeleton

    val minutesPickerData = ArrayList<Int>().apply { addAll(0..10) }
    val secondsPickerData = ArrayList<Int>().apply { addAll(0..59) }
    val roundsPickerData = ArrayList<Int>().apply { addAll(1..60) }
}