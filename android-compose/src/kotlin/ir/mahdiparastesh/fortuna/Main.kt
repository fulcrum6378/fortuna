@file:OptIn(ExperimentalMaterial3Api::class)

package ir.mahdiparastesh.fortuna

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch

class Main : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { FortunaTheme { Root() } }
    }
}

@Composable
fun Root() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.primary,
                drawerContentColor = MaterialTheme.colorScheme.onPrimary,
            ) {

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
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    title = { Text(stringResource(R.string.app_name)) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
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
                    }
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Panel(modifier = Modifier.padding(innerPadding))
        }
    }
}

@Composable
fun NavItem(
    @StringRes title: Int,
    @DrawableRes icon: Int,
) {
    NavigationDrawerItem(
        icon = {
            Icon(
                painterResource(icon),
                contentDescription = stringResource(title),
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        },
        label = {
            Text(
                text = stringResource(title),
                color = MaterialTheme.colorScheme.onPrimary,
            )
        },
        selected = false,
        onClick = { /*TODO*/ }
    )
}

@Composable
fun Panel(modifier: Modifier) {
    val options = listOf("Option 1", "Option 2", "Option 3")
    var selectedOption = remember { mutableStateOf(options[0]) }
    var expanded = remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded.value,
        onExpandedChange = { expanded.value = !expanded.value }
    ) {
        TextField(
            value = selectedOption.value,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown Icon")
            },
            //modifier = Modifier.menuAnchor(type, enabled)
        )
        ExposedDropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        selectedOption.value = option
                        expanded.value = false
                    }
                )
            }
        }
    }
}

/*@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FortunaTheme {
        Greeting("Android")
    }
}*/
