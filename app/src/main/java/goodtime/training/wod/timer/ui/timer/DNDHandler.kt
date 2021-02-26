package goodtime.training.wod.timer.ui.timer

import android.app.NotificationManager
import android.content.Context

class DNDHandler(context: Context) {
    private val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    fun toggleDndMode(enabled: Boolean) {
        if (notificationManager.isNotificationPolicyAccessGranted) {
            notificationManager.setInterruptionFilter(
                if (enabled) NotificationManager.INTERRUPTION_FILTER_PRIORITY
                else NotificationManager.INTERRUPTION_FILTER_ALL)
        }
    }
}
