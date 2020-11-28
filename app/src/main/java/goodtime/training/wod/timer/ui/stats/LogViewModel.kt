package goodtime.training.wod.timer.ui.stats

import androidx.lifecycle.ViewModel
import goodtime.training.wod.timer.data.model.Session
import goodtime.training.wod.timer.data.repository.AppRepository

class LogViewModel(private val appRepository: AppRepository) : ViewModel() {

    fun addSession(session: Session) = appRepository.addSession(session)
    fun getSessions() = appRepository.getSessions()
}