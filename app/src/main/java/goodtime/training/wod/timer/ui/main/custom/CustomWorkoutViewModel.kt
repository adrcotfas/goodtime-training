package goodtime.training.wod.timer.ui.main.custom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import goodtime.training.wod.timer.data.model.CustomWorkoutSkeleton
import goodtime.training.wod.timer.data.repository.AppRepository
import kotlinx.coroutines.launch

class CustomWorkoutViewModel(private val appRepository: AppRepository) : ViewModel() {

    fun getWorkoutList() = appRepository.getCustomWorkoutSkeletons()

    lateinit var currentWorkout: CustomWorkoutSkeleton
    var hasUnsavedSession = false
    var numberOfFavorites: Int = 0

    fun saveCurrentSelection() {
        viewModelScope.launch {
            appRepository.editCustomWorkoutSkeleton(currentWorkout)
        }
    }
}
