package ir.mahdiparastesh.fortuna

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.icu.util.Calendar
import android.widget.RemoteViews
import ir.mahdiparastesh.fortuna.Main.Companion.sp

class TodayWidget : AppWidgetProvider() {
    override fun onUpdate(c: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { appWidgetId ->
            appWidgetManager.updateAppWidget(appWidgetId, update(c))
        }
    }

    companion object {
        fun update(c: Context) = RemoteViews(c.packageName, R.layout.today_widget).apply {
            val cal = Main.calType.newInstance()
            setOnClickPendingIntent(R.id.root, Main.openInDate(c, cal))
            setTextViewText(
                R.id.dies, ItemDay.diesNum(cal[Calendar.DAY_OF_MONTH],
                    c.sp().getString(Main.SP_NUMERAL_TYPE, Main.arNumType)
                        .let { if (it == Main.arNumType) null else it })
            )
        }

        fun externalUpdate(c: Context) {
            AppWidgetManager.getInstance(c).updateAppWidget(
                ComponentName(c, TodayWidget::class.java.name), update(c)
            )
        }
    }
}
