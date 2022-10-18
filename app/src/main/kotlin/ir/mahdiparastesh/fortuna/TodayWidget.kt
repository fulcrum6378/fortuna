package ir.mahdiparastesh.fortuna

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.icu.util.Calendar
import android.widget.RemoteViews
import ir.mahdiparastesh.fortuna.Main.Companion.sp

class TodayWidget : AppWidgetProvider() {
    override fun onUpdate(c: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { appWidgetId ->
            val views = RemoteViews(c.packageName, R.layout.today_widget).apply {
                val cal = Main.calType.newInstance()
                setTextViewText(
                    R.id.dies, ItemDay.diesNum(cal[Calendar.DAY_OF_MONTH],
                        c.sp().getString(Main.SP_NUMERAL_TYPE, Main.arNumType)
                            .let { if (it == Main.arNumType) null else it })
                )
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
