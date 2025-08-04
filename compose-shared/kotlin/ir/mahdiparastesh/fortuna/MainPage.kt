package ir.mahdiparastesh.fortuna

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toolingGraphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.mahdiparastesh.fortuna.icon.ArabicNumerals
import ir.mahdiparastesh.fortuna.icon.FortunaIcons
import ir.mahdiparastesh.fortuna.icon.Menu
import ir.mahdiparastesh.fortuna.icon.Send
import ir.mahdiparastesh.fortuna.icon.Verbum
import ir.mahdiparastesh.fortuna.sect.VariabilisDialog
import ir.mahdiparastesh.fortuna.util.Arrow
import ir.mahdiparastesh.fortuna.util.FontFamilyMorrisRoman
import ir.mahdiparastesh.fortuna.util.FontFamilyQuattrocento
import ir.mahdiparastesh.fortuna.util.Icon
import ir.mahdiparastesh.fortuna.util.NumberUtils
import ir.mahdiparastesh.fortuna.util.NumberUtils.displayScore
import ir.mahdiparastesh.fortuna.util.NumberUtils.toKey
import ir.mahdiparastesh.fortuna.util.NumberUtils.write
import ir.mahdiparastesh.fortuna.util.Numeral
import ir.mahdiparastesh.fortuna.util.Numerals
import ir.mahdiparastesh.fortuna.util.OptionsMenu
import ir.mahdiparastesh.fortuna.util.OptionsMenuItem
import ir.mahdiparastesh.fortuna.util.SmallButton
import kotlinx.coroutines.launch
import java.time.temporal.ChronoField
import java.util.Locale

@Composable
fun MainPage() {
    val c = c
    val numeralState = remember { mutableStateOf(c.numeralType) }

    /*ModalNavigationDrawer(
        drawerContent = { Drawer() },
        drawerState = c.m.drawerState,
    ) {*/
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        Toolbar(numeralState)
        key(c.m.panelSwitch) { Panel() }
        key(c.m.gridSwitch) { Grid(numeralState) }
    }
    //}

    if (c.m.variabilis != null)
        VariabilisDialog(c)
}

/*@Composable
fun Drawer() {
    val c = c
    val coroutineScope = rememberCoroutineScope()

    ModalDrawerSheet(
        modifier = Modifier.width(280.dp),
        drawerShape = CutCornerShape(14.dp),
        drawerContainerColor = Theme.palette.themePleasure,
        drawerContentColor = Theme.palette.onTheme,
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
                        style = TextStyle(
                            color = Theme.palette.onTheme,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamilyQuattrocento,
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
                        tint = Theme.palette.onTheme,
                    )
                },
                shape = CutCornerShape(10.dp),
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
}*/

