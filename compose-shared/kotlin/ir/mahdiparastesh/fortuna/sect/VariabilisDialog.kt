package ir.mahdiparastesh.fortuna.sect

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ir.mahdiparastesh.fortuna.*
import ir.mahdiparastesh.fortuna.base.BaseDialog
import ir.mahdiparastesh.fortuna.base.MainComposablePage
import ir.mahdiparastesh.fortuna.util.NumberUtils.z

// AVOID OPTIMISING IMPORTS!
// `import ir.mahdiparastesh.fortuna.*` must always be present.

@Composable
fun VariabilisDialog(c: MainComposablePage) {
    val i = c.m.variabilis!!
    val luna: Luna by lazy { c.c.vita[c.c.luna] }
    val verbum = rememberSaveable {
        mutableStateOf<String?>(if (i != -1) luna.verba[i] else luna.verbum)
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
            textStyle = MaterialTheme.typography.bodyMedium,
            placeholder = {
                Text(c.str(R.string.notesHint))
            },
            maxLines = 10,
            shape = MaterialTheme.shapes.medium,
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceBright,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceBright,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
        )
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
