package ir.mahdiparastesh.fortuna

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.icu.util.Calendar
import android.os.*
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.ActionMenuView
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.core.view.forEachIndexed
import androidx.core.view.get
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import ir.mahdiparastesh.fortuna.Kit.SEXBOOK
import ir.mahdiparastesh.fortuna.Kit.blur
import ir.mahdiparastesh.fortuna.Kit.calType
import ir.mahdiparastesh.fortuna.Kit.color
import ir.mahdiparastesh.fortuna.Kit.create
import ir.mahdiparastesh.fortuna.Kit.groupDigits
import ir.mahdiparastesh.fortuna.Kit.moveCalendarInMonths
import ir.mahdiparastesh.fortuna.Kit.pdcf
import ir.mahdiparastesh.fortuna.Kit.resetHours
import ir.mahdiparastesh.fortuna.Kit.sp
import ir.mahdiparastesh.fortuna.Kit.z
import ir.mahdiparastesh.fortuna.Vita.Companion.lunaMaxima
import ir.mahdiparastesh.fortuna.Vita.Companion.mean
import ir.mahdiparastesh.fortuna.Vita.Companion.showScore
import ir.mahdiparastesh.fortuna.Vita.Companion.toCalendar
import ir.mahdiparastesh.fortuna.Vita.Companion.toKey
import ir.mahdiparastesh.fortuna.databinding.MainBinding
import ir.mahdiparastesh.fortuna.misc.Numerals
import ir.mahdiparastesh.fortuna.misc.SearchAdapter
import ir.mahdiparastesh.fortuna.misc.Sexbook
import ir.mahdiparastesh.fortuna.misc.TodayWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import kotlin.math.ceil

class Main : FragmentActivity(), NavigationView.OnNavigationItemSelectedListener {
    val c: Context get() = applicationContext
    val b: MainBinding by lazy { MainBinding.inflate(layoutInflater) }
    val m: Model by viewModels()
    val sp: SharedPreferences by lazy { sp() }
    var todayCalendar: Calendar = calType.create().resetHours()
    var todayLuna: String = todayCalendar.toKey()
    private var rollingLuna = true // "true" in order to trick onItemSelected
    private var rollingLunaWithAnnus = false
    private var rollingAnnusItself = false
    val varFieldBg: MaterialShapeDrawable by lazy {
        MaterialShapeDrawable(
            ShapeAppearanceModel.Builder()
                .setAllCorners(CornerFamily.CUT, resources.getDimension(R.dimen.smallCornerSize))
                .build()
        ).apply { fillColor = resources.getColorStateList(R.color.varField, null) }
    }

    class Fortuna : Application() {
        override fun onConfigurationChanged(newConfig: Configuration) {
            super.onConfigurationChanged(newConfig)
            TodayWidget.externalUpdate(applicationContext)
        }
    }

    companion object {
        const val EXTRA_LUNA = "luna"
        const val EXTRA_DIES = "dies"
        const val HANDLE_NEW_DAY = 0
        const val HANDLE_SEXBOOK_LOADED = 1
        var handler: Handler? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)
        m.vita = Vita.load(c)

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
        for (n in Numerals.all.indices) {
            val nt = Numerals.all[n]
            b.toolbar.menu.add(0, nt.id, n, nt.name).apply {
                isCheckable = true
                isChecked = sp.getString(Kit.SP_NUMERAL_TYPE, Kit.defNumType) ==
                        (nt.jClass?.simpleName ?: Kit.defNumType)
            }
        }
        ((b.toolbar[1] as ActionMenuView)[0] as ImageView)
            .tooltipText = getString(R.string.numerals)
        b.toolbar.setOnMenuItemClickListener { mItem ->
            sp.edit {
                putString(
                    Kit.SP_NUMERAL_TYPE,
                    Numerals.all.find { it.id == mItem.itemId }?.jClass?.simpleName
                        ?: Kit.defNumType
                )
            }; updateGrid(); updateOverflow(); shake(); true
        }

