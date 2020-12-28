package goodtime.training.wod.timer.common.preferences.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import goodtime.training.wod.timer.GoodtimeApplication

class BootReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == null) return
        try {
            if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
                Log.d(TAG, "onBootComplete")
                GoodtimeApplication.getReminderHelper().scheduleNotifications()
            }
        } catch (e: RuntimeException) {
            Log.wtf(TAG, "Could not process intent")
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}