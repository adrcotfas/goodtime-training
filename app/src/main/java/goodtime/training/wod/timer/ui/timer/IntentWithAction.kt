package goodtime.training.wod.timer.ui.timer

import android.content.Context
import android.content.Intent

class IntentWithAction : Intent {
    constructor(context: Context?, cls: Class<*>?, action: String) : super(context, cls) {
        this.action = action
    }
}
