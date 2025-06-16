package ir.mahdiparastesh.fortuna

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
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
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.ActionMenuView
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.forEachIndexed
import androidx.core.view.get
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import ir.mahdiparastesh.fortuna.base.MainPage
import ir.mahdiparastesh.fortuna.databinding.MainBinding
import ir.mahdiparastesh.fortuna.sect.BackupDialog
import ir.mahdiparastesh.fortuna.sect.ChronometerDialog
import ir.mahdiparastesh.fortuna.sect.HelpDialog
import ir.mahdiparastesh.fortuna.sect.SearchAdapter
import ir.mahdiparastesh.fortuna.sect.SearchDialog
import ir.mahdiparastesh.fortuna.sect.StatisticsDialog
import ir.mahdiparastesh.fortuna.sect.TodayWidget
import ir.mahdiparastesh.fortuna.sect.VariabilisDialog
import ir.mahdiparastesh.fortuna.util.AndroidUtils
import ir.mahdiparastesh.fortuna.util.Dropbox
import ir.mahdiparastesh.fortuna.util.MainHandler
import ir.mahdiparastesh.fortuna.util.NumberUtils.displayScore
import ir.mahdiparastesh.fortuna.util.NumberUtils.toKey
import ir.mahdiparastesh.fortuna.util.NumberUtils.z
import ir.mahdiparastesh.fortuna.util.Numerals
import ir.mahdiparastesh.fortuna.util.Sexbook
import ir.mahdiparastesh.fortuna.util.UiTools.blur
import ir.mahdiparastesh.fortuna.util.UiTools.color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.time.chrono.ChronoLocalDate
import java.time.temporal.ChronoField
import java.util.Locale
import kotlin.math.ceil

class Main : FragmentActivity(), MainPage, NavigationView.OnNavigationItemSelectedListener {

    override val c: Fortuna by lazy { applicationContext as Fortuna }
    val b: MainBinding by lazy { MainBinding.inflate(layoutInflater) }
    val m: Model by viewModels()
    var dropbox: Dropbox? = null

    val night: Boolean by lazy {
        resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }
    val cpl: FloatArray = floatArrayOf(0.296875f, 0.68359375f, 0.3125f)  // #4CAF50
    val cp: FloatArray by lazy {
        if (!night) cpl else floatArrayOf(0.01171875f, 0.296875f, 0.0234375f)  // #034C06
    }
    val csl: FloatArray = floatArrayOf(0.953125f, 0.26171875f, 0.2109375f)  // #F44336
    val cs: FloatArray by lazy {
        if (!night) csl else floatArrayOf(0.40234375f, 0.05078125f, 0.0234375f)  // #670D06
    }

    /** Improved drawable for the fields in [VariabilisDialog] and [ChronometerDialog] */
    val varFieldBg: MaterialShapeDrawable by lazy {
        MaterialShapeDrawable(
            ShapeAppearanceModel.Builder()
                .setAllCorners(CornerFamily.CUT, c.resources.getDimension(R.dimen.smallCornerSize))
                .build()
        ).apply { fillColor = c.resources.getColorStateList(R.color.varField, null) }
    }

    private var rollingLuna = true  // "true" in order to trick onItemSelected
    private var rollingLunaWithAnnus = false
    private var rollingAnnusItself = false

