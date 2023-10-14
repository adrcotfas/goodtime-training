package goodtime.training.wod.timer.common.preferences.reminders

import android.annotation.TargetApi
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import goodtime.training.wod.timer.MainActivity
import goodtime.training.wod.timer.R
import goodtime.training.wod.timer.common.StringUtils
import goodtime.training.wod.timer.common.preferences.PreferenceHelper
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

class ReminderHelper(context: Context?) : ContextWrapper(context), KodeinAware,
        OnSharedPreferenceChangeListener {

    override val kodein by closestKodein()
    private val preferenceHelper by instance<PreferenceHelper>()
    private var pendingIntents : Array<PendingIntent?> = arrayOfNulls(7)
    private val alarmManager: AlarmManager by lazy {
        this.getSystemService(ALARM_SERVICE) as AlarmManager
    }

    init {
        preferenceHelper.dataStore.preferences.registerOnSharedPreferenceChangeListener(this)
        initChannelIfNeeded()
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun initChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel = notificationManager.getNotificationChannel(GOODTIME_REMINDER_CHANNEL_ID)
            if (channel == null) {
                Log.d(TAG, "initChannel")
                val c = NotificationChannel(
                    GOODTIME_REMINDER_CHANNEL_ID,
                    "Workout reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                c.setShowBadge(true)
                notificationManager.createNotificationChannel(c)
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key != null) {
            if (key.contains(PreferenceHelper.REMINDER_DAYS)) {
                Log.d(TAG, "onSharedPreferenceChanged: $key")
                val idx = key.last().toInt() - '0'.toInt()
                val reminderDay = DayOfWeek.of(idx + 1)
                if (preferenceHelper.isReminderEnabledFor(reminderDay)) {
                    toggleBootReceiver(true)
                    scheduleNotification(reminderDay)
                } else {
                    cancelNotification(reminderDay)
                    toggleBootReceiver(false)
                }
            } else if (key == PreferenceHelper.REMINDER_TIME) {
                Log.d(TAG, "onSharedPreferenceChanged: REMINDER_TIME")
                cancelNotifications()
                scheduleNotifications()
            }
        }
    }

    private fun toggleBootReceiver(enabled: Boolean) {
        Log.d(TAG, "toggleBootReceiver ${if (enabled) "ENABLED" else "DISABLED"}")
        val receiver = ComponentName(this, BootReceiver::class.java)
        val pm = this.packageManager
        pm.setComponentEnabledSetting(
            receiver,
            if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun getReminderPendingIntent(index: Int): PendingIntent {
        if (pendingIntents[index] == null) {
            val intent = Intent(this, ReminderReceiver::class.java)
            intent.action = getString(R.string.reminder_action)
            pendingIntents[index] = PendingIntent.getBroadcast(
                this,
                REMINDER_REQUEST_CODE + index,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        return pendingIntents[index]!!
    }

    private fun cancelNotifications() {
        Log.d(TAG, "cancelNotifications")
        for (day in DayOfWeek.entries) {
            cancelNotification(day)
        }
    }

    private fun cancelNotification(day: DayOfWeek) {
        Log.d(TAG, "cancelNotification for $day")
        val reminderPendingIntent = getReminderPendingIntent(day.ordinal)
        alarmManager.cancel(reminderPendingIntent)
    }

    fun scheduleNotifications() {
        if (preferenceHelper.isReminderEnabled()) {
            for (i in preferenceHelper.getReminderDays().withIndex()) {
                if (i.value) {
                    val reminderDay = DayOfWeek.of(i.index + 1)
                    scheduleNotification(reminderDay)
                }
            }
        }
    }

    private fun scheduleNotification(reminderDay: DayOfWeek) {
        val now = LocalDateTime.now()
        Log.d(TAG, "now: ${now.toLocalTime()}")

        val time = LocalTime.ofSecondOfDay(preferenceHelper.getReminderTime().toLong())
        Log.d(TAG, "time of reminder: $time")

        var reminderTime = now
                .withHour(time.hour)
                .withMinute(time.minute)
                .withSecond(0)
                .with(TemporalAdjusters.nextOrSame(reminderDay))

        if (reminderTime.isBefore(now)) {
            Log.d(TAG, "reminderTime is before now; schedule for next week")
            reminderTime = reminderTime.plusWeeks(1)
        }

        Log.d(TAG, "reminderTime: $reminderTime")

        val reminderMillis = reminderTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        Log.d(TAG, "scheduleNotification at: " + StringUtils.formatDateAndTime(reminderMillis))
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            reminderMillis,
            AlarmManager.INTERVAL_DAY * 7,
            getReminderPendingIntent(reminderDay.ordinal)
        )
    }

    companion object {
        private const val TAG = "ReminderHelper"
        private const val GOODTIME_REMINDER_CHANNEL_ID = "goodtime.training.reminder_notification"
        const val REMINDER_REQUEST_CODE = 42
        private const val REMINDER_NOTIFICATION_ID = 99

        @JvmStatic
        fun notifyReminder(context: Context) {
            val openMainIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openMainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val builder = NotificationCompat.Builder(context, GOODTIME_REMINDER_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_run)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setContentIntent(pendingIntent)
                    .setShowWhen(false)
                    .setOnlyAlertOnce(true)
                    .setContentTitle(context.getString(R.string.reminder_title))
                    .setContentText(context.getString(R.string.reminder_text))
            val notificationManager =
                    context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(REMINDER_NOTIFICATION_ID, builder.build())
        }
        fun removeNotification(context: Context) {
            val notificationManager =
                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(REMINDER_NOTIFICATION_ID)
        }
    }
}