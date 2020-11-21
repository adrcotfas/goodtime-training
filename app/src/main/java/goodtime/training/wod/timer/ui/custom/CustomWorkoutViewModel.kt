package goodtime.training.wod.timer.ui.custom

import androidx.lifecycle.ViewModel
import goodtime.training.wod.timer.data.model.CustomWorkoutSkeleton
import goodtime.training.wod.timer.data.repository.AppRepository

class CustomWorkoutViewModel(private val appRepository: AppRepository) : ViewModel() {

    val customWorkoutList = appRepository.getCustomWorkoutSkeletons()

    lateinit var customWorkout : CustomWorkoutSkeleton

    fun saveCurrentSelection() {
        appRepository.addCustomWorkoutSkeleton(customWorkout)
    }
}
