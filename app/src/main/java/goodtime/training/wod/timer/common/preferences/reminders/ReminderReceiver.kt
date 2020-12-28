package goodtime.training.wod.timer.common.preferences.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import goodtime.training.wod.timer.common.preferences.reminders.ReminderHelper.Companion.notifyReminder

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive")
        notifyReminder(context)
    }

    companion object {
        private const val TAG = "ReminderReceiver"
    }
}