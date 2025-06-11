@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package ir.mahdiparastesh.fortuna

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import ir.mahdiparastesh.fortuna.base.MainPage
import ir.mahdiparastesh.fortuna.util.NumberUtils.displayScore
import ir.mahdiparastesh.fortuna.util.NumberUtils.toKey
import ir.mahdiparastesh.fortuna.util.NumberUtils.write
import ir.mahdiparastesh.fortuna.util.Numeral
import ir.mahdiparastesh.fortuna.util.Numerals
import kotlinx.coroutines.launch
import java.time.chrono.ChronoLocalDate
import java.time.temporal.ChronoField

class Main : ComponentActivity(), MainPage {
    override val c: Fortuna get() = applicationContext as Fortuna
    val m: Model by viewModels()

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

    class Model : ViewModel() {
        var date by mutableStateOf<ChronoLocalDate?>(null, structuralEqualityPolicy())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tp = android.graphics.Color.TRANSPARENT
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(tp, tp),
            navigationBarStyle =
                if (!night) SystemBarStyle.light(tp, tp) else SystemBarStyle.dark(tp)
        )
        if (m.date == null) m.date = c.date
        setContent { FortunaTheme { Root() } }
    }

    override fun updatePanel() {}
    override fun updateGrid() {}

    fun setDate(field: ChronoField, value: Int) {
        c.date = c.date.with(field, value.toLong())
        onDateChanged()
    }

    override fun moveInYears(to: Int) {
        setDate(ChronoField.YEAR, c.date[ChronoField.YEAR] + to)
        onDateChanged()
    }

    override fun variabilis(day: Int) {
        TODO()
    }

    override fun onDateChanged() {
        c.luna = c.date.toKey()
        m.date = c.date
    }

    /** Proper implementation of vibration in across different supported APIs */
    @Suppress("DEPRECATION")
    fun shake(dur: Long = 40L) {
        (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        else getSystemService(VIBRATOR_SERVICE) as Vibrator)
            .vibrate(VibrationEffect.createOneShot(dur, 100))
    }
}

@get:Composable
val c: Main get() = LocalActivity.current as Main

@Composable
fun Root() {
    //Log.d("YURIKO", "Root()")
    val c = c
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val numeralState = remember {
        mutableStateOf<String?>(
            c.c.sp.getString(Fortuna.SP_NUMERAL_TYPE, Fortuna.SP_NUMERAL_TYPE_DEF)
        )
    }

    ModalNavigationDrawer(
        drawerContent = { Drawer() },
        drawerState = drawerState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            Toolbar(drawerState, numeralState)
            Panel()
            Grid(numeralState)
        }
    }
}

@Composable
fun Drawer() {
    //Log.d("YURIKO", "Drawer()")
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.primary,
        drawerContentColor = MaterialTheme.colorScheme.onPrimary,
    ) {

        @Composable
        fun NavItem(
            @StringRes title: Int,
            @DrawableRes icon: Int,
        ) {
            NavigationDrawerItem(
                label = {
                    Text(
                        text = stringResource(title),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                },
                selected = false,
                onClick = { /*TODO*/ },
                icon = {
                    Icon(
                        painterResource(icon),
                        contentDescription = stringResource(title),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                },
            )
        }

        @Composable
        fun NavDivider() {
            HorizontalDivider(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.8f),
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }

        NavItem(R.string.today, R.drawable.today)
        NavItem(R.string.navSearch, R.drawable.search)
        NavItem(R.string.navStat, R.drawable.statistics)
        NavDivider()
        NavItem(R.string.navExport, R.drawable.data_export)
        NavItem(R.string.navImport, R.drawable.data_import)
        NavItem(R.string.navSend, R.drawable.data_send)
        NavItem(R.string.backup, R.drawable.backup)
        NavDivider()
        NavItem(R.string.navHelp, R.drawable.help)
    }
}

@Composable
fun Toolbar(drawerState: DrawerState, numeralState: MutableState<String?>) {
    val c = c
    val coroutineScope = rememberCoroutineScope()
    var numeralsExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(stringResource(R.string.app_name)) },
        navigationIcon = {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        drawerState.apply {
                            if (isClosed) open() else close()
                        }
                    }
                },
            ) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = stringResource(R.string.navOpen),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
        actions = {
            IconButton(onClick = { numeralsExpanded = !numeralsExpanded }) {
                Icon(
                    painterResource(R.drawable.arabic_numerals),
                    contentDescription = stringResource(R.string.numerals),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
            DropdownMenu(
                expanded = numeralsExpanded,
                onDismissRequest = { numeralsExpanded = false }
            ) {
                for (n in Numerals.all.indices) {
                    val nt = Numerals.all[n]
                    val ntName = nt.name() ?: Fortuna.SP_NUMERAL_TYPE_DEF

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = numeralState.value == ntName,
                                    onCheckedChange = null,
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(nt.name))
                            }
                        },
                        onClick = {
                            c.c.sp.edit { putString(Fortuna.SP_NUMERAL_TYPE, ntName) }
                            c.shake()
                            numeralState.value = ntName
                            numeralsExpanded = false
                        },
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    )
}

