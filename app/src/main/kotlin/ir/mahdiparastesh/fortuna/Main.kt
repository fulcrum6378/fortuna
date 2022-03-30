package ir.mahdiparastesh.fortuna

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.MenuItem
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.lifecycle.ViewModel
import com.google.android.material.navigation.NavigationView
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
    lateinit var calendar: PersianCalendar

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
    }

    override fun onResume() {
        super.onResume()
        calendar = PersianCalendar()
        b.grid.adapter = ItemDay(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return true
    }

    @ColorInt
    fun ContextThemeWrapper.color(@AttrRes attr: Int) = TypedValue().apply {
        theme.resolveAttribute(attr, this, true)
    }.data

    fun pdcf(@ColorInt color: Int) = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)

    class Model : ViewModel()

}
