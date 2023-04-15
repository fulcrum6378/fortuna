package ir.mahdiparastesh.fortuna.misc

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.res.Configuration
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.RemoteViews
import androidx.core.graphics.drawable.toBitmap
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import ir.mahdiparastesh.fortuna.Kit
import ir.mahdiparastesh.fortuna.Kit.sp
import ir.mahdiparastesh.fortuna.R

class TodayWidget : AppWidgetProvider() {
    override fun onUpdate(c: Context, manager: AppWidgetManager, ids: IntArray) {
        super.onUpdate(c, manager, ids)
        ids.forEach { id -> manager.updateAppWidget(id, update(c)) }
    }

    override fun onAppWidgetOptionsChanged(
        c: Context, manager: AppWidgetManager, id: Int, newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(c, manager, id, newOptions)
        manager.updateAppWidget(id, update(c))
    }

    companion object {
        fun update(c: Context) = RemoteViews(c.packageName, R.layout.today_widget).apply {
            val cal = Kit.calType.newInstance()
            val den = c.resources.displayMetrics.density
            setImageViewBitmap(
                R.id.bg, MaterialShapeDrawable(
                    ShapeAppearanceModel.Builder().setAllCorners(CornerFamily.CUT, den * 4f).build()
                ).let {
                    it.fillColor = c.resources.getColorStateList(R.color.todayWidget, null)
                    c.resources.configuration.orientation
                    val size = arrayOf(30, 50)
                    if (c.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                        size.reverse()
                    it.toBitmap((den * size[0]).toInt(), (den * size[1]).toInt())
                })
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
