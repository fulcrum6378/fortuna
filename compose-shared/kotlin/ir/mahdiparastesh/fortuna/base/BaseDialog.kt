package ir.mahdiparastesh.fortuna.base

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun BaseDialog(
    title: String,
    onDismissRequest: () -> Unit,
    content: @Composable (ColumnScope.() -> Unit)
) {
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(MaterialTheme.shapes.large)  // use it before background()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(vertical = 16.dp, horizontal = 20.dp)
        ) {
            // title
            Text(
                text = title,
                modifier = Modifier
                    .padding(top = 2.dp, bottom = 20.dp),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.displaySmall,
            )

            content()
        }
    }
}