@Composable
fun Toolbar(numeralState: MutableState<String?>) {
    val c = c
    val coroutineScope = rememberCoroutineScope()
    val numeralsExpanded = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Theme.geometry.toolbarHeight)
            .background(Theme.palette.themePleasure),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        @Composable
        fun ActionButton(
            onClick: () -> Unit,
            content: @Composable () -> Unit
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .width(Theme.geometry.toolbarHeight)
                    .combinedClickable(
                        onClick = onClick,
                        //onLongClick = onLongClick, TODO tooltip
                        role = Role.Button,
                    )
                    .pointerHoverIcon(PointerIcon.Hand),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }

        ActionButton(
            onClick = {
                coroutineScope.launch {
                    c.m.drawerState = !c.m.drawerState
                }
            },
        ) {
            Icon(
                imageVector = FortunaIcons.Menu,
                contentDescription = c.str(R.string.navOpen),
                tint = Theme.palette.onTheme,
            )
        }

        BasicText(
            text = c.str(R.string.app_name),
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            style = TextStyle(
                color = Theme.palette.onTheme,
                fontSize = Theme.geometry.appTitle,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamilyMorrisRoman,
            ),
        )

        ActionButton(
            onClick = { numeralsExpanded.value = true },
        ) {
            Icon(
                imageVector = FortunaIcons.ArabicNumerals,
                contentDescription = c.str(R.string.numerals),
                tint = Theme.palette.onTheme,
            )

            OptionsMenu(
                expandedState = numeralsExpanded,
                popupVerticalOffset = 43,
                popupWidth = 179.dp,
                itemRange = Numerals.all.indices,
            ) { n ->
                val nt = Numerals.all[n]
                val ntName: String? = nt.name()

                OptionsMenuItem(
                    onClick = {
                        c.numeralType = ntName
                        numeralState.value = ntName
                        numeralsExpanded.value = false
                    },
                ) {
                    if (numeralState.value == ntName) {
                        Box(
                            Modifier
                                .toolingGraphicsLayer()
                                .size(15.dp)
                                .paint(
                                    rememberVectorPainter(FortunaIcons.Send),
                                    colorFilter = ColorFilter.tint(
                                        Theme.palette.onWindow
                                    ),
                                    contentScale = ContentScale.Fit
                                )
                        )
                        Spacer(Modifier.width(18.dp))
                    } else
                        Spacer(Modifier.width(33.dp))
                    BasicText(
                        text = c.str(nt.name),
                        style = TextStyle(
                            color = Theme.palette.onWindow,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamilyQuattrocento,
                        ),
                    )
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
    val lunaExpanded = rememberSaveable { mutableStateOf(false) }
    val prevNextMargin = 20.dp
    val arrowCornerSize = 7.dp  // 17.dp

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
        SmallButton(
            onClick = { c.moveInYears(1) },
            onLongClick = { c.moveInYears(5) },
            cornerSize = arrowCornerSize,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 62.dp, y = (-34).dp),
        ) {
            Arrow(c.str(R.string.annusUpDesc), 180f)
        }

        // move to a previous year (down)
        SmallButton(
            onClick = { c.moveInYears(-1) },
            onLongClick = { c.moveInYears(-5) },
            cornerSize = arrowCornerSize,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 62.dp, y = 36.dp),
        ) {
            Arrow(c.str(R.string.annusDownDesc), 0f)
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
            SmallButton(
                onClick = { c.moveInMonths(false) },
                onLongClick = { c.moveInMonths(false, 6) },
                cornerSize = arrowCornerSize,
            ) {
                Arrow(c.str(R.string.prevDesc), 90f)
            }
            Spacer(Modifier.width(prevNextMargin))

            // luna (month selector)
            Row(
                modifier = Modifier
                    .clip(CutCornerShape(7.dp))
                    .clickable { lunaExpanded.value = true }
                    .pointerHoverIcon(PointerIcon.Hand),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    text = months[c.m.date!![ChronoField.MONTH_OF_YEAR] - 1],
                    modifier = Modifier
                        .width(150.dp)
                        .padding(start = 10.dp, top = 3.dp, bottom = 3.dp),
                    style = TextStyle(
                        color = Theme.palette.onWindow,
                        fontSize = Theme.geometry.thisMonthName,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamilyQuattrocento,
                    ),
                )
                Arrow(null, if (lunaExpanded.value) 180f else 0f)

                OptionsMenu(
                    expandedState = lunaExpanded,
                    popupVerticalOffset = 50,
                    popupWidth = 200.dp,
                    itemRange = months.indices,
                ) { i ->
                    val option = months[i]

                    OptionsMenuItem(
                        onClick = {
                            c.setDate(ChronoField.MONTH_OF_YEAR, i + 1)
                            lunaExpanded.value = false
                        },
                    ) {
                        BasicText(
                            text = option,
                            modifier = Modifier.padding(horizontal = 5.dp),
                            style = TextStyle(
                                color = Theme.palette.onWindow,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = FontFamilyQuattrocento,
                            ),
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
                textStyle = TextStyle(
                    color = Theme.palette.onWindow,
                    fontSize = Theme.geometry.thisYearNumber,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamilyQuattrocento,
                    textAlign = TextAlign.Center,
                ),
                singleLine = true,
            )

            // default monthly score
            Spacer(Modifier.width(prevNextMargin))
            SmallButton(
                onClick = { c.m.variabilis = -1 },
                cornerSize = 7.dp,
            ) {
                BasicText(
                    text = luna.default.displayScore(true),
                    modifier = Modifier.padding(7.dp),
                    style = TextStyle(
                        color = Theme.palette.onWindow,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamilyQuattrocento,
                    ),
                )
            }

            // move to a next month
            Spacer(Modifier.width(prevNextMargin))
            SmallButton(
                onClick = { c.moveInMonths(true) },
                onLongClick = { c.moveInMonths(true, 6) },
                cornerSize = arrowCornerSize,
            ) {
                Arrow(c.str(R.string.nextDesc), -90f)
            }
        }


        // default monthly emoji
        if (luna.emoji != null) BasicText(
            text = luna.emoji ?: "",
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 155.dp, y = (-29).dp),
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamilyQuattrocento,
            ),
        )

        // default monthly verbum
        if (luna.verbum?.isNotBlank() == true) Icon(
            imageVector = FortunaIcons.Verbum,
            contentDescription = c.str(R.string.verbumDesc),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 178.dp, y = (-27).dp),
            tint = Theme.palette.onWindow,
        )


        val panelBottomTextStyle = TextStyle(
            color = Theme.palette.onWindow,
            fontSize = Theme.geometry.panelBottomTexts,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamilyQuattrocento,
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
            style = panelBottomTextStyle,
        )

        // luna size in terms of bytes
        BasicText(
            text = NumberUtils.showBytes(c.strArr(R.array.bytes), luna.size),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 8.dp),
            style = panelBottomTextStyle,
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
    val targetColour by animateColorAsState(
        targetValue = when {
            score != null && score > 0f -> {
                textColor = Theme.palette.onTheme
                Color(c.cp[0], c.cp[1], c.cp[2], score / Vita.MAX_RANGE)
            }

            score != null && score < 0f -> {
                textColor = Theme.palette.onTheme
                Color(c.cs[0], c.cs[1], c.cs[2], -score / Vita.MAX_RANGE)
            }

            else -> {
                textColor = Theme.palette.onWindow
                Color.Transparent
            }
        },
        animationSpec = tween(durationMillis = 275)
    )

    val emoji = luna.emojis.getOrNull(i)
    val hasVerbum = luna.verba[i]?.isNotBlank() == true

    Box(
        modifier = Modifier
            .fillMaxWidth(fraction = if (!isWide) 0.2f else 0.1f)
            .height(100.dp)
            .background(targetColour)
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
                style = TextStyle(
                    color = textColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamilyQuattrocento,
                ),
            )
            // score
            BasicText(
                text = (if (isEstimated) "c. " else "") + score.displayScore(false),
                modifier = Modifier.alpha(if (score != null) 1f else .6f),
                style = TextStyle(
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamilyQuattrocento,
                ),
            )
        }

        // emoji
        if (emoji != null) BasicText(
            text = emoji,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(7.dp),
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamilyQuattrocento,
            ),
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
