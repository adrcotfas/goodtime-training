package goodtime.training.wod.timer.ui.timer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import goodtime.training.wod.timer.data.repository.AppRepository

class TimerViewModelFactory(
        private val context: Context,
        private val preferenceHelper: PreferenceHelper,
        private val appRepository: AppRepository)
    : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return TimerViewModel(TimerNotificationHelper(context, preferenceHelper), appRepository) as T
    }
}
