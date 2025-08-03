package ir.mahdiparastesh.fortuna.util

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.rotate
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
import androidx.compose.ui.window.PopupProperties
import ir.mahdiparastesh.fortuna.Theme
import ir.mahdiparastesh.fortuna.icon.Arrow
import ir.mahdiparastesh.fortuna.icon.FortunaIcons

@Composable
fun Icon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
) {
    Box(
        modifier
            .toolingGraphicsLayer()
            .paint(
                painter = rememberVectorPainter(imageVector),
                colorFilter = remember(tint) {
                    if (tint == Color.Unspecified) null else ColorFilter.tint(tint)
                },
                contentScale = ContentScale.Fit
            )
            .then(
                if (contentDescription != null) Modifier.semantics {
                    this.contentDescription = contentDescription
                    this.role = Role.Image
                } else Modifier
            ),
    )
}

@Composable
fun Arrow(
    contentDescription: String?,
    rotation: Float,
) {
    Icon(
        imageVector = FortunaIcons.Arrow,
        contentDescription = contentDescription,
        modifier = Modifier
            .padding(5.dp)
            .then(
                if (rotation != 0f) Modifier.rotate(rotation) else Modifier
            ),
        tint = Theme.palette.onWindow
    )
}

@Composable
fun RoundButton(
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    cornerSize: Dp = 0.dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier
            .clip(CutCornerShape(cornerSize))
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
fun OptionsMenu(
    expandedState: MutableState<Boolean>,
    popupVerticalOffset: Int,
    popupWidth: Dp,
    itemRange: IntRange,
    itemContent: @Composable (Int) -> Unit
) {
    if (expandedState.value) Popup(
        alignment = Alignment.TopStart,
        offset = IntOffset(0, popupVerticalOffset),  // position relative to parent
        onDismissRequest = { expandedState.value = false },
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            clippingEnabled = true,
        ),
    ) {
        Column(
            modifier = Modifier
                .width(popupWidth)
                .background(Theme.palette.popup),
        ) {
            for (i in itemRange)
                itemContent(i)
        }
    }
}

@Composable
fun OptionsMenuItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier
            .clickable(onClick = onClick)
            .pointerHoverIcon(PointerIcon.Hand)
    ) {
        content()
    }
}
