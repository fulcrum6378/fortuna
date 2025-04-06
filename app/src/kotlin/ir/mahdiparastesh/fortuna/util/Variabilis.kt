package ir.mahdiparastesh.fortuna.util

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.NumberPicker
import ir.mahdiparastesh.fortuna.R
import ir.mahdiparastesh.fortuna.util.Kit.color

/** An improved version of [NumberPicker] with a customised font */
class Variabilis(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
    NumberPicker(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) :
            this(context, attrs, android.R.attr.numberPickerStyle)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            this(context, attrs, defStyleAttr, 0)

    @Suppress("RemoveRedundantQualifierName")
    override fun addView(child: View?, params: ViewGroup.LayoutParams?) {
        super.addView(child, params)
        if (child is EditText) {
            child.typeface = context.resources.getFont(R.font.quattrocento_bold)
            child.isEnabled = false
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                child.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
                // this text size is applied differently than NumberPicker.textSize()
                child.setTextColor((context as ContextThemeWrapper).color(android.R.attr.textColor))
            }
        }
    }
}
