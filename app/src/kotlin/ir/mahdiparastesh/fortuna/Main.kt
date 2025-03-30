package ir.mahdiparastesh.fortuna

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
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
import ir.mahdiparastesh.fortuna.Vita.Companion.showScore
import ir.mahdiparastesh.fortuna.databinding.MainBinding
import ir.mahdiparastesh.fortuna.sect.BackupDialog
import ir.mahdiparastesh.fortuna.sect.HelpDialog
import ir.mahdiparastesh.fortuna.sect.SearchAdapter
import ir.mahdiparastesh.fortuna.sect.SearchDialog
import ir.mahdiparastesh.fortuna.sect.StatisticsDialog
import ir.mahdiparastesh.fortuna.sect.TodayWidget
import ir.mahdiparastesh.fortuna.util.Dropbox
import ir.mahdiparastesh.fortuna.util.NumberUtils.groupDigits
import ir.mahdiparastesh.fortuna.util.NumberUtils.toKey
import ir.mahdiparastesh.fortuna.util.NumberUtils.z
import ir.mahdiparastesh.fortuna.util.Numerals
import ir.mahdiparastesh.fortuna.util.Sexbook
import ir.mahdiparastesh.fortuna.util.UiTools
import ir.mahdiparastesh.fortuna.util.UiTools.SEXBOOK
import ir.mahdiparastesh.fortuna.util.UiTools.blur
import ir.mahdiparastesh.fortuna.util.UiTools.color
import ir.mahdiparastesh.fortuna.util.UiTools.pdcf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.time.chrono.ChronoLocalDate
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

class Main : FragmentActivity(), NavigationView.OnNavigationItemSelectedListener {
    val c: Fortuna by lazy { applicationContext as Fortuna }
    val b: MainBinding by lazy { MainBinding.inflate(layoutInflater) }
    val m: Model by viewModels()
    private var rollingLuna = true // "true" in order to trick onItemSelected
    private var rollingLunaWithAnnus = false
    private var rollingAnnusItself = false
    var dropbox: Dropbox? = null

    companion object {
        const val EXTRA_LUNA = "luna"
        const val EXTRA_DIES = "dies"
        const val HANDLE_NEW_DAY = 0
        const val HANDLE_SEXBOOK_LOADED = 1
        var handler: Handler? = null
    }