    class Model : ViewModel() {
        var sexbook: MutableLiveData<Sexbook.Data?> = MutableLiveData(null)
        var emojis = listOf<String>()
        var variabilisScore: Int? = null
        var variabilisEmoji: String? = null
        var variabilisVerbum: String? = null
        var lastSearchQuery: String? = null
        var searchResults = ArrayList<SearchAdapter.Result>()
        var compareDatesWith: ChronoLocalDate? = null
        var changingConfigForLunaSpinner = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            //SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle =  // do NOT use SystemBarStyle.auto()
                if (!night) SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                else SystemBarStyle.dark(Color.TRANSPARENT)
        )
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
        for (n in Numerals.all.indices) {
            val nt = Numerals.all[n]
            b.toolbar.menu.add(0, n, n, nt.name).apply {
                isCheckable = true
                isChecked = c.sp.getString(Fortuna.SP_NUMERAL_TYPE, Fortuna.SP_NUMERAL_TYPE_DEF) ==
                        (nt.name() ?: Fortuna.SP_NUMERAL_TYPE_DEF)
            }
        }
        ((b.toolbar[1] as ActionMenuView)[0] as ImageView)
            .tooltipText = getString(R.string.numerals)
        b.toolbar.setOnMenuItemClickListener { mItem ->
            c.sp.edit {
                putString(
                    Fortuna.SP_NUMERAL_TYPE,
                    Numerals.all[mItem.itemId].name() ?: Fortuna.SP_NUMERAL_TYPE_DEF
                )
            }
            updateGrid()
            updateOverflow()
            shake()
            true
        }

        updateGrid()  // a theme should be applied after ActionBarDrawerToggle is created.

