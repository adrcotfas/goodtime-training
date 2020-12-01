package goodtime.training.wod.timer.ui.main.hiit

import androidx.lifecycle.ViewModel
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.model.SessionType
import goodtime.training.wod.timer.data.repository.AppRepository

class HiitViewModel(private val repo: AppRepository) : ViewModel() {

    lateinit var hiitData:  HiitSpinnerData
    lateinit var session: SessionSkeleton

    val secondsPickerData = ArrayList<Int>().apply { addAll(1..90) }
    val roundsPickerData = ArrayList<Int>().apply { addAll(1..60) }

    fun getFavorites() = repo.getSessionSkeletons(SessionType.HIIT)
}