        // Panel
        b.luna.adapter = ArrayAdapter(
            this, R.layout.spinner, resources.getStringArray(R.array.luna)
        ).apply { setDropDownViewResource(R.layout.spinner_dd) }
        b.luna.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>) {}
            override fun onItemSelected(parent: AdapterView<*>, view: View?, i: Int, id: Long) {
                if (resolvingIntent) return
                if (rollingLuna) {
                    rollingLuna = false; return; }
                if (m.changingConfigForLunaSpinner) {
                    m.changingConfigForLunaSpinner = false; return; }
                m.luna = "${z(b.annus.text, 4)}.${z(i + 1)}"
                m.calendar = m.luna!!.toCalendar(calType)
                updateGrid()
            }
        } // setOnItemClickListener cannot be used with a spinner
        b.annus.addTextChangedListener {
            if (it.toString().length !=/*<*/ 4 && !rollingAnnusItself) {
                rollingAnnusItself = false; return@addTextChangedListener; }
            if (rollingLunaWithAnnus) {
                rollingLunaWithAnnus = false; return@addTextChangedListener; }
            if (m.luna?.split(".")?.get(0) == it.toString() || resolvingIntent)
                return@addTextChangedListener // don't move this before rollingLunaWithAnnus

            m.luna = "${z(it, 4)}.${z(b.luna.selectedItemPosition + 1)}"
            m.calendar = m.luna!!.toCalendar(calType)
            updateGrid()
        }
        b.annus.setOnEditorActionListener { v, actionId, _ ->
            if (actionId != EditorInfo.IME_ACTION_GO) return@setOnEditorActionListener true
            val annus = v.text.toString()
            if (annus == "" || annus == "-") return@setOnEditorActionListener true
            m.luna = "${z(annus, 4)}.${z(b.luna.selectedItemPosition + 1)}"
            m.calendar = m.luna!!.toCalendar(calType)
            updateGrid()
            b.annus.blur(c)
            return@setOnEditorActionListener true
        }
        b.annusUp.setOnClickListener { rollAnnus(1) }
        b.annusDown.setOnClickListener { rollAnnus(-1) }
        b.annusUp.setOnLongClickListener { rollAnnus(5); true }
        b.annusDown.setOnLongClickListener { rollAnnus(-5); true }
        b.next.setOnClickListener { rollCalendar(true) }
        b.prev.setOnClickListener { rollCalendar(false) }
        b.next.setOnLongClickListener { rollCalendar(true, 6); true }
        b.prev.setOnLongClickListener { rollCalendar(false, 6); true }
        b.defVar.setOnClickListener { (b.grid.adapter as? Grid)?.changeVar(-1) }
        b.verbumIcon.setColorFilter(color(android.R.attr.textColor))

        // Handler
        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    HANDLE_NEW_DAY -> {
                        todayCalendar = calType.create().resetHours()
                        todayLuna = todayCalendar.toKey()
                        updateGrid()
                    }
                    HANDLE_SEXBOOK_LOADED -> {
                        m.sexbook = msg.obj as Sexbook.Data
                        (b.grid.adapter as? Grid)?.apply {
                            sexbook = cacheSexbook()
                            m.changingVar?.also { i ->
                                cvTvSexbook?.appendCrushDates(i, dailyCalendar(i))
                                cvTvSexbook?.appendSexReports(i)
                            }
                        }
                    }
                }
            }
        }

        // Nyx
        Kit.reqPermissions.forEach {
            if (ActivityCompat.checkSelfPermission(c, it) != PackageManager.PERMISSION_GRANTED)
                reqPermLauncher.launch(it)
        }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(Nyx.CHANNEL)
        Nyx.alarm(c) // Nyx.test(c)

        // Miscellaneous
        addOnNewIntentListener { it.resolveIntent() }
        m.emojis = InputStreamReader(resources.openRawResource(R.raw.emojis), Charsets.UTF_8)
            .use { it.readText().split(' ') }
        if (try {
                packageManager.getPackageInfo(SEXBOOK, 0)
                true
            } catch (_: PackageManager.NameNotFoundException) {
                false
            } && m.sexbook == null
        ) Sexbook(this).start()
    }

    var firstResume = true // a new instance of Main is created on a configuration change.
    override fun onResume() {
        super.onResume()
        if (m.luna == null) {
            intent.resolveIntent()
            if (!Vita.Stored(c).exists()) {
                m.vita!![m.luna!!] = Luna(m.calendar)
                m.vita!!.save(c)
            } else m.vita?.reform(c)
        } else if (firstResume) {
            updateGrid()

            // Restore saved states (null-safe)
            m.changingVar?.also {
                (b.grid.adapter as? Grid)?.changeVar(it,
                    (m.calendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, it) })
            }
            m.showingDate?.also { (b.grid.adapter as? Grid)?.detailDate(it, m.calendar) }
        }
        firstResume = false
    }

    var resolvingIntent = false
    private fun Intent.resolveIntent() {
        if (m.changingVar != null) return
        resolvingIntent = true
        val extraLuna = getStringExtra(EXTRA_LUNA)
        if (extraLuna != null) {
            m.luna = extraLuna
            m.calendar = extraLuna.toCalendar(calType)
        } else {
            m.calendar = calType.create()
            m.luna = m.calendar.toKey()
        }
        updatePanel()
        updateGrid()
        if (hasExtra(EXTRA_DIES)) getIntExtra(EXTRA_DIES, 1).also {
            (b.grid.adapter as? Grid)?.changeVar(it - 1,
                (m.calendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, it) })
        }
        resolvingIntent = false
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navToday -> {
                if (todayLuna != m.calendar.toKey()) {
                    m.calendar = calType.create()
                    onCalendarChanged(); }
                closeDrawer()
            }
            R.id.navSearch -> SearchDialog().show(
                supportFragmentManager,
                SearchAdapter.FRAGMENT_TAG
            )
            R.id.navStat -> StatisticsDialog().show(supportFragmentManager, "stat")
            R.id.navExport -> exportLauncher.launch(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = Vita.MIME_TYPE
                putExtra(Intent.EXTRA_TITLE, c.getString(R.string.export_file))
            })
            R.id.navImport -> importLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = Vita.MIME_TYPE
            })
            R.id.navSend -> m.vita?.export(c)?.also { sendFile(it, R.string.export_file) }
            R.id.navBackup -> BackupDialog().show(supportFragmentManager, "back")
            R.id.navHelp -> HelpDialog().show(supportFragmentManager, "help")
        }
        return true
    }

    /** Requests all the required permissions. (currently only for notifications in Android 13+) */
    private val reqPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) return@registerForActivityResult
        (c.getSystemService(NOTIFICATION_SERVICE) as NotificationManager).apply {
            cancel(Nyx.CHANNEL)
            createNotificationChannel(
                NotificationChannel(
                    Nyx.REMIND, c.getString(R.string.ntfReminderTitle),
                    NotificationManager.IMPORTANCE_LOW
                ).apply { description = getString(R.string.ntfReminderDesc) }
            )
        }
    }

    /** Invoked when a file is ready to be exported. */
    private val exportLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != RESULT_OK) return@registerForActivityResult
            val bExp = try {
                c.contentResolver.openFileDescriptor(it.data!!.data!!, "w")?.use { des ->
                    FileOutputStream(des.fileDescriptor)
                        .use { fos -> fos.write(m.vita?.export(c)) }
                }
                true
            } catch (_: Exception) {
                false
            }
            Toast.makeText(
                c, if (bExp) R.string.done else R.string.failed, Toast.LENGTH_LONG
            ).show()
        }

    /** Invoked when a file is ready to be imported. */
    private val importLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
            if (res.resultCode != RESULT_OK) return@registerForActivityResult
            var data: String? = null
            try {
                c.contentResolver.openFileDescriptor(res.data!!.data!!, "r")?.use { des ->
                    data = FileInputStream(des.fileDescriptor).use { it.readBytes() }
                        .toString(Charsets.UTF_8)
                }
                data!!
            } catch (_: Exception) {
                Toast.makeText(
                    c, R.string.importOpenError, Toast.LENGTH_LONG
                ).show(); return@registerForActivityResult
            }
            val imported: Vita
            try {
                imported = Vita.loads(data)
            } catch (_: Exception) {
                Toast.makeText(
                    c, R.string.importReadError, Toast.LENGTH_LONG
                ).show(); return@registerForActivityResult
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

    /** Caches the data and lets it be shared. */
    fun sendFile(binary: ByteArray, @StringRes fileName: Int) {
        val exported = File(cacheDir, c.getString(fileName))
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
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }.also { startActivity(it) }
                }
            }
        }
    }

    /** Updates year and month inputs of the top panel. */
    @SuppressLint("SetTextI18n")
    private fun updatePanel() {
        b.annus.setText(m.calendar[Calendar.YEAR].toString())
        b.luna.setSelection(m.calendar[Calendar.MONTH])
    }

    /** Refreshes the [Grid] and adjusts its size. */
    @SuppressLint("SetTextI18n")
    fun updateGrid() {
        if (b.grid.adapter == null) b.grid.adapter = Grid(this)
        else {
            b.grid.invalidateViews()
            (b.grid.adapter as Grid).onRefresh()
        }
        (b.grid.adapter as Grid).also {
            b.defVar.text = it.luna.default.showScore()
            b.lunaMean.text = "x̄: " + it.luna.mean(it.maximumStats ?: 0).groupDigits(6)
            b.lunaSize.text = Kit.showBytes(this@Main, it.luna.size)
            b.lunaSize.isInvisible = it.luna.size == 0L
            b.verbumIcon.isVisible = it.luna.verbum?.isNotBlank() == true
            b.emoji.text = it.luna.emoji
            b.emoji.isVisible = it.luna.emoji?.isNotBlank() == true
        }
        b.grid.layoutParams.apply {
            height = ((resources.getDimension(R.dimen.gridItemHeight) *
                    ceil(m.calendar.lunaMaxima().toFloat() / 5f)) +
                    resources.getDimension(R.dimen.gridAdditionalHeight)).toInt()
        }
    }

    /** Updates the overflow menu after the numeral system is changed. */
    private fun updateOverflow() {
        b.toolbar.menu.forEachIndexed { i, item ->
            item.isChecked = sp.getString(Kit.SP_NUMERAL_TYPE, Kit.defNumType) ==
                    (Numerals.all[i].jClass?.simpleName ?: Kit.defNumType)
        }
    }

    /** Moves the calendar in months for N times. */
    private fun rollCalendar(forward: Boolean, nTimes: Int = 1) {
        repeat(nTimes) { m.calendar.moveCalendarInMonths(forward) }
        onCalendarChanged()
    }

    /** Updates everything whenever the calendar changes. */
    fun onCalendarChanged() {
        m.luna = m.calendar.toKey()
        rollingLunaWithAnnus = true
        rollingLuna = true
        updatePanel()
        updateGrid()
        b.annus.blur(c)
    }

    /** Moves the calendar into a different year. */
    @SuppressLint("SetTextI18n")
    private fun rollAnnus(to: Int) {
        rollingAnnusItself = true
        b.annus.setText((b.annus.text.toString().toInt() + to).toString())
        b.annus.blur(c)
    }

    /**
     * Calculates the maximum date for getting a mean value for statistics, ignores the future.
     * @return null if the given month is the future
     */
    fun maximaForStats(cal: Calendar, key: String = cal.toKey()): Int? =
        if (key == todayLuna) todayCalendar[Calendar.DAY_OF_MONTH] // this month
        else if (cal.timeInMillis < todayCalendar.timeInMillis) cal.lunaMaxima() // past months
        else null // future months

    /** Proper implementation of Vibration in across different supported APIs. */
    @Suppress("DEPRECATION")
    fun shake(dur: Long = 40L) {
        (if (Build.VERSION.SDK_INT >= 31)
            (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        else getSystemService(VIBRATOR_SERVICE) as Vibrator)
            .vibrate(VibrationEffect.createOneShot(dur, 100))
    }

    fun closeDrawer() {
        b.root.closeDrawer(GravityCompat.START, true)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        if (b.root.isDrawerOpen(GravityCompat.START)) {
            closeDrawer()
            return; }
        @Suppress("DEPRECATION") super.onBackPressed()
    }

    override fun onStop() {
        super.onStop()
        TodayWidget.externalUpdate(c)
    }

    override fun onDestroy() {
        m.changingConfigForLunaSpinner = true
        handler = null
        super.onDestroy()
    }

    class Model : ViewModel() {
        var vita: Vita? = null
        var luna: String? = null
        lateinit var calendar: Calendar
        var sexbook: Sexbook.Data? = null
        var emojis = listOf<String>()
        var changingVar: Int? = null
        var changingVarScore: Int? = null
        var changingVarEmoji: String? = null
        var changingVarVerbum: String? = null
        var lastSearchQuery: String? = null
        var searchResults = ArrayList<SearchAdapter.Result>()
        var showingDate: Int? = null
        var compareDatesWith: Calendar? = null
        var changingConfigForLunaSpinner = false

        fun thisLuna() = vita?.find(luna!!) ?: Luna(calendar)
    }
}

/* TODO:
  * - Select multiple day cells in order to score them once; needs custom selection
  * - Backup reminder for Fortuna; record latest export! (notify inside the app)
  */
