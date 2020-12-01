package goodtime.training.wod.timer.ui.main.amrap_for_time

import goodtime.training.wod.timer.data.model.SessionType
import goodtime.training.wod.timer.data.repository.AppRepository

class AmrapViewModel(repo: AppRepository)
    : MinutesAndSecondsViewModel(repo, SessionType.AMRAP)
