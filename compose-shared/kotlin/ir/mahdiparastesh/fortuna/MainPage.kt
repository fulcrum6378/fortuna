@file:OptIn(ExperimentalMaterial3Api::class)

package ir.mahdiparastesh.fortuna

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toolingGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import ir.mahdiparastesh.fortuna.icon.ArabicNumerals
import ir.mahdiparastesh.fortuna.icon.Arrow
import ir.mahdiparastesh.fortuna.icon.Backup
import ir.mahdiparastesh.fortuna.icon.Export
import ir.mahdiparastesh.fortuna.icon.FortunaIcons
import ir.mahdiparastesh.fortuna.icon.Help
import ir.mahdiparastesh.fortuna.icon.Import
import ir.mahdiparastesh.fortuna.icon.Menu
import ir.mahdiparastesh.fortuna.icon.Search
import ir.mahdiparastesh.fortuna.icon.Send
import ir.mahdiparastesh.fortuna.icon.Statistics
import ir.mahdiparastesh.fortuna.icon.Today
import ir.mahdiparastesh.fortuna.icon.Verbum
import ir.mahdiparastesh.fortuna.sect.VariabilisDialog
import ir.mahdiparastesh.fortuna.util.NumberUtils
import ir.mahdiparastesh.fortuna.util.NumberUtils.displayScore
import ir.mahdiparastesh.fortuna.util.NumberUtils.toKey
import ir.mahdiparastesh.fortuna.util.NumberUtils.write
import ir.mahdiparastesh.fortuna.util.Numeral
import ir.mahdiparastesh.fortuna.util.Numerals
import kotlinx.coroutines.launch
import java.time.temporal.ChronoField
import java.util.Locale

@Composable
fun MainPage() {
    val c = c
    val numeralState = remember { mutableStateOf(c.numeralType) }

    ModalNavigationDrawer(
        drawerContent = { Drawer() },
        drawerState = c.m.drawerState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            Toolbar(numeralState)
            key(c.m.panelSwitch) { Panel() }
            key(c.m.gridSwitch) { Grid(numeralState) }
        }
    }

    if (c.m.variabilis != null)
        VariabilisDialog(c)
}

@Composable
fun Icon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
) {
    val painter = rememberVectorPainter(imageVector)
    val colorFilter = remember(tint) {
        if (tint == Color.Unspecified) null else ColorFilter.tint(tint)
    }
    val semantics =
        if (contentDescription != null)
            Modifier.semantics {
                this.contentDescription = contentDescription
                this.role = Role.Image
            }
        else
            Modifier
    Box(
        modifier = modifier
            .toolingGraphicsLayer()
            .paint(painter, colorFilter = colorFilter, contentScale = ContentScale.Fit)
            .then(semantics),
    )
}

@Composable
fun RoundButton(
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    width: Dp = 36.dp,
    height: Dp = 36.dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(width, height)
            .clip(RoundedCornerShape(20.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                role = Role.Button,
            )
            .pointerHoverIcon(PointerIcon.Hand),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun Drawer() {
    val c = c
    val coroutineScope = rememberCoroutineScope()

    ModalDrawerSheet(
        modifier = Modifier.width(280.dp),
        drawerShape = MaterialTheme.shapes.large,
        drawerContainerColor = MaterialTheme.colorScheme.primary,
        drawerContentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        val hPad = 15.dp

        @Composable
        fun Item(
            title: String,
            icon: ImageVector,
            onClick: () -> Unit
        ) {
            NavigationDrawerItem(
                label = {
                    BasicText(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = MaterialTheme.colorScheme.onPrimary,
                        ),
                    )
                },
                selected = false,
                onClick = onClick,
                modifier = Modifier
                    .height(54.dp)
                    .padding(hPad, 2.dp)
                    .pointerHoverIcon(PointerIcon.Hand),
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                },
                shape = MaterialTheme.shapes.medium,
            )
        }

        @Composable
        fun Divider() {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = hPad * 1.75f)
                    .align(Alignment.CenterHorizontally),
                color = Color(0x66FFFFFF),
            )
        }

        @Composable
        fun Space() {
            Spacer(Modifier.height(7.dp))
        }

        Space()
        Item(c.str(R.string.today), FortunaIcons.Today) {
            if (c.c.todayLuna != c.c.date.toKey()) {
                c.c.date = c.c.todayDate
                c.onDateChanged()
            }
            coroutineScope.launch {
                c.m.drawerState.close()
            }
        }
        Item(c.str(R.string.navSearch), FortunaIcons.Search) {}
        Item(c.str(R.string.navStat), FortunaIcons.Statistics) {}
        Space()
        Divider()
        Space()
        Item(c.str(R.string.navExport), FortunaIcons.Export) {}
        Item(c.str(R.string.navImport), FortunaIcons.Import) {}
        Item(c.str(R.string.navSend), FortunaIcons.Send) {}
        Item(c.str(R.string.backup), FortunaIcons.Backup) {}
        Space()
        Divider()
        Space()
        Item(c.str(R.string.navHelp), FortunaIcons.Help) {}
    }
}

