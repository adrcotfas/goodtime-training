package goodtime.training.wod.timer.ui.timer

import android.content.Context
import android.content.Intent
import goodtime.training.wod.timer.ui.timer.TimeService.Companion.SESSIONS_NAME
import goodtime.training.wod.timer.ui.timer.TimeService.Companion.SESSIONS_RAW

class IntentWithAction : Intent {
    constructor(context: Context?, cls: Class<*>?, action: String) : super(context, cls) {
        this.action = action
    }

    constructor(context: Context?, cls: Class<*>?, action: String?, sessionsRaw: String, sessionName: String?) : super(context, cls) {
        this.action = action
        this.putExtra(SESSIONS_RAW, sessionsRaw)
        this.putExtra(SESSIONS_NAME, sessionName)
    }
}
