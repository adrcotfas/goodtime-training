package goodtime.training.wod.timer.ui.main.intervals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import goodtime.training.wod.timer.data.repository.AppRepository

class IntervalsViewModelFactory(private val repo: AppRepository)
    : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return IntervalsViewModel(repo) as T
    }
}