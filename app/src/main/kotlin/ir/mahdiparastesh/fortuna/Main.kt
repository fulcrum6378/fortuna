package ir.mahdiparastesh.fortuna

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.icu.util.Calendar
import android.os.Bundle
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModel
import com.google.android.material.navigation.NavigationView
import ir.mahdiparastesh.fortuna.Vita.Companion.toKey
import ir.mahdiparastesh.fortuna.Vita.Companion.z
import ir.mahdiparastesh.fortuna.databinding.MainBinding

// adb connect adb-R58MA6P17YD-MEhKF8._adb-tls-connect._tcp

@Suppress("MemberVisibilityCanBePrivate")
class Main : ComponentActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var c: Context
    lateinit var b: MainBinding
    val m: Model by viewModels()
    lateinit var toggleNav: ActionBarDrawerToggle
    lateinit var fontTitle: Typeface // may be removed later
    lateinit var fontBold: Typeface // may be removed later
    lateinit var fontRegular: Typeface // may be removed later

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        c = applicationContext
        b = MainBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Fonts
        fontTitle = resources.getFont(R.font.morris_roman_black)
        // From https://www.1001fonts.com/morris-roman-font.html
        fontBold = resources.getFont(R.font.quattrocento_bold)
        fontRegular = resources.getFont(R.font.quattrocento_regular)
        // From https://www.1001fonts.com/quattrocento-font.html
        // Later try https://www.1001fonts.com/day-roman-font.html

        // Toolbar & Navigation
        toggleNav = ActionBarDrawerToggle(
            this, b.root, b.toolbar, R.string.navOpen, R.string.navClose
        ).apply {
            b.root.addDrawerListener(this)
            isDrawerIndicatorEnabled = true
            syncState()
        }
        b.nav.setNavigationItemSelectedListener(this)
        b.toolbar.navigationIcon?.colorFilter =
            pdcf(color(com.google.android.material.R.attr.colorOnPrimary))

        // Header
        b.luna.adapter = ArrayAdapter(
            c, R.layout.spinner, resources.getStringArray(R.array.luna)
        ).apply { setDropDownViewResource(R.layout.spinner_dd) }
        b.luna.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, i: Int, id: Long) {
                m.lunaChanged = true
                m.luna = "${z(b.annus.text, 4)}.${z(i + 1)}"
                updateGrid()
            }
        }
        b.annus.addTextChangedListener {
            if (it.toString().length != 4) return@addTextChangedListener
            m.luna = "${z(it, 4)}.${z(b.luna.selectedItemPosition + 1)}"
            updateGrid()
        }
    }

    override fun onResume() {
        super.onResume()
        m.vita = Vita.load(c)
        if (!m.lunaChanged) PersianCalendar().apply {
            m.luna = toKey()
            updateHeader(this)
            if (!Vita.Stored(c).exists()) {
                m.vita!![toKey()] = Vita.emptyLuna()
                m.vita!!.save(c)
            }
        }
        updateGrid()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return true
    }

    fun updateHeader(cal: PersianCalendar) {
        b.annus.setText(cal[Calendar.YEAR].toString())
        b.luna.setSelection(cal[Calendar.MONTH])
    }

    fun updateGrid() {
        b.grid.adapter = ItemDay(this)
    }

    @ColorInt
    fun color(@AttrRes attr: Int) = TypedValue().apply {
        theme.resolveAttribute(attr, this, true)
    }.data

    fun pdcf(@ColorInt color: Int) = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)

    class Model : ViewModel() {
        var vita: Vita? = null
        lateinit var luna: String
        var lunaChanged = false
    }
}