@Composable
fun Panel() {
    //Log.d("YURIKO", "Panel()")
    val c = c
    val months = stringArrayResource(R.array.luna)
    var lunaExpanded by rememberSaveable { mutableStateOf(false) }

    Box {
        // a shadow beneath the TopAppBar
        Box(
            Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0x40000000), Color.Transparent),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            val textFieldColours = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            )

            // month selector (luna)
            ExposedDropdownMenuBox(
                expanded = lunaExpanded,
                onExpandedChange = { lunaExpanded = it },
                modifier = Modifier.width(200.dp),
            ) {
                TextField(
                    value = months[c.m.date!![ChronoField.MONTH_OF_YEAR] - 1],
                    onValueChange = { /* unused */ },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .width(200.dp),
                    readOnly = true,
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.rotate(if (lunaExpanded) 180f else 0f),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    colors = textFieldColours,
                )
                ExposedDropdownMenu(
                    expanded = lunaExpanded,
                    onDismissRequest = { lunaExpanded = false },
                ) {
                    months.forEachIndexed { i, option ->
                        DropdownMenuItem(
                            text = {
                                Text(option)
                            },
                            onClick = {
                                c.setDate(ChronoField.MONTH_OF_YEAR, i + 1)
                                c.onDateChanged()
                                lunaExpanded = false
                            }
                        )
                    }
                }
            }

            // year selector (annus)
            TextField(
                value = c.m.date!![ChronoField.YEAR].toString(),
                onValueChange = {
                    c.setDate(ChronoField.YEAR, it.toInt())
                    c.onDateChanged()
                },
                modifier = Modifier.width(85.dp),
                textStyle = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
                singleLine = true,
                colors = textFieldColours,
            )
        }
    }
}

@Composable
fun Grid(numeralState: MutableState<String?>) {
    //Log.d("YURIKO", "Grid()")
    val c = c
    val luna = c.c.vita[c.m.date!!.toKey()]
    val numeralType = Numerals.build(numeralState.value)
    val maximumStats = c.c.maximaForStats(c.c.date, c.c.luna)
    val config = LocalConfiguration.current
    val isWide = config.smallestScreenWidthDp >= 600

    FlowRow(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        maxItemsInEachRow = if (!isWide) 5 else 10,
    ) {
        for (i in 0 until c.m.date!!.lengthOfMonth())
            Dies(
                i, luna, numeralType, maximumStats, c.c.luna == c.c.todayLuna, isWide
            )
    }
}

@Composable
fun Dies(
    i: Int,
    luna: Luna,
    numeral: Numeral?,
    maximumStats: Int?,
    hasToday: Boolean,
    isWide: Boolean,
) {
    //Log.d("YURIKO", "Dies(${i + 1})")
    val score: Float? =
        if (i < (maximumStats ?: 0)) luna[i] ?: luna.default else null
    val isEstimated = i < (maximumStats ?: 0) && luna[i] == null && luna.default != null
    val isToday = hasToday && c.c.todayDate[ChronoField.DAY_OF_MONTH] == i + 1

    val textColor: Color
    val targetColour = when {
        score != null && score > 0f -> {
            textColor = MaterialTheme.colorScheme.onPrimary
            Color(c.cp[0], c.cp[1], c.cp[2], score / Vita.MAX_RANGE)
        }

        score != null && score < 0f -> {
            textColor = MaterialTheme.colorScheme.onSecondary
            Color(c.cs[0], c.cs[1], c.cs[2], -score / Vita.MAX_RANGE)
        }

        else -> {
            textColor = MaterialTheme.colorScheme.onSurface
            Color.Transparent
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth(fraction = if (!isWide) 0.2f else 0.1f)
            .background(targetColour)  // TODO animate
            .border(
                BorderStroke(
                    if (isToday) 5.dp else 0.5.dp,
                    Color(
                        if (isToday) {
                            if (!isSystemInDarkTheme()) 0x44000000 else 0x44FFFFFF
                        } else {
                            if (!isSystemInDarkTheme()) 0xFFF0F0F0 else 0xFF252525
                        }
                    ),
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(vertical = 23.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = numeral.write(i + 1),
                color = textColor,
                fontSize = 18.sp,
            )
            Text(
                text = (if (isEstimated) "c. " else "") + score.displayScore(false),
                modifier = Modifier.alpha(if (score != null) 1f else .6f),
                color = textColor,
                fontSize = 13.sp,
            )
        }
    }
}
