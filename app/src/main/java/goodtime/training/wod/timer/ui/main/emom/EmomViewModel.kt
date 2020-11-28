package goodtime.training.wod.timer.ui.main.emom

import androidx.lifecycle.ViewModel
import goodtime.training.wod.timer.data.model.SessionSkeleton

class EmomViewModel : ViewModel() {
    val emomData = EmomSpinnerData()

    lateinit var session : SessionSkeleton

    val minutesPickerData = ArrayList<Int>().apply { addAll(0..10) }
    val secondsPickerData = ArrayList<Int>().apply { addAll(0..59) }
    val roundsPickerData = ArrayList<Int>().apply { addAll(1..60) }
}