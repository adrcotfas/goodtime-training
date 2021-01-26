package goodtime.training.wod.timer.ui.main.custom

import androidx.lifecycle.ViewModel
import goodtime.training.wod.timer.data.model.CustomWorkoutSkeleton
import goodtime.training.wod.timer.data.repository.AppRepository

class CustomWorkoutViewModel(private val appRepository: AppRepository) : ViewModel() {

    fun getWorkoutList() = appRepository.getCustomWorkoutSkeletons()

    lateinit var currentWorkout: CustomWorkoutSkeleton
    var hasUnsavedSession = false

    fun saveCurrentSelection() {
        appRepository.editCustomWorkoutSkeleton(currentWorkout)
    }
}
