package goodtime.training.wod.timer.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import goodtime.training.wod.timer.common.soundplayer.SoundPlayer
import goodtime.training.wod.timer.data.repository.AppRepository

class WorkoutViewModelFactory(private val soundPlayer: SoundPlayer, private val appRepository: AppRepository)
    : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return WorkoutViewModel(soundPlayer, appRepository) as T
    }
}