        // Panel
        b.luna.adapter = ArrayAdapter(
            this, R.layout.spinner, resources.getStringArray(R.array.luna)
        ).apply { setDropDownViewResource(R.layout.spinner_dd) }
        updatePanel()
        b.luna.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>) {}
            override fun onItemSelected(parent: AdapterView<*>, view: View?, i: Int, id: Long) {
                if (rollingLuna) {
                    rollingLuna = false
                    return; }
                if (m.changingConfigForLunaSpinner) {
                    m.changingConfigForLunaSpinner = false
                    return; }

                c.luna = "${z(b.annus.text, 4)}.${z(i + 1)}"
                c.date = c.lunaToDate(c.luna)
                updateGrid()
            }
        }  // setOnItemClickListener cannot be used with a spinner
        b.annus.addTextChangedListener {
            if (it.toString().length !=/*<*/ 4 && !rollingAnnusItself)
                return@addTextChangedListener
            if (rollingLunaWithAnnus) {
                rollingLunaWithAnnus = false
                return@addTextChangedListener; }
            val year: Int = try {
                it.toString().toInt()
            } catch (_: NumberFormatException) {
                return@addTextChangedListener
            }
            if (c.date[ChronoField.YEAR] == year)
            // don't move this condition before rollingLunaWithAnnus
                return@addTextChangedListener

            c.luna = "${z(it, 4)}.${z(b.luna.selectedItemPosition + 1)}"
            c.date = c.lunaToDate(c.luna)
            updateGrid()
        }
        b.annus.setOnEditorActionListener { v, actionId, _ ->
            if (actionId != EditorInfo.IME_ACTION_GO) return@setOnEditorActionListener true
            val annus = v.text.toString()
            if (annus == "" || annus == "-") return@setOnEditorActionListener true
            c.luna = "${z(annus, 4)}.${z(b.luna.selectedItemPosition + 1)}"
            c.date = c.lunaToDate(c.luna)
            updateGrid()
            b.annus.blur(c)
            return@setOnEditorActionListener true
        }
        b.annusUp.setOnClickListener { moveInYears(1) }
        b.annusDown.setOnClickListener { moveInYears(-1) }
        b.annusUp.setOnLongClickListener { moveInYears(5); true }
        b.annusDown.setOnLongClickListener { moveInYears(-5); true }
        b.next.setOnClickListener { moveInMonths(true) }
        b.prev.setOnClickListener { moveInMonths(false) }
        b.next.setOnLongClickListener { moveInMonths(true, 6); true }
        b.prev.setOnLongClickListener { moveInMonths(false, 6); true }
        b.defVar.setOnClickListener { variabilis(-1) }
        b.verbumIcon.setColorFilter(color(android.R.attr.textColor))

        // Handler
        MainHandler.handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MainHandler.HANDLE_NEW_DAY -> {
                        c.updateToday()
                        updateGrid()
                    }
                }
            }
        }

        // runtime permission(s)
        val requiredPermissions =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                arrayOf(Manifest.permission.POST_NOTIFICATIONS)
            else
                arrayOf()
        // note: change reqPermLauncher to RequestMultiplePermissions() if you wanna add more.
        for (prm in requiredPermissions) {
            if (checkSelfPermission(prm) != PackageManager.PERMISSION_GRANTED)
                reqPermLauncher.launch(prm)
        }

        // Nyx
        Nyx.cancelNotification(c)
        Nyx.alarm(c)
        //Nyx.test(c)

        // miscellaneous background IO tasks
        CoroutineScope(Dispatchers.IO).launch {

            // load all emojis for input text filtering
            m.emojis = InputStreamReader(resources.openRawResource(R.raw.emojis), Charsets.UTF_8)
                .use { it.readText().split(' ') }

            // Sexbook integration
            if (m.sexbook.value == null &&
                try {
                    packageManager.getPackageInfo(Sexbook.PACKAGE, 0)
                    true
                } catch (_: PackageManager.NameNotFoundException) {
                    false
                }
            ) Sexbook(c).load { data ->
                m.sexbook.value = data
            }

            // scroll to top on older devices
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                delay(500)
                withContext(Dispatchers.Main) {
                    b.scroller.scrollTo(0, 0)
                }
            }
        }

        // resolving intents
        addOnNewIntentListener { resolveIntent(it) }
        resolveIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        dropbox?.onResume()
    }

    private fun resolveIntent(intent: Intent) {
        intent.getStringExtra(MainHandler.EXTRA_LUNA)?.also { extraLuna ->
            c.luna = extraLuna
            c.date = c.chronology.dateNow()
            updatePanel()
            updateGrid()
        }
        if (intent.hasExtra(MainHandler.EXTRA_DIES))
            variabilis(intent.getIntExtra(MainHandler.EXTRA_DIES, 1) - 1)
    }

    override fun onRestart() {
        super.onRestart()
        c.updateToday()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navToday -> {
                if (c.todayLuna != c.date.toKey()) {
                    c.date = c.chronology.dateNow()
                    onDateChanged(); }
                closeDrawer()
            }

            R.id.navSearch -> SearchDialog().show(supportFragmentManager, SearchDialog.TAG)
            R.id.navStat -> StatisticsDialog().show(supportFragmentManager, StatisticsDialog.TAG)
            R.id.navExport ->
                if (c.vita.hasData()
                ) exportLauncher.launch(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = Fortuna.VITA_MIME_TYPE
                    putExtra(Intent.EXTRA_TITLE, c.getString(R.string.export_file))
                })
                else Toast.makeText(c, R.string.emptyVita, Toast.LENGTH_SHORT).show()

            R.id.navImport ->
                importLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = Fortuna.VITA_MIME_TYPE
                })

            R.id.navSend ->
                if (c.vita.hasData())
                    c.vita.export().also { sendFile(it, R.string.export_file) }
                else
                    Toast.makeText(c, R.string.emptyVita, Toast.LENGTH_SHORT).show()

            R.id.navBackup -> BackupDialog().show(supportFragmentManager, BackupDialog.TAG)
            R.id.navHelp -> HelpDialog().show(supportFragmentManager, HelpDialog.TAG)
        }
        return true
    }

    /** Requests all the required permissions. (currently only for notifications in Android 13+) */
    private val reqPermLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    /** Invoked when a file is ready to be exported. */
    private val exportLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != RESULT_OK) return@registerForActivityResult
            val bExp = try {
                c.contentResolver.openFileDescriptor(it.data!!.data!!, "w")?.use { des ->
                    FileOutputStream(des.fileDescriptor).use { fos ->
                        fos.write(c.vita.export())
                    }
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
                imported = Vita(c, text = data)
            } catch (_: Exception) {
                Toast.makeText(
                    c, R.string.importReadError, Toast.LENGTH_LONG
                ).show(); return@registerForActivityResult
            }
            MaterialAlertDialogBuilder(this@Main).apply {
                setIcon(R.drawable.data_import)
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
        CoroutineScope(Dispatchers.IO).launch {
            val exported = File(cacheDir, c.getString(fileName))
            runCatching {
                FileOutputStream(exported).use { it.write(binary) }
            }.onSuccess {
                withContext(Dispatchers.Main) {
                    Intent(Intent.ACTION_SEND).apply {
                        type = Fortuna.VITA_MIME_TYPE
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

    @SuppressLint("SetTextI18n")
    override fun updatePanel() {
        b.annus.setText(c.date[ChronoField.YEAR].toString())
        b.luna.setSelection(c.date[ChronoField.MONTH_OF_YEAR] - 1)
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun updateGrid() {
        if (b.grid.adapter == null)
            b.grid.adapter = Grid(this)
        else {
            (b.grid.adapter as Grid).onRefresh()
            b.grid.invalidateViews()
        }
        val mean: Float
        (b.grid.adapter as Grid).also { grid ->
            b.defVar.text = grid.luna.default.displayScore(true)
            val scores = grid.luna.collectScores(grid.maximumStats ?: 0)
            mean = grid.luna.mean(0, scores)
            b.lunaMean.text = "∑ : " + grid.luna.sum(0, scores) +
                    " - x̄: " + String.format(Locale.UK, "%.2f", mean)
            b.lunaSize.text = AndroidUtils.showBytes(this@Main, grid.luna.size)
            b.lunaSize.isInvisible = grid.luna.size == 0L
            b.verbumIcon.isVisible = grid.luna.verbum?.isNotBlank() == true
            b.emoji.text = grid.luna.emoji
            b.emoji.isVisible = grid.luna.emoji?.isNotBlank() == true
        }
        b.grid.layoutParams.apply {
            height = (resources.getDimension(R.dimen.gridItemHeight) * ceil(
                c.date.lengthOfMonth().toFloat() / resources.getInteger(R.integer.gridColumns)
                    .toFloat()
            )).toInt()
        }

        val bgColor: Int
        val fgColor: Int
        if (mean == 0f) {  // empty or mediocre
            bgColor = Color.TRANSPARENT
            fgColor = color(android.R.attr.textColor)
        } else {
            if (mean > 0f) {  // pleasant
                bgColor = color(com.google.android.material.R.attr.colorPrimary)
                fgColor = color(com.google.android.material.R.attr.colorOnPrimary)
            } else /*mean < 0f*/ {  // painful
                bgColor = color(com.google.android.material.R.attr.colorSecondary)
                fgColor = color(com.google.android.material.R.attr.colorOnSecondary)
            }
        }
        b.toolbar.setBackgroundColor(bgColor)
        b.toolbar.navigationIcon?.colorFilter =  // a tint color will never work!
            PorterDuffColorFilter(fgColor, PorterDuff.Mode.SRC_IN)
        b.toolbar.setTitleTextColor(fgColor)
        b.toolbar.overflowIcon?.setTint(fgColor)

        if (!night) WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = mean == 0f
    }

    /** Updates the overflow menu after the numeral system is changed. */
    private fun updateOverflow() {
        b.toolbar.menu.forEachIndexed { i, item ->
            item.isChecked = c.sp.getString(Fortuna.SP_NUMERAL_TYPE, Fortuna.SP_NUMERAL_TYPE_DEF) ==
                    (Numerals.all[i].name() ?: Fortuna.SP_NUMERAL_TYPE_DEF)
        }
    }

    /**
     * Opens [VariabilisDialog].
     * @param day starting from 0
     */
    fun variabilis(day: Int) {
        VariabilisDialog.newInstance(day).show(supportFragmentManager, VariabilisDialog.TAG)
    }

    override fun onDateChanged() {
        rollingLunaWithAnnus = true
        rollingLuna = true
        super.onDateChanged()
        b.annus.blur(c)
    }

    @SuppressLint("SetTextI18n")
    override fun moveInYears(to: Int) {
        rollingAnnusItself = true
        b.annus.setText((b.annus.text.toString().toInt() + to).toString())
        b.annus.blur(c)
    }

    /** Proper implementation of vibration in across different supported APIs */
    @Suppress("DEPRECATION")
    fun shake(dur: Long = 40L) {
        (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
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
        MainHandler.handler = null
        super.onDestroy()
    }
}
