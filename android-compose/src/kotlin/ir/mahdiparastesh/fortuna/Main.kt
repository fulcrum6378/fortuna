@file:OptIn(ExperimentalMaterial3Api::class)

package ir.mahdiparastesh.fortuna

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import ir.mahdiparastesh.fortuna.util.NumberUtils.write
import ir.mahdiparastesh.fortuna.util.NumberUtils.z
import ir.mahdiparastesh.fortuna.util.Numeral
import ir.mahdiparastesh.fortuna.util.Numerals
import kotlinx.coroutines.launch
import java.time.temporal.ChronoField

class Main : ComponentActivity(), MainPage {
    override val c: Fortuna get() = applicationContext as Fortuna
    val m: Model by viewModels()

    class Model : ViewModel() {
        var annus by mutableStateOf<String?>(null)
        var luna by mutableStateOf<Int?>(null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (m.annus == null) m.annus = c.date[ChronoField.YEAR].toString()
        if (m.luna == null) m.luna = c.date[ChronoField.MONTH_OF_YEAR] - 1

        setContent { FortunaTheme { Root() } }
    }

    override fun updatePanel() {
        m.annus = c.date[ChronoField.YEAR].toString()
        m.luna = c.date[ChronoField.MONTH_OF_YEAR] - 1
    }

    override fun updateGrid() {}

    override fun moveInYears(to: Int) {
        m.annus = (m.annus!!.toInt() + to).toString()
    }

    override fun variabilis(day: Int) {
        TODO()
    }
}

@get:Composable
val c: Main get() = LocalActivity.current as Main

@Composable
fun Root() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerContent = { Drawer() },
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },  // FIXME
                        ) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = stringResource(R.string.navOpen),
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO Handle menu */ }) {
                            Icon(
                                painterResource(R.drawable.arabic_numerals),
                                contentDescription = stringResource(R.string.numerals),
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            },
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                Panel()
                Grid()
            }
        }
    }
}

@Composable
fun Drawer() {
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
fun Toolbar() {

}

@Composable
fun Panel() {
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
                    value = months[c.m.luna!!],
                    onValueChange = { c.m.luna = months.indexOf(it) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .width(200.dp),
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(lunaExpanded)
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
                                c.c.luna = "${z(c.m.annus!!, 4)}.${z(i + 1)}"
                                c.c.date = c.c.lunaToDate(c.c.luna)
                                c.m.luna = i
                                lunaExpanded = false
                            }
                        )
                    }
                }
            }

            // year selector (annus)
            TextField(
                value = c.m.annus!!,
                onValueChange = {
                    c.c.luna = "${z(it, 4)}.${z(c.m.luna!! + 1)}"
                    c.c.date = c.c.lunaToDate(c.c.luna)
                    c.m.annus = it
                },
                modifier = Modifier.width(85.dp),
                textStyle = TextStyle(textAlign = TextAlign.Center),
                singleLine = true,
                colors = textFieldColours,
            )
        }
    }
}

@Composable
fun Grid() {
    val c = c
    val numeral = Numerals.build(
        c.c.sp.getString(Fortuna.SP_NUMERAL_TYPE, Fortuna.SP_NUMERAL_TYPE_DEF)
            .let { if (it == Fortuna.SP_NUMERAL_TYPE_DEF) null else it })

    LazyVerticalGrid(
        columns = GridCells.Fixed(5),  // TODO 10 if ...
        modifier = Modifier.fillMaxSize(),
    ) {
        items(c.c.date.lengthOfMonth()) { i -> Dies(i, numeral) }
    }
}

@Composable
fun Dies(i: Int, numeral: Numeral?) {
    Box(
        modifier = Modifier
            .border(
                BorderStroke(
                    0.5.dp,
                    Color(
                        // TODO if today #44000000 night: #44FFFFFF
                        if (!isSystemInDarkTheme()) 0xFFF0F0F0 else 0xFF252525
                    )
                )
            ),  // TODO textColor
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = numeral.write(i + 1),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
        )
    }
}

/*@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FortunaTheme {
        Greeting("Android")
    }
}*/