@Composable
fun Toolbar(numeralState: MutableState<String?>) {
    val c = c
    val coroutineScope = rememberCoroutineScope()
    var numeralsExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(10.dp, 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoundButton(
            onClick = {
                coroutineScope.launch {
                    c.m.drawerState.apply {
                        if (isClosed) open() else close()
                    }
                }
            },
            width = 42.dp,
            height = 42.dp,
        ) {
            Icon(
                imageVector = FortunaIcons.Menu,
                contentDescription = c.str(R.string.navOpen),
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }

        BasicText(
            text = c.str(R.string.app_name),
            modifier = Modifier
                .weight(1f)
                .padding(start = 20.dp),
            style = MaterialTheme.typography.displayLarge.copy(
                color = MaterialTheme.colorScheme.onPrimary,
            ),
        )

        RoundButton(
            onClick = { numeralsExpanded = !numeralsExpanded },
            width = 42.dp,
            height = 42.dp,
        ) {
            Icon(
                imageVector = FortunaIcons.ArabicNumerals,
                contentDescription = c.str(R.string.numerals),
                tint = MaterialTheme.colorScheme.onPrimary,
            )

            if (numeralsExpanded) Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(0, 36),  // position relative to parent
                onDismissRequest = { numeralsExpanded = false }
            ) {
                Column(
                    modifier = Modifier
                        .width(179.dp)
                        .background(MaterialTheme.colorScheme.surface),
                ) {
                    for (n in Numerals.all.indices) {
                        val nt = Numerals.all[n]
                        val ntName: String? = nt.name()

                        Box(
                            modifier = Modifier
                                .clickable {
                                    c.numeralType = ntName
                                    numeralState.value = ntName
                                    numeralsExpanded = false
                                }
                                .pointerHoverIcon(PointerIcon.Hand)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = numeralState.value == ntName,
                                    onCheckedChange = null,
                                    colors = CheckboxColorScheme,
                                )
                                Spacer(Modifier.width(18.dp))
                                BasicText(
                                    text = c.str(nt.name),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Panel() {
    val c = c
    val luna = c.c.vita[c.m.date!!.toKey()]
    val months = c.strArr(R.array.luna)
    var lunaExpanded by rememberSaveable { mutableStateOf(false) }
    val prevNextMargin = 20.dp

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


        // move to a next year (up)
        RoundButton(
            onClick = { c.moveInYears(1) },
            onLongClick = { c.moveInYears(5) },
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 62.dp, y = (-34).dp),
        ) {
            Icon(
                imageVector = FortunaIcons.Arrow,
                contentDescription = c.str(R.string.annusUpDesc),
                modifier = Modifier.rotate(180f),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        // move to a previous year (down)
        RoundButton(
            onClick = { c.moveInYears(-1) },
            onLongClick = { c.moveInYears(-5) },
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 62.dp, y = 36.dp),
        ) {
            Icon(
                imageVector = FortunaIcons.Arrow,
                contentDescription = c.str(R.string.annusDownDesc),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }


        // the vertically centered contents:
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 37.dp, bottom = 33.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {

            // move to a previous month
            RoundButton(
                onClick = { c.moveInMonths(false) },
                onLongClick = { c.moveInMonths(false, 6) },
            ) {
                Icon(
                    imageVector = FortunaIcons.Arrow,
                    contentDescription = c.str(R.string.prevDesc),
                    modifier = Modifier.rotate(90f),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.width(prevNextMargin))

            // luna (month selector)
            ExposedDropdownMenuBox(
                expanded = lunaExpanded,
                onExpandedChange = { lunaExpanded = it },
                modifier = Modifier.width(200.dp),
            ) {
                Row {
                    BasicText(
                        text = months[c.m.date!![ChronoField.MONTH_OF_YEAR] - 1],
                        modifier = Modifier
                            .width(200.dp)
                            .clickable { lunaExpanded = true }
                            .pointerHoverIcon(PointerIcon.Hand),
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                    Icon(
                        imageVector = FortunaIcons.Arrow,
                        contentDescription = null,
                        modifier = Modifier.rotate(if (lunaExpanded) 180f else 0f),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                ExposedDropdownMenu(
                    expanded = lunaExpanded,
                    onDismissRequest = { lunaExpanded = false },
                    shape = MaterialTheme.shapes.medium,
                ) {
                    months.forEachIndexed { i, option ->
                        DropdownMenuItem(
                            text = {
                                BasicText(
                                    text = option,
                                    modifier = Modifier.padding(horizontal = 5.dp),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurface,
                                    ),
                                )
                            },
                            onClick = {
                                c.setDate(ChronoField.MONTH_OF_YEAR, i + 1)
                                lunaExpanded = false
                            },
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                        )
                    }
                }
            }

            // annus (year field)
            BasicTextField(
                value = c.m.date!![ChronoField.YEAR].toString(),
                onValueChange = {
                    c.setDate(ChronoField.YEAR, it.toInt())
                },
                modifier = Modifier.width(85.dp),
                textStyle = MaterialTheme.typography.titleSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                singleLine = true,
            )

            // default monthly score
            Spacer(Modifier.width(prevNextMargin))
            RoundButton(
                onClick = { c.m.variabilis = -1 },
            ) {
                BasicText(
                    text = luna.default.displayScore(true),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                )
            }

            // move to a next month
            Spacer(Modifier.width(prevNextMargin))
            RoundButton(
                onClick = { c.moveInMonths(true) },
                onLongClick = { c.moveInMonths(true, 6) },
            ) {
                Icon(
                    imageVector = FortunaIcons.Arrow,
                    contentDescription = c.str(R.string.nextDesc),
                    modifier = Modifier.rotate(-90f),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }


        // default monthly emoji
        if (luna.emoji != null) BasicText(
            text = luna.emoji ?: "",
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 155.dp, y = (-29).dp),
            style = MaterialTheme.typography.labelSmall,
        )

        // default monthly verbum
        if (luna.verbum?.isNotBlank() == true) Icon(
            imageVector = FortunaIcons.Verbum,
            contentDescription = c.str(R.string.verbumDesc),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 178.dp, y = (-27).dp),
            tint = MaterialTheme.colorScheme.onSurface,
        )


        // luna sum and mean
        val maximumStats = c.c.maximaForStats(c.c.date, c.c.luna)
        val scores = luna.collectScores(maximumStats ?: 0)
        val mean = luna.mean(0, scores)
        BasicText(
            text = "∑ : " + luna.sum(0, scores) +
                    " - x̄: " + String.format(Locale.UK, "%.2f", mean),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = 8.dp),
            style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
            ),
        )

        // luna size in terms of bytes
        BasicText(
            text = NumberUtils.showBytes(c.strArr(R.array.bytes), luna.size),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 8.dp),
            style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
            ),
        )
    }
}

@Composable
fun Grid(numeralState: MutableState<String?>) {
    val c = c
    val luna = c.c.vita[c.m.date!!.toKey()]
    val numeralType = c.buildNumeral(numeralState.value)
    val maximumStats = c.c.maximaForStats(c.c.date, c.c.luna)
    val isWideScreen = c.isWideScreen()

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 48.dp),
        maxItemsInEachRow = if (!isWideScreen) 5 else 10,
    ) {
        for (i in 0 until c.m.date!!.lengthOfMonth())
            Dies(
                i, luna, numeralType, maximumStats,
                c.c.luna == c.c.todayLuna, isWideScreen
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
    val c = c

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

    val emoji = luna.emojis.getOrNull(i)
    val hasVerbum = luna.verba[i]?.isNotBlank() == true

    Box(
        modifier = Modifier
            .fillMaxWidth(fraction = if (!isWide) 0.2f else 0.1f)
            .height(100.dp)
            .background(targetColour)  // TODO animate
            .border(
                BorderStroke(
                    if (isToday) 5.dp else 0.25.dp,
                    Color(
                        if (isToday) {
                            if (!isSystemInDarkTheme()) 0x44000000 else 0x44FFFFFF
                        } else {
                            if (!isSystemInDarkTheme()) 0xFFF0F0F0 else 0xFF252525
                        }
                    ),
                )
            )
            .clickable { c.m.variabilis = i }
            .pointerHoverIcon(PointerIcon.Hand),
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            // day number
            BasicText(
                text = numeral.write(i + 1),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = textColor,
                ),
            )
            // score
            BasicText(
                text = (if (isEstimated) "c. " else "") + score.displayScore(false),
                modifier = Modifier.alpha(if (score != null) 1f else .6f),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = textColor,
                ),
            )
        }

        // emoji
        if (emoji != null) BasicText(
            text = emoji,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(7.dp),
            style = MaterialTheme.typography.labelSmall,
        )
        // verbum
        if (hasVerbum) Icon(
            imageVector = FortunaIcons.Verbum,
            contentDescription = c.str(R.string.verbumDesc),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            tint = textColor,
        )
    }
}
