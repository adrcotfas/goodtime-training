package goodtime.training.wod.timer.ui.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import goodtime.training.wod.timer.common.soundplayer.SoundPlayer
import goodtime.training.wod.timer.data.repository.AppRepository

class TimerViewModelFactory(private val soundPlayer: SoundPlayer, private val appRepository: AppRepository)
    : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return TimerViewModel(soundPlayer, appRepository) as T
    }
}
