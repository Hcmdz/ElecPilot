package com.HcmDz.ElecPilot.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.HcmDz.ElecPilot.R

@Composable
fun SearchMenuPopup(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<String>,
    onPick: (String) -> Unit,
    contentWidth: Dp
) {
    if (!expanded) return
    val density = LocalDensity.current
    Popup(
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = false),
        popupPositionProvider = object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset {
                val margin = with(density) { 8.dp.roundToPx() }
                val x = anchorBounds.left
                val y = minOf(anchorBounds.bottom + margin, windowSize.height - popupContentSize.height - margin)
                    .coerceAtLeast(margin)
                return IntOffset(x, y)
            }
        }
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 3.dp,
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .width(contentWidth)
                    .heightIn(max = 260.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp)
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item, fontSize = 13.sp) },
                        onClick = { onPick(item) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, stringResource(R.string.search_icon_cd), modifier = Modifier.size(16.dp))
                        }
                    )
                }
            }
        }
    }
}
