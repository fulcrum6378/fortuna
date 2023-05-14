package ir.mahdiparastesh.fortuna

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.os.Build
import androidx.core.app.ActivityCompat
import ir.mahdiparastesh.fortuna.Kit.resetHours
import ir.mahdiparastesh.fortuna.Vita.Companion.toKey
import ir.mahdiparastesh.fortuna.misc.TodayWidget

/** Awakens every night at 12 AM and perform various actions. */
class Nyx : BroadcastReceiver() {
    companion object {
        const val REMIND = "remind"
        const val CHANNEL = 666

        fun alarm(c: Context) {
            (c.getSystemService(Context.ALARM_SERVICE) as? AlarmManager)?.setInexactRepeating(
                AlarmManager.RTC, Calendar.getInstance()
                    .apply { timeInMillis += Kit.A_DAY; resetHours() }.timeInMillis,
                Kit.A_DAY, broadcast(c)
            )
        }

        private fun broadcast(c: Context): PendingIntent = PendingIntent.getBroadcast(
            c, 0, Intent(c, Nyx::class.java), PendingIntent.FLAG_IMMUTABLE
        )

        @Suppress("unused")
        fun test(c: Context) {
            broadcast(c).send()
        }
    }

    override fun onReceive(c: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            alarm(c); return; }

        // Remind the user to score the recent day if already has not
        val cal = Kit.calType.newInstance().apply { timeInMillis -= Kit.A_DAY }
        val score = Vita.load(c).getOrDefault(cal.toKey(), null)
            ?.get(cal[Calendar.DAY_OF_MONTH] - 1)
        if (score == null && (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ActivityCompat.checkSelfPermission(c, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED)
        ) (c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(
            CHANNEL, Notification.Builder(c, REMIND)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(c.getString(R.string.ntfReminder))
                .setContentIntent(Kit.openInDate(c, cal, 0))
                .setAutoCancel(true)
                .setShowWhen(false)
                .build()
        )

        // Miscellaneous
        Vita.backup(c)
        TodayWidget.externalUpdate(c)
        Main.handler?.obtainMessage(Main.HANDLE_NEW_DAY)?.sendToTarget()
    }
}
