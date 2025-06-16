package ir.mahdiparastesh.fortuna.sect

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ir.mahdiparastesh.fortuna.Main
import ir.mahdiparastesh.fortuna.R
import ir.mahdiparastesh.fortuna.util.NumberUtils.z

@Composable
fun VariabilisDialog(c: Main) {
    Dialog(
        onDismissRequest = { c.m.variabilis = null },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(MaterialTheme.shapes.large)  // use it before background()
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 15.dp, horizontal = 20.dp)
        ) {
            val i = c.m.variabilis!!

            // title
            Text(
                text = if (i != -1) "${c.c.luna}.${z(i + 1)}"
                else stringResource(R.string.defValue)
            )
        }
    }
}
