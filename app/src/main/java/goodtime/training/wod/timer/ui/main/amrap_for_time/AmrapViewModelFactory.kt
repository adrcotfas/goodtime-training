package goodtime.training.wod.timer.ui.main.amrap_for_time

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import goodtime.training.wod.timer.data.repository.AppRepository

class AmrapViewModelFactory(private val repo: AppRepository)
    : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AmrapViewModel(repo) as T
    }
}