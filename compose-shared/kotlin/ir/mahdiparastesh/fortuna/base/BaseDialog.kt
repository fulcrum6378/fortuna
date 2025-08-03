package ir.mahdiparastesh.fortuna.base

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ir.mahdiparastesh.fortuna.Theme
import ir.mahdiparastesh.fortuna.util.FontFamilyQuattrocento

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
                .clip(CutCornerShape(14.dp))  // use it before background()
                .background(Theme.palette.dialog)
                .padding(20.dp, 18.dp, 20.dp, 14.dp)
        ) {
            // title
            BasicText(
                text = title,
                modifier = Modifier
                    .padding(top = 2.dp, bottom = 20.dp),
                style = TextStyle(
                    color = Theme.palette.onWindow,
                    fontSize = Theme.geometry.dialogTitle,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamilyQuattrocento,
                ),
            )

            content()
        }
    }
}

@Composable
fun BaseDialogButton(
    text: String,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(CutCornerShape(10.dp))
            .then(
                if (onLongClick == null)
                    Modifier.clickable(onClick = onClick, role = Role.Button)
                else
                    Modifier.combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick,
                        role = Role.Button,
                    )
            )
            .pointerHoverIcon(PointerIcon.Hand),
        contentAlignment = Alignment.Center
    ) {
        BasicText(
            text = text,
            modifier = Modifier.padding(10.dp, 9.dp),
            style = TextStyle(
                color = Theme.palette.onWindow,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamilyQuattrocento,
            ),
        )
    }
}
