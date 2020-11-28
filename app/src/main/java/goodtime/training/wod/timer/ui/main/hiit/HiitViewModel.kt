package goodtime.training.wod.timer.ui.main.hiit

import androidx.lifecycle.ViewModel
import goodtime.training.wod.timer.data.model.SessionSkeleton

class HiitViewModel : ViewModel() {
    val tabataData =  HiitSpinnerData()

    lateinit var session : SessionSkeleton

    val secondsPickerData = ArrayList<Int>().apply { addAll(1..90) }
    val roundsPickerData = ArrayList<Int>().apply { addAll(1..60) }
}
