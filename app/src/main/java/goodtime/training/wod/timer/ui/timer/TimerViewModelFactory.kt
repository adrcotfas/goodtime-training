package goodtime.training.wod.timer.ui.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import goodtime.training.wod.timer.data.repository.AppRepository

class TimerViewModelFactory(
        private val workoutManager: WorkoutManager,
        private val appRepository: AppRepository,
        private val preferenceHelper: PreferenceHelper)
    : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return TimerViewModel(workoutManager, appRepository, preferenceHelper) as T
    }
}
