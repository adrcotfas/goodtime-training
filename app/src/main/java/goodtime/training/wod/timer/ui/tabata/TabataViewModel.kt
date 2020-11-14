package goodtime.training.wod.timer.ui.tabata

import androidx.lifecycle.ViewModel
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.ui.common.TabataSpinnerData

class TabataViewModel : ViewModel() {
    val tabataData =  TabataSpinnerData()

    fun setTabataData(durationWork: Pair<Int, Int>, breakDuration: Pair<Int, Int>, numRounds: Int) {
        tabataData.setMinutesWork(durationWork.first)
        tabataData.setSecondsWork(durationWork.second)
        tabataData.setMinutesBreak(breakDuration.first)
        tabataData.setSecondsBreak(breakDuration.second)
        tabataData.setRounds(numRounds)
    }

    lateinit var session : SessionSkeleton

    val secondsPickerData = ArrayList<Int>().apply { addAll(1..90) }
    val roundsPickerData = ArrayList<Int>().apply { addAll(1..60) }
}
