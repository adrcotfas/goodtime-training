package goodtime.training.wod.timer.ui.main.custom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import goodtime.training.wod.timer.data.model.CustomWorkoutSkeleton
import goodtime.training.wod.timer.data.model.SessionSkeleton
import goodtime.training.wod.timer.data.repository.AppRepository
import kotlinx.coroutines.launch

class CustomWorkoutViewModel(private val appRepository: AppRepository) : ViewModel() {

    fun getWorkoutList() = appRepository.getCustomWorkoutSkeletons()
    var favorites = listOf<CustomWorkoutSkeleton>()

    lateinit var currentWorkoutName: String
    var currentWorkoutSessions = arrayListOf<SessionSkeleton>()

    var hasUnsavedSession = false

    fun saveCurrentSelection() {
        viewModelScope.launch {
            appRepository.editCustomWorkoutSkeleton(CustomWorkoutSkeleton(currentWorkoutName, currentWorkoutSessions))
        }
    }

    fun favoritesContainCurrentWorkoutName(): Boolean {
        return favorites.find { it.name == currentWorkoutName } != null
    }

}
