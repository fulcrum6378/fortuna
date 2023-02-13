package ir.mahdiparastesh.fortuna

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.icu.util.Calendar
import android.net.Uri
import android.os.*
import android.util.SparseArray
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.ActionMenuView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.util.containsKey
import androidx.core.util.forEach
import androidx.core.view.GravityCompat
import androidx.core.view.forEachIndexed
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import ir.mahdiparastesh.fortuna.Kit.SEXBOOK
import ir.mahdiparastesh.fortuna.Kit.calType
import ir.mahdiparastesh.fortuna.Kit.moveCalendarInMonths
import ir.mahdiparastesh.fortuna.Kit.resetHours
import ir.mahdiparastesh.fortuna.Kit.toValue
import ir.mahdiparastesh.fortuna.Kit.z
import ir.mahdiparastesh.fortuna.Vita.Companion.lunaMaxima
import ir.mahdiparastesh.fortuna.Vita.Companion.mean
import ir.mahdiparastesh.fortuna.Vita.Companion.showScore
import ir.mahdiparastesh.fortuna.Vita.Companion.toCalendar
import ir.mahdiparastesh.fortuna.Vita.Companion.toKey
import ir.mahdiparastesh.fortuna.databinding.BackupBinding
import ir.mahdiparastesh.fortuna.databinding.MainBinding
import ir.mahdiparastesh.fortuna.databinding.WholeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import kotlin.math.ceil

