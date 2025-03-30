package ir.mahdiparastesh.fortuna.sect

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import androidx.core.graphics.drawable.toBitmap
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import ir.mahdiparastesh.fortuna.Fortuna
import ir.mahdiparastesh.fortuna.R
import ir.mahdiparastesh.fortuna.util.Numerals
import ir.mahdiparastesh.fortuna.util.Numerals.write
import ir.mahdiparastesh.fortuna.util.UiTools
import java.time.temporal.ChronoField

/**
 * An app widget for notifying the user of the current date in the current calendar with the chosen
 * type of numerals.
 *
 * [AppWidgetProvider] sucks when using ADB; it doesn't update properly!
 */
class TodayWidget : AppWidgetProvider() {
    override fun onUpdate(c: Context, manager: AppWidgetManager, ids: IntArray) {
        super.onUpdate(c, manager, ids)
        ids.forEach { id ->
            manager.updateAppWidget(id, update(c.applicationContext as Fortuna))
        }
    }

    companion object {
        fun update(c: Fortuna) = RemoteViews(c.packageName, R.layout.today_widget).apply {
            val cal = c.createDate()
            val den = c.resources.displayMetrics.density
            setImageViewBitmap(
                R.id.bg, MaterialShapeDrawable(
                    ShapeAppearanceModel.Builder().setAllCorners(CornerFamily.CUT, den * 8f).build()
                ).let {
                    it.fillColor = c.resources.getColorStateList(R.color.todayWidget, null)
                    val size = if (!c.isLandscape()) arrayOf(70, 100) else arrayOf(140, 50)
                    it.toBitmap((den * size[0]).toInt(), (den * size[1]).toInt())
                })
            setOnClickPendingIntent(R.id.root, UiTools.openInDate(c, cal, 1))
            setTextViewText(
                R.id.dies, Numerals.build(
                    c.sp.getString(UiTools.SP_NUMERAL_TYPE, UiTools.SP_NUMERAL_TYPE_DEF)
                        .let { if (it == UiTools.SP_NUMERAL_TYPE_DEF) null else it }
                ).write(cal[ChronoField.DAY_OF_MONTH])
            )
            val month = c.resources.getStringArray(R.array.luna)[cal[ChronoField.MONTH_OF_YEAR]]
            if (c.isLandscape()) {
                setTextViewText(R.id.luna, "$month\n${cal[ChronoField.YEAR]}")
                setTextViewText(R.id.year, "")
            } else {
                setTextViewText(R.id.luna, month)
                setTextViewText(R.id.year, "${cal[ChronoField.YEAR]}")
            }
        } // RemoteViews has no getters.

        fun externalUpdate(c: Fortuna) {
            AppWidgetManager.getInstance(c).updateAppWidget(
                ComponentName(c, TodayWidget::class.java.name), update(c)
            )
        }
    }
}
