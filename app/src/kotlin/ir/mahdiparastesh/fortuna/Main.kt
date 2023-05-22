package ir.mahdiparastesh.fortuna

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.RippleDrawable
import android.icu.util.Calendar
import android.os.*
import android.util.SparseArray
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import ir.mahdiparastesh.fortuna.Kit.blur
import ir.mahdiparastesh.fortuna.Kit.calType
import ir.mahdiparastesh.fortuna.Kit.color
import ir.mahdiparastesh.fortuna.Kit.moveCalendarInMonths
import ir.mahdiparastesh.fortuna.Kit.pdcf
import ir.mahdiparastesh.fortuna.Kit.resetHours
import ir.mahdiparastesh.fortuna.Kit.sp
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
import ir.mahdiparastesh.fortuna.misc.Numerals
import ir.mahdiparastesh.fortuna.misc.Sexbook
import ir.mahdiparastesh.fortuna.misc.TodayWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.math.ceil

class Main : ComponentActivity(), NavigationView.OnNavigationItemSelectedListener {
    val c: Context get() = applicationContext
    val b: MainBinding by lazy { MainBinding.inflate(layoutInflater) }
    val m: Model by viewModels()
    val sp: SharedPreferences by lazy { sp() }
    var todayCalendar: Calendar = calType.newInstance().resetHours()
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
                isChecked = sp.getString(Kit.SP_NUMERAL_TYPE, Kit.arNumType) ==
                    (nt.jClass?.simpleName ?: Kit.arNumType)
            }
        }
        ((b.toolbar[1] as ActionMenuView)[0] as ImageView)
            .tooltipText = getString(R.string.numerals)
        b.toolbar.setOnMenuItemClickListener { mItem ->
            sp.edit {
                putString(
                    Kit.SP_NUMERAL_TYPE,
                    Numerals.all.find { it.id == mItem.itemId }?.jClass?.simpleName ?: Kit.arNumType
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

                    HANDLE_SEXBOOK_LOADED -> {
                        m.sexbook = msg.obj as Sexbook.Data
                        (b.grid.adapter as? Grid)?.apply {
                            sexbook = cacheSexbook()
                            m.changingVar?.also { i ->
                                cvTvSexbook?.appendCrushBirthdays(i)
                                cvTvSexbook?.appendSexReports(i)
                            }
                        }
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
        ) Sexbook(this).start()
        (c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
            cancel(Nyx.CHANNEL)
            createNotificationChannel(
                NotificationChannel(
                    Nyx.REMIND, c.getString(R.string.ntfReminderTitle),
                    NotificationManager.IMPORTANCE_LOW
                ).apply { description = getString(R.string.ntfReminderDesc) }
            )
        }
        addOnNewIntentListener { it.resolveIntent() }
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
                if (todayLuna != m.calendar.toKey()) {
                    m.calendar = calType.newInstance()
                    onCalendarChanged(); }
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
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
            if (res.resultCode != RESULT_OK) return@registerForActivityResult
            var data: String? = null
            try {
                c.contentResolver.openFileDescriptor(res.data!!.data!!, "r")?.use { des ->
                    data = FileInputStream(des.fileDescriptor).use { it.readBytes() }
                        .toString(Charsets.UTF_8)
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
            item.isChecked = sp.getString(Kit.SP_NUMERAL_TYPE, Kit.arNumType) ==
                (Numerals.all[i].jClass?.simpleName ?: Kit.arNumType)
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

    /**
     * Opens an AlertDialog for statistics.
     *
     * Making statistics in a way that it'll show every year since the minimum scored days till the
     * maximum scored days could cause a super huge table in irregular scoring accident, e. g. if
     * someone accidentally or deliberately score a day in year 25 or 8000.
     */
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

            val cp = getColor(R.color.CP)
            val cs = getColor(R.color.CS)
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
                    for (butt in arrayOf(backup, export)) butt.background = RippleDrawable(
                        ColorStateList.valueOf(
                            color(com.google.android.material.R.attr.colorPrimaryVariant)
                        ), null, MaterialShapeDrawable(
                            ShapeAppearanceModel.Builder().apply {
                                val dim = resources.getDimension(R.dimen.mediumCornerSize)
                                var premise = butt == backup
                                if (resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL)
                                    premise = !premise
                                if (premise) setBottomLeftCorner(CornerFamily.CUT, dim)
                                else setBottomRightCorner(CornerFamily.CUT, dim)
                            }.build()
                        )
                    )
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
                        if (!Vita.Backup(c).exists()) return@setOnClickListener
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
        return "${z(d[Calendar.YEAR], 4)}.${z(d[Calendar.MONTH] + 1)}." +
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

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
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

    class Model : ViewModel() {
        var vita: Vita? = null
        var luna: String? = null
        lateinit var calendar: Calendar
        var sexbook: Sexbook.Data? = null
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
  * Search in Vita
  * Select multiple day cells in order to score them once
  * Calculate a day's distance from another specific day
  */
