package ir.mahdiparastesh.fortuna

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import ir.mahdiparastesh.fortuna.sect.TodayWidget
import ir.mahdiparastesh.fortuna.util.Dropbox
import ir.mahdiparastesh.fortuna.util.NumberUtils
import ir.mahdiparastesh.fortuna.util.NumberUtils.toKey
import ir.mahdiparastesh.fortuna.util.UiTools
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.chrono.ChronoLocalDate
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit

/** Awakens every night at 12 AM and performs various actions. */
class Nyx : BroadcastReceiver() {

    companion object {
        private const val REMIND = "remind"
        private const val CHANNEL = 378

        fun alarm(c: Context) {
            (c.getSystemService(Context.ALARM_SERVICE) as? AlarmManager)?.setInexactRepeating(
                AlarmManager.RTC,
                LocalDate
                    .now()
                    .plus(1, ChronoUnit.DAYS)
                    .atTime(0, 0, 0)
                    .toInstant(OffsetDateTime.now().offset)
                    .toEpochMilli(),
                NumberUtils.A_DAY, broadcast(c)
            )
        }

        private fun broadcast(c: Context): PendingIntent = PendingIntent.getBroadcast(
            c, 0, Intent(c, Nyx::class.java), PendingIntent.FLAG_IMMUTABLE
        )

        @Suppress("unused")
        fun test(c: Context) {
            broadcast(c).send()
        }

        fun cancelNotification(c: Context) {
            (c.getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .cancel(CHANNEL)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            alarm(context); return; }
        val c = context.applicationContext as Fortuna

        // today
        TodayWidget.externalUpdate(c)
        Main.handler?.obtainMessage(Main.HANDLE_NEW_DAY)?.sendToTarget()

        // remind the user to score the recent day if already has not
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val date = c.chronology.dateNow().minus(1, ChronoUnit.DAYS)
            val score = Vita(c).getOrDefault(date.toKey(), null)
                ?.get(date[ChronoField.DAY_OF_MONTH] - 1)
            if (score == null) howWasYourDay(c, date)
        }

        // backup
        c.backupVita()
        val dropbox = Dropbox(c)
        if (dropbox.isAuthenticated())
            CoroutineScope(Dispatchers.IO).launch { dropbox.backup() }
    }

    private fun howWasYourDay(c: Fortuna, date: ChronoLocalDate) {
        val nm = c.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(
                REMIND, c.getString(R.string.ntfReminderTitle),
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = c.getString(R.string.ntfReminderDesc) }
        )
        nm.notify(
            CHANNEL, Notification.Builder(c, REMIND)
                .setSmallIcon(R.drawable.logo_monochrome)
                .setColor(c.resources.getColor(R.color.CP, c.theme))
                .setContentTitle(c.getString(R.string.ntfReminder))
                .setContentIntent(UiTools.openInDate(c, date, 0))
                .setAutoCancel(true)
                .setShowWhen(false)
                .build()
        )
    }
}
