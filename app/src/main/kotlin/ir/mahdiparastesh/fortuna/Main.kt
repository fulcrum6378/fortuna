package ir.mahdiparastesh.fortuna

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.icu.util.Calendar
import android.net.Uri
import android.os.*
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.ActionMenuView
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.core.view.forEachIndexed
import androidx.core.view.get
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import ir.mahdiparastesh.fortuna.ItemDay.Companion.changeVar
import ir.mahdiparastesh.fortuna.Vita.Companion.lunaMaxima
import ir.mahdiparastesh.fortuna.Vita.Companion.mean
import ir.mahdiparastesh.fortuna.Vita.Companion.showScore
import ir.mahdiparastesh.fortuna.Vita.Companion.toCalendar
import ir.mahdiparastesh.fortuna.Vita.Companion.toKey
import ir.mahdiparastesh.fortuna.Vita.Companion.z
import ir.mahdiparastesh.fortuna.databinding.MainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@SuppressLint("InvalidFragmentVersionForActivityResult")
class Main : ComponentActivity(), NavigationView.OnNavigationItemSelectedListener {
    val c: Context get() = applicationContext
    val b: MainBinding by lazy { MainBinding.inflate(layoutInflater) }
    val m: Model by viewModels() // belongs to ComponentActivity
    val sp: SharedPreferences by lazy { getSharedPreferences("settings", Context.MODE_PRIVATE) }
    val varFieldBg: MaterialShapeDrawable by lazy {
        MaterialShapeDrawable(
            ShapeAppearanceModel.Builder()
                .setAllCorners(CornerFamily.CUT, c.resources.getDimension(R.dimen.smallCornerSize))
                .build()
        ).apply { fillColor = c.resources.getColorStateList(R.color.variabilis_field, null) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)

        // Toolbar & Navigation
        ActionBarDrawerToggle(
            this, b.root, b.toolbar, R.string.navOpen, R.string.navClose
        ).apply {
            b.root.addDrawerListener(this)
            isDrawerIndicatorEnabled = true
            syncState()
        }
        b.nav.setNavigationItemSelectedListener(this)
        b.toolbar.navigationIcon?.colorFilter =
            pdcf(color(com.google.android.material.R.attr.colorOnPrimary))
        for (n in BaseNumeral.all.indices) {
            val nt = BaseNumeral.all[n]
            b.toolbar.menu.add(0, nt.id, n, nt.name).apply {
                isCheckable = true
                isChecked = sp.getString(SP_NUMERAL_TYPE, arNumType) ==
                        (nt.jClass?.canonicalName ?: arNumType)
            }
        }
        // AndroidX Toolbar children:
        // 0 => title(AppCompatTextView)
        // 1 => actionButtons(ActionMenuView<ActionMenuPresenter$OverflowMenuButton>)
        // 2 => drawerButton(AppCompatImageButton)
        ((b.toolbar[1] as ActionMenuView)[0] as ImageView)
            .tooltipText = getString(R.string.numerals)
        b.toolbar.setOnMenuItemClickListener { mItem ->
            sp.edit {
                putString(
                    SP_NUMERAL_TYPE,
                    BaseNumeral.all.find { it.id == mItem.itemId }?.jClass?.canonicalName
                        ?: arNumType
                )
            }; updateGrid(); updateOverflow(); shake(); true
        }

        // Panel
        b.luna.adapter = ArrayAdapter(
            this, R.layout.spinner, resources.getStringArray(R.array.luna)
        ).apply { setDropDownViewResource(R.layout.spinner_dd) }
        b.luna.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, i: Int, id: Long) {
                if (firstResume) return
                if (rollingLuna) {
                    rollingLuna = false; return; }
                m.luna = "${z(b.annus.text, 4)}.${z(i + 1)}"
                m.calendar = m.luna!!.toCalendar(calType)
                updateGrid()
            }
        }
        b.annus.addTextChangedListener {
            if (it.toString().length != 4 || firstResume) return@addTextChangedListener
            if (rollingAnnus) {
                rollingAnnus = false
                return@addTextChangedListener; }
            m.luna = "${z(it, 4)}.${z(b.luna.selectedItemPosition + 1)}"
            m.calendar = m.luna!!.toCalendar(calType)
            updateGrid()
        }
        b.annusUp.setOnClickListener { rollAnnus(1) }
        b.annusDown.setOnClickListener { rollAnnus(-1) }
        b.annusUp.setOnLongClickListener { rollAnnus(5); true }
        b.annusDown.setOnLongClickListener { rollAnnus(-5); true }
        b.next.setOnClickListener { rollCalendar(true) }
        b.prev.setOnClickListener { rollCalendar(false) }
        b.next.setOnLongClickListener { rollCalendar(true, 6); true }
        b.prev.setOnLongClickListener { rollCalendar(false, 6); true }
        b.defVar.setOnClickListener {
            m.thisLuna().changeVar(this@Main, -1)
        }
        b.verbum.setColorFilter(color(android.R.attr.textColor))

        // Miscellaneous
        if (try {
                packageManager.getPackageInfo(SEXBOOK, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        ) Sexbook().start()
        (c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
            cancel(Reminder.CHANNEL)
            createNotificationChannel(
                NotificationChannel(
                    Reminder.REMIND, c.getString(R.string.ntfReminderDesc),
                    NotificationManager.IMPORTANCE_LOW
                ).apply { description = getString(R.string.ntfReminderDesc) }
            )
        }
        Reminder.alarm(c) // Reminder.test(c)
        if (m.showingStat) stat()
        if (m.showingHelp) help()
        m.showingDate?.also { ItemDay.showDate(this, it) }
    }

    private var firstResume = true
    override fun onResume() {
        super.onResume()
        m.vita = Vita.load(c)
        if (m.luna == null) {
            val extraLuna = intent.getStringExtra(EXTRA_LUNA)
            if (firstResume && extraLuna != null) {
                m.luna = extraLuna
                m.calendar = extraLuna.toCalendar(calType)
            } else {
                m.calendar = calType.newInstance()
                m.luna = m.calendar.toKey()
            }
            updatePanel()
            if (!Vita.Stored(c).exists()) {
                m.vita!![m.luna!!] = Luna(m.calendar)
                m.vita!!.save(c)
            }
        }
        updateGrid()
        if (firstResume) {
            if (intent.hasExtra(EXTRA_DIES))
                intent?.getIntExtra(EXTRA_DIES, 0)
                    ?.also { m.vita!![m.luna!!]?.changeVar(this@Main, it - 1) }
            else m.changingVar?.also { m.vita!![m.luna!!]?.changeVar(this@Main, it) }
            m.vita?.reform(c)
        } else b.annus.clearFocus()
        firstResume = false
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navToday -> {
                m.calendar = calType.newInstance()
                m.luna = m.calendar.toKey()
                rollingAnnus = true
                rollingLuna = true
                updatePanel()
                updateGrid()
                b.root.closeDrawer(GravityCompat.START, true)
            }
            R.id.navStat -> stat()
            R.id.navExport -> exportLauncher.launch(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = Vita.MIME_TYPE
                putExtra(Intent.EXTRA_TITLE, c.getString(R.string.export_file))
            })
            R.id.navImport -> importLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = Vita.MIME_TYPE
            })
            R.id.navSend -> {
                val binary = m.vita?.export(c) ?: return false
                val exported = File(cacheDir, c.getString(R.string.export_file))
                CoroutineScope(Dispatchers.IO).launch {
                    runCatching {
                        FileOutputStream(exported).use { it.write(binary) }
                    }.onSuccess {
                        withContext(Dispatchers.Main) {
                            Intent(Intent.ACTION_SEND).apply {
                                type = Vita.MIME_TYPE
                                putExtra(
                                    Intent.EXTRA_STREAM,
                                    FileProvider.getUriForFile(c, packageName, exported)
                                )
                            }.also { startActivity(it) }
                        }
                    }
                }
            }
            R.id.navHelp -> help()
        }
        return true
    }

    private val exportLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            val bExp = try {
                c.contentResolver.openFileDescriptor(it.data!!.data!!, "w")?.use { des ->
                    FileOutputStream(des.fileDescriptor).use { fos ->
                        fos.write(m.vita?.export(c))
                    }
                }
                true
            } catch (ignored: Exception) {
                false
            }
            Toast.makeText(
                c, if (bExp) R.string.done else R.string.exportUndone, Toast.LENGTH_LONG
            ).show()
        }

    private val importLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK) return@registerForActivityResult
            var data: String? = null
            try {
                c.contentResolver.openFileDescriptor(it.data!!.data!!, "r")?.use { des ->
                    val sb = StringBuffer()
                    FileInputStream(des.fileDescriptor).use { fis ->
                        var i: Int
                        while (fis.read().also { r -> i = r } != -1) sb.append(i.toChar())
                    }
                    data = sb.toString()
                }
                data!!
            } catch (e: Exception) {
                Toast.makeText(c, R.string.importOpenError, Toast.LENGTH_LONG).show()
                return@registerForActivityResult
            }
            val imported: Vita
            try {
                imported = Vita.loads(data!!)
            } catch (e: Exception) {
                Toast.makeText(c, R.string.importReadError, Toast.LENGTH_LONG).show()
                return@registerForActivityResult
            }
            MaterialAlertDialogBuilder(this@Main).apply {
                setTitle(c.resources.getString(R.string.navImport))
                setMessage(c.resources.getString(R.string.askImport))
                setPositiveButton(R.string.yes) { _, _ ->
                    m.vita = imported.also { vita -> vita.save(c) }
                    updateGrid()
                    Toast.makeText(c, R.string.done, Toast.LENGTH_LONG).show()
                }
                setNegativeButton(R.string.no, null)
            }.show()
        }

    private fun updatePanel() {
        b.annus.setText(m.calendar[Calendar.YEAR].toString())
        b.luna.setSelection(m.calendar[Calendar.MONTH])
    }

    fun updateGrid() {
        b.grid.adapter = ItemDay(this).also {
            b.defVar.text = it.luna.default.showScore()
            b.lunaMean.text = it.luna.mean(m.calendar.lunaMaxima()).toString()
            b.verbum.vis(it.luna.verbum?.isNotBlank() == true)
        }
    }

    private fun updateOverflow() {
        b.toolbar.menu.forEachIndexed { i, item ->
            item.isChecked = sp.getString(SP_NUMERAL_TYPE, arNumType) ==
                    (BaseNumeral.all[i].jClass?.canonicalName ?: arNumType)
        }
    }

    private var rollingAnnus = false
    private var rollingLuna = true // in order to trick onItemSelected
    private fun rollCalendar(up: Boolean, repeat: Int = 1) {
        repeat(repeat) {
            m.calendar.roll(Calendar.MONTH, up)
            if ((up && m.calendar[Calendar.MONTH] == 0) ||
                (!up && m.calendar[Calendar.MONTH] == m.calendar.getActualMaximum(Calendar.MONTH))
            ) m.calendar.roll(Calendar.YEAR, up)
        }
        m.luna = m.calendar.toKey()
        rollingAnnus = true
        rollingLuna = true
        updatePanel()
        updateGrid()
    }

    @SuppressLint("SetTextI18n")
    private fun rollAnnus(to: Int) {
        b.annus.setText((b.annus.text.toString().toInt() + to).toString())
    }

    private fun stat() {
        m.showingStat = true
        MaterialAlertDialogBuilder(this).apply {
            val scores = arrayListOf<Float>()
            m.vita?.forEach { key, luna ->
                for (v in 0 until key.toCalendar(calType).lunaMaxima())
                    (luna[v] ?: luna.default)?.also { scores.add(it) }
            }
            val sum = scores.sum()
            val text = getString(
                R.string.statText,
                (if (scores.isEmpty()) 0f else sum / scores.size.toFloat()).toString(),
                sum.toString(), scores.size.toString()
            )
            setTitle(R.string.navStat)
            setMessage(text)
            setPositiveButton(R.string.ok, null)
            setNeutralButton(R.string.copy) { _, _ ->
                (c.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?)?.setPrimaryClip(
                    ClipData.newPlainText(getString(R.string.fortunaStat), text)
                )
                Toast.makeText(c, R.string.done, Toast.LENGTH_SHORT).show()
            }
            setOnDismissListener { m.showingStat = false }
        }.show()
    }

    private fun help() {
        m.showingHelp = true
        MaterialAlertDialogBuilder(this).apply {
            setTitle(R.string.navHelp)
            setMessage(R.string.help)
            setPositiveButton(R.string.ok, null)
            setOnDismissListener { m.showingHelp = false }
        }.show()
    }

    @Suppress("DEPRECATION")
    fun shake(dur: Long = 40L) {
        val vib = (if (Build.VERSION.SDK_INT >= 31)
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        else getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
        vib.vibrate(VibrationEffect.createOneShot(dur, 100))
    }

    override fun onBackPressed() {
        if (b.root.isDrawerOpen(GravityCompat.START)) {
            b.root.closeDrawer(GravityCompat.START, true)
            return; }
        super.onBackPressed()
    }

    companion object {
        const val EXTRA_LUNA = "luna"
        const val EXTRA_DIES = "dies"
        const val SP_NUMERAL_TYPE = "numeral_type"
        const val arNumType = "0"
        const val SEXBOOK = "ir.mahdiparastesh.sexbook"
        val calType = when (BuildConfig.FLAVOR) {
            "gregorian" -> android.icu.util.GregorianCalendar::class.java
            /*"persian"*/ else -> PersianCalendar::class.java
        }

        val otherCalendars = arrayOf(
            android.icu.util.GregorianCalendar::class.java,
            PersianCalendar::class.java,
            android.icu.util.IslamicCalendar::class.java,
            android.icu.util.ChineseCalendar::class.java,
            android.icu.util.IndianCalendar::class.java,
            android.icu.util.CopticCalendar::class.java,
            android.icu.util.HebrewCalendar::class.java,
        ).filter { it != calType }

        @ColorInt
        fun ContextThemeWrapper.color(@AttrRes attr: Int) = TypedValue().apply {
            theme.resolveAttribute(attr, this, true)
        }.data

        fun pdcf(@ColorInt color: Int, mode: PorterDuff.Mode = PorterDuff.Mode.SRC_IN) =
            PorterDuffColorFilter(color, mode)

        fun View.vis(bb: Boolean = true) {
            visibility = if (bb) View.VISIBLE else View.GONE
        }
    }

    inner class Sexbook : Thread() {
        override fun run() {
            val places = hashMapOf<Long, String>()
            contentResolver.query(
                Uri.parse("content://$SEXBOOK/place"),
                null, null, null, null
            )?.use { cur ->
                while (cur.moveToNext())
                    places[cur.getLong(0)] = cur.getString(1)
            }
            val cur = contentResolver.query(
                Uri.parse("content://$SEXBOOK/report"),
                null, null, null, "time ASC" // DESC
            ) ?: return
            val sexbook = arrayListOf<Sex>()
            while (cur.moveToNext()) {
                val cal = calType.newInstance()
                cal.timeInMillis = cur.getLong(1)
                sexbook.add(
                    Sex(
                        cur.getLong(0),
                        cal[Calendar.YEAR].toShort(), (cal[Calendar.MONTH] + 1).toShort(),
                        cal[Calendar.DAY_OF_MONTH].toShort(), cal[Calendar.HOUR_OF_DAY].toByte(),
                        cal[Calendar.MINUTE].toByte(), cal[Calendar.SECOND].toByte(),
                        cur.getString(2), cur.getShort(3).toByte(),
                        cur.getString(4), cur.getInt(5) == 1,
                        places[cur.getLong(6)]
                    )
                )
            }
            cur.close()
            m.sexbook = sexbook.toList()
        }
    }

    data class Sex(
        val id: Long, val year: Short, val month: Short, val day: Short, // never compare bytes!
        val hour: Byte, val minute: Byte, val second: Byte,
        val key: String, val type: Byte, val desc: String, val accurate: Boolean, val place: String?
    )

    class Model : ViewModel() {
        var vita: Vita? = null
        var luna: String? = null
        lateinit var calendar: Calendar
        var sexbook: List<Sex>? = null
        var changingVar: Int? = null
        var changingVarScore: Int? = null
        var changingVarVerbum: String? = null
        var showingStat = false
        var showingHelp = false
        var showingDate: Int? = null

        fun thisLuna() = vita?.find(luna!!) ?: Luna(calendar)
    }
}
