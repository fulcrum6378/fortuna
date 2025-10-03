package ir.mahdiparastesh.fortuna.sect

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import ir.mahdiparastesh.fortuna.Fortuna
import ir.mahdiparastesh.fortuna.R
import java.time.temporal.ChronoField

/**
 * An app widget for notifying the user of the current date in the current calendar with the chosen
 * type of numerals
 */
class TodayWidget : AppWidgetProvider() {

    override fun onUpdate(c: Context, manager: AppWidgetManager, ids: IntArray) {
        super.onUpdate(c, manager, ids)
        ids.forEach { id ->
            manager.updateAppWidget(id, update(c.applicationContext as Fortuna))
        }
    }

    companion object {

        fun externalUpdate(c: Fortuna) {
            AppWidgetManager.getInstance(c).updateAppWidget(
                ComponentName(c, TodayWidget::class.java.name), update(c)
            )
        }

        private fun update(c: Fortuna) = RemoteViews(c.packageName, R.layout.today_widget).apply {
            val date = c.chronology.dateNow()
            val den = c.resources.displayMetrics.density
            /*FIX-ME setImageViewBitmap(
                R.id.bg, MaterialShapeDrawable(
                    ShapeAppearanceModel.Builder().setAllCorners(CornerFamily.CUT, den * 8f).build()
                ).let {
                    it.fillColor = c.resources.getColorStateList(R.color.todayWidget, null)
                    val size = if (!c.isLandscape()) arrayOf(70, 100) else arrayOf(140, 50)
                    it.toBitmap((den * size[0]).toInt(), (den * size[1]).toInt())
                })
            setOnClickPendingIntent(R.id.root, UiTools.openInDate(c, date, 1))
            setTextViewText(
                R.id.dies, Numerals.build(
                    c.sp.getString(Fortuna.SP_NUMERAL_TYPE, Fortuna.SP_NUMERAL_TYPE_DEF)
                        .let { if (it == Fortuna.SP_NUMERAL_TYPE_DEF) null else it }
                ).of(date[ChronoField.DAY_OF_MONTH])
            )*/
            val month =
                c.resources.getStringArray(R.array.luna)[date[ChronoField.MONTH_OF_YEAR] - 1]
            if (c.isLandscape()) {
                setTextViewText(R.id.luna, "$month\n${date[ChronoField.YEAR]}")
                setTextViewText(R.id.year, "")
            } else {
                setTextViewText(R.id.luna, month)
                setTextViewText(R.id.year, "${date[ChronoField.YEAR]}")
            }
        }  // RemoteViews has no getters.
    }
}