@SuppressLint("InvalidFragmentVersionForActivityResult")
class Main : ComponentActivity(), NavigationView.OnNavigationItemSelectedListener {
    val c: Context get() = applicationContext
    val b: MainBinding by lazy { MainBinding.inflate(layoutInflater) }
    val m: Model by viewModels() // belongs to ComponentActivity
    val sp: SharedPreferences by lazy { sp() }
    var todayCalendar: Calendar = calType.newInstance().resetHours()
    var todayLuna: String = todayCalendar.toKey()
    private var rollingLuna = true // "true" in order to trick onItemSelected
    private var rollingLunaWithAnnus = false
    private var rollingAnnusItself = false
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
                    Numerals.all.find { it.id == mItem.itemId }?.jClass?.canonicalName
                        ?: arNumType
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
                        todayCalendar = calType.newInstance().resetHours()
                        todayLuna = todayCalendar.toKey()
                        updateGrid()
                    }
                    HANDLE_SEXBOOK_LOADED -> (b.grid.adapter as? Grid)?.apply {
                        sexbook = cacheSexbook()
                        m.changingVar?.also { i -> cvTvSexbook?.showSexbook(i) }
                    }
                }
            }
        }

        // Restore saved states
        if (m.showingStat) stat()
        if (m.showingHelp) help()

        // Miscellaneous
        if (Kit.reqPermissions.isNotEmpty())
            ActivityCompat.requestPermissions(this, Kit.reqPermissions, 0)
        if (try {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(SEXBOOK, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            } && m.sexbook == null
        ) Sexbook().start()
        (c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
            cancel(Nyx.CHANNEL)
            createNotificationChannel(
                NotificationChannel(
                    Nyx.REMIND, c.getString(R.string.ntfReminderTitle),
                    NotificationManager.IMPORTANCE_LOW
                ).apply { description = getString(R.string.ntfReminderDesc) }
            )
        }
        Nyx.alarm(c) // Nyx.test(c)
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.resolveIntent()
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
            m.calendar = calType.newInstance()
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
                m.calendar = calType.newInstance()
                onCalendarChanged()
                closeDrawer()
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
            R.id.navSend -> m.vita?.export(c)?.also { sendFile(it, R.string.export_file) }
            R.id.navBackup -> navBackup()
            R.id.navHelp -> help()
        }
        return true
    }

    /*override fun onRequestPermissionsResult(code: Int, arr: Array<out String>, res: IntArray) {
        super.onRequestPermissionsResult(code, arr, res)
        if (res.isNotEmpty() && res[0] == PackageManager.PERMISSION_GRANTED) DO SOMETHING
    }*/

    /** Invoked when a file is ready to be exported. */
    private val exportLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != RESULT_OK) return@registerForActivityResult
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

    /** Invoked when a file is ready to be imported. */
    private val importLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != RESULT_OK) return@registerForActivityResult
            var data: String? = null
            try {
                c.contentResolver.openFileDescriptor(it.data!!.data!!, "r")?.use { des ->
                    data = FileInputStream(des.fileDescriptor).readBytes().toString(Charsets.UTF_8)
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

    /** Caches the data and lets it be shared. */
    private fun sendFile(binary: ByteArray, @StringRes fileName: Int) {
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
                    }.also { startActivity(it) }
                }
            }
        }
    }

    /** Updates year and month inputs of the top panel. */
    private fun updatePanel() {
        b.annus.setText(m.calendar[Calendar.YEAR].toString())
        b.luna.setSelection(m.calendar[Calendar.MONTH])
    }

    /** Refreshes the {@link Grid} and adjusts its size. */
    fun updateGrid() {
        if (b.grid.adapter == null) b.grid.adapter = Grid(this)
        else {
            b.grid.invalidateViews()
            (b.grid.adapter as Grid).onRefresh()
        }
        (b.grid.adapter as Grid).also {
            b.defVar.text = it.luna.default.showScore()
            b.lunaMean.text = it.luna.mean(m.calendar.lunaMaxima()).toString()
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
            item.isChecked = sp.getString(SP_NUMERAL_TYPE, arNumType) ==
                    (Numerals.all[i].jClass?.canonicalName ?: arNumType)
        }
    }

    /** Moves the calendar in months for N times. */
    private fun rollCalendar(forward: Boolean, nTimes: Int = 1) {
        repeat(nTimes) { m.calendar.moveCalendarInMonths(forward) }
        onCalendarChanged()
    }

    /** Updates everything whenever the calendar changes. */
    private fun onCalendarChanged() {
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

    /** Opens an AlertDialog for statistics. */
    private fun stat() {
        if (m.showingStat && !firstResume) return
        m.showingStat = true
        var dialogue: AlertDialog? = null
        dialogue = MaterialAlertDialogBuilder(this).apply {
            val scores = arrayListOf<Float>()
            val keyMeanMap = hashMapOf<String, Float>()
            m.vita?.forEach { key, luna ->
                val lunaScores = arrayListOf<Float>()
                for (v in 0 until key.toCalendar(calType).lunaMaxima())
                    (luna[v] ?: luna.default)?.also { lunaScores.add(it) }
                scores.addAll(lunaScores)
                keyMeanMap[key] = lunaScores.sum() / lunaScores.size.toFloat()
            } // don't use Luna.mean() for efficiency.
            val sum = scores.sum()
            val text = getString(
                R.string.statText,
                (if (scores.isEmpty()) 0f else sum / scores.size.toFloat()).toString(),
                sum.toString(), scores.size.toString()
            )

            val maxMonths = calType.newInstance().getMaximum(Calendar.MONTH) + 1
            val meanMap = SparseArray<Array<Float?>>()
            keyMeanMap.forEach { (key, mean) ->
                val spl = key.split(".")
                val y = spl[0].toInt()
                val m = spl[1].toInt() - 1
                if (!meanMap.containsKey(y)) meanMap[y] = Array(maxMonths) { null }
                meanMap[y][m] = mean
            }
            val bw = WholeBinding.inflate(layoutInflater)
            val cp = color(com.google.android.material.R.attr.colorPrimary)
            val cs = color(com.google.android.material.R.attr.colorSecondary)
            val cellH = resources.getDimension(R.dimen.statCellHeight).toInt()
            val nullCellColour = ContextCompat.getColor(c, R.color.statCell)
            val monthNames = resources.getStringArray(R.array.luna)
            meanMap.forEach { year, array ->
                bw.years.addView(TextView(this@Main).apply {
                    setText(year.toString())
                    textSize = cellH.toFloat() * 0.25f
                    gravity = Gravity.CENTER_VERTICAL
                }, LinearLayout.LayoutParams(-2, cellH))

                val tr = LinearLayout(this@Main)
                tr.orientation = LinearLayout.HORIZONTAL
                tr.weightSum = maxMonths.toFloat()
                array.forEachIndexed { month, score ->
                    tr.addView(View(this@Main).apply {
                        setBackgroundColor(
                            when {
                                score != null && score > 0f -> Color.valueOf(
                                    cp.red.toValue(), cp.green.toValue(), cp.blue.toValue(),
                                    score / Vita.MAX_RANGE
                                ).toArgb()
                                score != null && score < 0f -> Color.valueOf(
                                    cs.red.toValue(), cs.green.toValue(), cs.blue.toValue(),
                                    -score / Vita.MAX_RANGE
                                ).toArgb()
                                score != null -> Color.TRANSPARENT
                                else -> nullCellColour
                            }
                        )
                        tooltipText = "${monthNames[month]} $year${score?.let { "\n$it" } ?: ""}"
                        setOnClickListener(object : Kit.DoubleClickListener() {
                            override fun onDoubleClick() {
                                m.calendar = calType.newInstance().apply {
                                    set(Calendar.YEAR, year)
                                    set(Calendar.MONTH, month)
                                    set(Calendar.DAY_OF_MONTH, 1)
                                    resetHours()
                                }
                                onCalendarChanged()
                                dialogue?.cancel()
                                closeDrawer()
                            }
                        })
                    }, LinearLayout.LayoutParams(0, cellH, 1f)
                        .apply { setMargins(1, 1, 1, 1) })
                }
                bw.table.addView(tr, LinearLayout.LayoutParams(-1, cellH))
            }
            bw.sv.post {
                bw.sv.smoothScrollTo(
                    0, (bw.body.bottom + bw.sv.paddingBottom) - (bw.sv.scrollY + bw.sv.height)
                )
            }

            setTitle(R.string.navStat)
            setMessage(text)
            setView(bw.root)
            setPositiveButton(R.string.ok, null)
            setNeutralButton(R.string.copy) { _, _ ->
                (c.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.setPrimaryClip(
                    ClipData.newPlainText(getString(R.string.fortunaStat), text)
                )
                Toast.makeText(c, R.string.done, Toast.LENGTH_SHORT).show()
            }
            setOnDismissListener { m.showingStat = false }
        }.show()
    }

    /**
     * Shows the status of the automatically backed-up file with 3 action buttons:<br />
     * Backup: manually backs up the data.<br />
     * Restore: overwrites the backup file on the main file.<br />
     * Export: exports the backup file.
     */
    private fun navBackup() {
        if (m.showingBack && !firstResume) return
        m.showingBack = true
        MaterialAlertDialogBuilder(this).apply {
            setTitle(R.string.backup)
            setMessage(R.string.backupDesc)
            setView(
                BackupBinding.inflate(layoutInflater).apply {
                    updateStatus()
                    backup.setOnClickListener { Vita.backup(c); updateStatus() }
                    restore.setOnClickListener {
                        MaterialAlertDialogBuilder(this@Main).apply {
                            setTitle(c.resources.getString(R.string.restore))
                            setMessage(
                                c.resources.getString(R.string.backupRestoreSure, lastBackup())
                            )
                            setPositiveButton(R.string.yes) { _, _ ->
                                m.vita = Vita.loads(
                                    FileInputStream(Vita.Backup(c)).use { String(it.readBytes()) }
                                ).also { vita -> vita.save(c) }
                                updateGrid()
                                Toast.makeText(c, R.string.done, Toast.LENGTH_LONG).show()
                            }
                            setNegativeButton(R.string.no, null)
                        }.show()
                    }
                    export.setOnClickListener {
                        sendFile(
                            FileInputStream(Vita.Backup(c)).use { it.readBytes() },
                            R.string.backup_file
                        )
                    }
                }.root
            )
            setCancelable(true)
            setOnDismissListener { m.showingBack = false }
        }.show()
    }

    /** @return the human-readable modification date of the backup. */
    private fun lastBackup(): String {
        val f = Vita.Backup(c)
        if (!f.exists()) return getString(R.string.never)
        val d = calType.newInstance().apply { timeInMillis = f.lastModified() }
        return "${z(d[Calendar.YEAR], 4)}.${z(d[Calendar.MONTH])}." +
                "${z(d[Calendar.DAY_OF_MONTH])} - ${z(d[Calendar.HOUR])}:" +
                "${z(d[Calendar.MINUTE])}:${z(d[Calendar.SECOND])}"
    }

    /** Updates the modification date of the backup file. */
    private fun BackupBinding.updateStatus() {
        status.text = getString(R.string.backupTime, lastBackup())
    }

    /** Shows an AlertDialog containing the guide for this app. */
    private fun help() {
        if (m.showingHelp && !firstResume) return
        m.showingHelp = true
        MaterialAlertDialogBuilder(this).apply {
            setTitle(R.string.navHelp)
            setMessage(R.string.help)
            setPositiveButton(R.string.ok, null)
            setOnDismissListener { m.showingHelp = false }
        }.show()
    }

    /** Helper class for vibration. */
    @Suppress("DEPRECATION")
    fun shake(dur: Long = 40L) {
        (if (Build.VERSION.SDK_INT >= 31)
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        else getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
            .vibrate(VibrationEffect.createOneShot(dur, 100))
    }

    private fun closeDrawer() {
        b.root.closeDrawer(GravityCompat.START, true)
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        if (b.root.isDrawerOpen(GravityCompat.START)) {
            closeDrawer()
            return; }
        super.onBackPressed()
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

    companion object {
        const val EXTRA_LUNA = "luna"
        const val EXTRA_DIES = "dies"
        const val SP_NUMERAL_TYPE = "numeral_type"
        const val arNumType = "0"
        const val HANDLE_NEW_DAY = 0
        const val HANDLE_SEXBOOK_LOADED = 1
        var handler: Handler? = null

        /** @return the main shared preferences instance; <code>settings.xml</code>. */
        fun Context.sp(): SharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)

        /** @return the colour value of this attribute resource from the theme. */
        @ColorInt
        fun ContextThemeWrapper.color(@AttrRes attr: Int) = TypedValue().apply {
            theme.resolveAttribute(attr, this, true)
        }.data

        /** @return the colour filter instance of this colour with the given PorterDuff.Mode. */
        fun pdcf(@ColorInt color: Int, mode: PorterDuff.Mode = PorterDuff.Mode.SRC_IN) =
            PorterDuffColorFilter(color, mode)

        /** Opens the specified date in the device's default calendar app. */
        fun openInDate(c: Context, cal: Calendar, req: Int): PendingIntent =
            PendingIntent.getActivity(
                c, req, Intent(c, Main::class.java)
                    .putExtra(EXTRA_LUNA, cal.toKey())
                    .putExtra(EXTRA_DIES, cal[Calendar.DAY_OF_MONTH]),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
            ) // A unique request code protects the PendingIntent from being recycled!

        /** Clears focus from an EditText. */
        fun EditText.blur(c: Context) {
            (c.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
                ?.hideSoftInputFromWindow(windowToken, 0)
            clearFocus()
        }
    }

    /**
     * Imports data from the Sexbook application in a separate thread.
     *
     * @see <a href="https://github.com/fulcrum1378/sexbook">Sexbook repository</a>
     * @see <a href="https://mahdiparastesh.ir/?fk=1">Mahdi Parastesh's personal website</a>
     */
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
            handler?.obtainMessage(HANDLE_SEXBOOK_LOADED)?.sendToTarget()
        }
    }

    /**
     * Data class containing the information about a sex record from Sexbook.
     *
     * @param id the unique id (Long)
     * @param year in this calendar (Short)
     * @param month in this calendar (Short)
     * @param day in this calendar (Short)
     * @param hour (Byte)
     * @param minute (Byte)
     * @param second (Byte)
     * @param key the raw input from the user indicating their crush's name (String)
     * @param type wet dream, masturbation, oral, anal or vaginal sex (Byte)
     * @param desc description (String)
     * @param accurate is this record accurate? (Boolean)
     * @param place the place where the sex happened (String?)
     *
     * @see Grid#showSexbook
     */
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
        var changingVarEmoji: String? = null
        var changingVarVerbum: String? = null
        var showingStat = false
        var showingBack = false
        var showingHelp = false
        var showingDate: Int? = null
        var changingConfigForLunaSpinner = false

        fun thisLuna() = vita?.find(luna!!) ?: Luna(calendar)
    }
}

/* TODO:
  * Select multiple day cells in order to score them once
  */