    class Model : ViewModel() {
        var sexbook: Sexbook.Data? = null
        var emojis = listOf<String>()
        var changingVar: Int? = null
        var changingVarScore: Int? = null
        var changingVarEmoji: String? = null
        var changingVarVerbum: String? = null
        var lastSearchQuery: String? = null
        var searchResults = ArrayList<SearchAdapter.Result>()
        var showingDate: Int? = null
        var compareDatesWith: ChronoLocalDate? = null
        var changingConfigForLunaSpinner = false
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
        for (n in Numerals.all.indices) {
            val nt = Numerals.all[n]
            b.toolbar.menu.add(0, nt.id, n, nt.name).apply {
                isCheckable = true
                isChecked = c.sp.getString(UiTools.SP_NUMERAL_TYPE, UiTools.SP_NUMERAL_TYPE_DEF) ==
                        (nt.jClass?.simpleName ?: UiTools.SP_NUMERAL_TYPE_DEF)
            }
        }
        ((b.toolbar[1] as ActionMenuView)[0] as ImageView)
            .tooltipText = getString(R.string.numerals)
        b.toolbar.setOnMenuItemClickListener { mItem ->
            c.sp.edit {
                putString(
                    UiTools.SP_NUMERAL_TYPE,
                    Numerals.all.find { it.id == mItem.itemId }?.jClass?.simpleName
                        ?: UiTools.SP_NUMERAL_TYPE_DEF
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
                c.luna = "${z(b.annus.text, 4)}.${z(i + 1)}"
                c.date = c.lunaToDate(c.luna!!)
                updateGrid()
            }
        } // setOnItemClickListener cannot be used with a spinner
        b.annus.addTextChangedListener {
            if (it.toString().length !=/*<*/ 4 && !rollingAnnusItself) {
                rollingAnnusItself = false; return@addTextChangedListener; }
            if (rollingLunaWithAnnus) {
                rollingLunaWithAnnus = false; return@addTextChangedListener; }
            if (c.luna?.split(".")?.get(0) == it.toString() || resolvingIntent)
                return@addTextChangedListener // don't move this before rollingLunaWithAnnus

            c.luna = "${z(it, 4)}.${z(b.luna.selectedItemPosition + 1)}"
            c.date = c.lunaToDate(c.luna!!)
            updateGrid()
        }
        b.annus.setOnEditorActionListener { v, actionId, _ ->
            if (actionId != EditorInfo.IME_ACTION_GO) return@setOnEditorActionListener true
            val annus = v.text.toString()
            if (annus == "" || annus == "-") return@setOnEditorActionListener true
            c.luna = "${z(annus, 4)}.${z(b.luna.selectedItemPosition + 1)}"
            c.date = c.lunaToDate(c.luna!!)
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
                        c.todayDate = c.createDate()
                        c.todayLuna = c.todayDate.toKey()
                        updateGrid()
                    }
                    HANDLE_SEXBOOK_LOADED -> {
                        m.sexbook = msg.obj as Sexbook.Data
                        (b.grid.adapter as? Grid)?.apply {
                            sexbook = cacheSexbook()
                            m.changingVar?.also { i ->
                                cvTvSexbook?.appendCrushDates(
                                    i.toShort(),
                                    dailyCalendar(i)[ChronoField.YEAR].toShort()
                                )
                                cvTvSexbook?.appendSexReports(i)
                            }
                        }
                    }
                }
            }
        }

        // Nyx
        c.requiredPermissions.forEach {
            if (ActivityCompat.checkSelfPermission(c, it) != PackageManager.PERMISSION_GRANTED)
                reqPermLauncher.launch(it)
        }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(Nyx.CHANNEL)
        Nyx.alarm(c) // Nyx.test(c)

        // miscellaneous
        addOnNewIntentListener { it.resolveIntent() }
        m.emojis = InputStreamReader(resources.openRawResource(R.raw.emojis), Charsets.UTF_8)
            .use { it.readText().split(' ') }
        if (try {
                packageManager.getPackageInfo(SEXBOOK, 0)
                true
            } catch (_: PackageManager.NameNotFoundException) {
                false
            } && m.sexbook == null
        ) Sexbook(c).start()
    }

    var firstResume = true // a new instance of Main is created on a configuration change.
    override fun onResume() {
        super.onResume()

        c.todayDate = c.createDate()
        c.todayLuna = c.todayDate.toKey()

        if (c.luna == null) {
            intent.resolveIntent()
            if (!c.stored.exists()) {
                c.vita!![c.luna!!] = Luna(c.date.lengthOfMonth())
                c.vita!!.save()
            } else c.vita?.reform()
        } else if (firstResume) {
            updateGrid()

            // restore saved states (null-safe)

            m.changingVar?.also {
                (b.grid.adapter as? Grid)
                    ?.changeVar(it, c.date.with(ChronoField.DAY_OF_MONTH, (it + 1).toLong()))
            }
            m.showingDate?.also { (b.grid.adapter as? Grid)?.detailDate(it, c.date) }
        }
        firstResume = false

        dropbox?.onResume()
    }

    var resolvingIntent = false
    private fun Intent.resolveIntent() {
        if (m.changingVar != null) return
        resolvingIntent = true
        val extraLuna = getStringExtra(EXTRA_LUNA)
        if (extraLuna != null) {
            c.luna = extraLuna
            c.date = c.lunaToDate(extraLuna)
        } else {
            c.date = c.createDate()
            c.luna = c.date.toKey()
        }
        updatePanel()
        updateGrid()
        if (hasExtra(EXTRA_DIES)) getIntExtra(EXTRA_DIES, 1).also {
            (b.grid.adapter as? Grid)
                ?.changeVar(it - 1, c.date.with(ChronoField.DAY_OF_MONTH, it.toLong()))
        }
        resolvingIntent = false
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navToday -> {
                if (c.todayLuna != c.date.toKey()) {
                    c.date = c.createDate()
                    onCalendarChanged(); }
                closeDrawer()
            }
            R.id.navSearch -> SearchDialog().show(supportFragmentManager, "srch")
            R.id.navStat -> StatisticsDialog().show(supportFragmentManager, "stat")
            R.id.navExport -> exportLauncher.launch(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = UiTools.VITA_MIME_TYPE
                putExtra(Intent.EXTRA_TITLE, c.getString(R.string.export_file))
            })
            R.id.navImport -> importLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = UiTools.VITA_MIME_TYPE
            })
            R.id.navSend -> c.vita?.export()?.also { sendFile(it, R.string.export_file) }
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
                        .use { fos -> fos.write(c.vita?.export()) }
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
                imported = Vita(c, data)
            } catch (_: Exception) {
                Toast.makeText(
                    c, R.string.importReadError, Toast.LENGTH_LONG
                ).show(); return@registerForActivityResult
            }
            MaterialAlertDialogBuilder(this@Main).apply {
                setTitle(c.resources.getString(R.string.navImport))
                setMessage(c.resources.getString(R.string.askImport))
                setPositiveButton(R.string.yes) { _, _ ->
                    c.vita = imported.also { vita -> vita.save() }
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
                        type = UiTools.VITA_MIME_TYPE
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
        b.annus.setText(c.date[ChronoField.YEAR].toString())
        b.luna.setSelection(c.date[ChronoField.MONTH_OF_YEAR])
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
            b.lunaSize.text = UiTools.showBytes(this@Main, it.luna.size)
            b.lunaSize.isInvisible = it.luna.size == 0L
            b.verbumIcon.isVisible = it.luna.verbum?.isNotBlank() == true
            b.emoji.text = it.luna.emoji
            b.emoji.isVisible = it.luna.emoji?.isNotBlank() == true
        }
        b.grid.layoutParams.apply {
            height = ((resources.getDimension(R.dimen.gridItemHeight) *
                    ceil(c.date.lengthOfMonth().toFloat() / 5f)) +
                    resources.getDimension(R.dimen.gridAdditionalHeight)).toInt()
        }
    }

    /** Updates the overflow menu after the numeral system is changed. */
    private fun updateOverflow() {
        b.toolbar.menu.forEachIndexed { i, item ->
            item.isChecked = c.sp.getString(UiTools.SP_NUMERAL_TYPE, UiTools.SP_NUMERAL_TYPE_DEF) ==
                    (Numerals.all[i].jClass?.simpleName ?: UiTools.SP_NUMERAL_TYPE_DEF)
        }
    }

    /** Moves the calendar in months for N times. */
    private fun rollCalendar(forward: Boolean, nTimes: Long = 1L) {
        c.date =
            if (forward) c.date.plus(nTimes, ChronoUnit.MONTHS)
            else c.date.minus(nTimes, ChronoUnit.MONTHS)
        onCalendarChanged()
    }

    /** Updates everything whenever the calendar changes. */
    fun onCalendarChanged() {
        c.luna = c.date.toKey()
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
    fun maximaForStats(cal: ChronoLocalDate, key: String = cal.toKey()): Int? =
        if (key == c.todayLuna) c.todayDate[ChronoField.DAY_OF_MONTH] // this month
        else if (cal.isBefore(c.todayDate)) cal.lengthOfMonth() // past months
        else null // future months

    fun saveDies(luna: Luna, i: Int, score: Float?, emoji: String?, verbum: String?) {
        if (i != -1) {
            luna.diebus[i] = score
            luna.verba[i] = verbum?.ifBlank { null }
            luna.emojis[i] = emoji?.ifBlank { null }
        } else {
            luna.default = score
            luna.verbum = verbum?.ifBlank { null }
            luna.emoji = emoji?.ifBlank { null }
        }
        c.vita!![c.luna!!] = luna
        c.vita!!.save()
        updateGrid()
    }

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
}
