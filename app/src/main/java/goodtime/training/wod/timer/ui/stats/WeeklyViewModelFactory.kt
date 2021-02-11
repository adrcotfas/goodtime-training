package goodtime.training.wod.timer.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import goodtime.training.wod.timer.data.repository.AppRepository

class WeeklyGoalViewModelFactory(private val appRepository: AppRepository)
    : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return WeeklyGoalViewModel(appRepository) as T
    }
}