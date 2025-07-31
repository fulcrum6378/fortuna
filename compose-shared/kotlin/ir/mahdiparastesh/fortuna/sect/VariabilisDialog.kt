package ir.mahdiparastesh.fortuna.sect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.mahdiparastesh.fortuna.*
import ir.mahdiparastesh.fortuna.base.BaseDialog
import ir.mahdiparastesh.fortuna.base.BaseDialogButton
import ir.mahdiparastesh.fortuna.base.MainComposablePage
import ir.mahdiparastesh.fortuna.util.FontFamilyQuattrocento
import ir.mahdiparastesh.fortuna.util.NumberUtils.z

// AVOID OPTIMISING IMPORTS!
// `import ir.mahdiparastesh.fortuna.*` must always be present.

@Composable
fun VariabilisDialog(c: MainComposablePage) {
    val i = c.m.variabilis!!
    val luna: Luna by lazy { c.c.vita[c.c.luna] }
    val verbum = rememberSaveable {
        mutableStateOf(if (i != -1) luna.verba[i] else luna.verbum)
    }

    BaseDialog(
        title =
            if (i != -1) "${c.c.luna}.${z(i + 1)}"
            else c.str(R.string.defValue),
        onDismissRequest = { c.m.variabilis = null },
    ) {

        // verbum
        TextField(
            value = verbum.value ?: "",
            onValueChange = { verbum.value = it },
            modifier = Modifier
                .fillMaxWidth(),
            textStyle = TextStyle(
                fontSize = 17.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamilyQuattrocento,
                lineHeight = 26.sp,
            ),
            placeholder = {
                Text(c.str(R.string.notesHint))
            },
            maxLines = 10,
            shape = CutCornerShape(10.dp),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Theme.palette.onVariabilisField,
                unfocusedTextColor = Theme.palette.onVariabilisField,
                focusedContainerColor = Theme.palette.variabilisField,
                unfocusedContainerColor = Theme.palette.variabilisField,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
        )

        // buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(0.3333333f),
                horizontalArrangement = Arrangement.Start,
            ) {
                BaseDialogButton(c.str(R.string.clear)) {
                    // TODO Toast hold a little longer
                    c.saveDies(luna, i, null, null, null)
                    c.m.variabilis = null
                    // TODO c.c.shake()
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(1f),
                horizontalArrangement = Arrangement.End,
            ) {
                BaseDialogButton(c.str(R.string.cancel)) { c.m.variabilis = null }
                Spacer(Modifier.width(2.dp))
                BaseDialogButton(c.str(R.string.save)) {
                    c.saveDies(
                        luna, i,
                        luna.default ?: 0f,
                        luna.emoji,
                        verbum.value
                    )
                    c.m.variabilis = null
                    // TODO c.c.shake()
                }
            }
        }
    }
}

/*fun NumberPicker(
    range: IntRange,
    selectedValue: Int,
    onValueChange: (Int) -> Unit
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = range.indexOf(selectedValue))
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    LazyColumn(
        state = listState,
        flingBehavior = flingBehavior,
        modifier = Modifier.height(150.dp)
    ) {
        items(range.toList()) { number ->
            Text(
                text = number.toString(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { onValueChange(number) },
                fontSize = 24.sp
            )
        }
    }
}*/
