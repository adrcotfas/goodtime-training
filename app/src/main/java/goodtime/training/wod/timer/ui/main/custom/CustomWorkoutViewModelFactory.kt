package goodtime.training.wod.timer.ui.main.custom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import goodtime.training.wod.timer.data.repository.AppRepository

class CustomWorkoutViewModelFactory(private val appRepository: AppRepository)
    : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CustomWorkoutViewModel(appRepository) as T
    }
}

