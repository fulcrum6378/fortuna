package ir.mahdiparastesh.fortuna.misc

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.icu.util.Calendar
import android.widget.RemoteViews
import ir.mahdiparastesh.fortuna.Kit
import ir.mahdiparastesh.fortuna.Kit.sp
import ir.mahdiparastesh.fortuna.R

class TodayWidget : AppWidgetProvider() {
    override fun onUpdate(c: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { id -> manager.updateAppWidget(id, update(c)) }
    }

    companion object {
        fun update(c: Context) = RemoteViews(c.packageName, R.layout.today_widget).apply {
            val cal = Kit.calType.newInstance()
            setOnClickPendingIntent(R.id.root, Kit.openInDate(c, cal, 1))
            setTextViewText(
                R.id.dies, Numerals.make(cal[Calendar.DAY_OF_MONTH],
                    c.sp().getString(Kit.SP_NUMERAL_TYPE, Kit.arNumType)
                        .let { if (it == Kit.arNumType) null else it })
            )
            setTextViewText(
                R.id.luna, c.resources.getStringArray(R.array.luna)[cal[Calendar.MONTH]] +
                        "\n${cal[Calendar.YEAR]}"
            )
        } // RemoteViews has no getters.

        fun externalUpdate(c: Context) {
            AppWidgetManager.getInstance(c).updateAppWidget(
                ComponentName(c, TodayWidget::class.java.name), update(c)
            )
        }
    }
}
