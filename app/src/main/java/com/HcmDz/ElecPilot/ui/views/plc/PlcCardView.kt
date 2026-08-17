package com.HcmDz.ElecPilot.ui.views.plc

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.HcmDz.ElecPilot.data.db.PlcEntity
import com.HcmDz.ElecPilot.ui.components.ScrollToTopButton
import com.HcmDz.ElecPilot.util.MatchType
import com.HcmDz.ElecPilot.util.SmartSearchResult
import com.HcmDz.ElecPilot.ui.theme.matchTypeColor
import com.HcmDz.ElecPilot.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlcCardView(
    plcResults: List<SmartSearchResult<PlcEntity>>,
    showScore: Boolean = false,
    onPlcClick: (SmartSearchResult<PlcEntity>) -> Unit = {},
    onEditClick: (SmartSearchResult<PlcEntity>) -> Unit = {},
    onDeleteClick: (SmartSearchResult<PlcEntity>) -> Unit = {},
    onToggleFavorite: (SmartSearchResult<PlcEntity>) -> Unit = {},
    isSelectMode: Boolean = false,
    selectedIds: Set<Long> = emptySet(),
    onToggleSelect: (Long) -> Unit = {},
    onActivateSelectMode: () -> Unit = {},
    isLoading: Boolean = false,
    onImport: () -> Unit = {},
    searchQuery: String = "",
    hasActiveFilters: Boolean = false
) {
    if (plcResults.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            } else if (searchQuery.isBlank() && !hasActiveFilters) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.empty_no_data_plc),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onImport,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.FileUpload, stringResource(R.string.action_import), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.empty_import_hint))
                    }
                }
            } else {
                Text(
                    text = when {
                        searchQuery.isNotBlank() -> stringResource(R.string.search_no_results, searchQuery)
                        else -> stringResource(R.string.search_no_results_filters)
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        return
    }

    val listState = rememberLazyListState()

    val orderKey = remember(plcResults) { plcResults.map { it.item.id to it.item.favorite } }

    LaunchedEffect(searchQuery, hasActiveFilters, if (searchQuery.isBlank()) orderKey else Unit) {
        listState.scrollToItem(0)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            state = listState
        ) {
        items(plcResults, key = { it.item.id }) { sr ->
            val plc = sr.item
            val matchColor = matchTypeColor(sr.matchType)
            val isSelected = plc.id in selectedIds
            val stableOnPlcClick = remember(sr, onPlcClick) { { onPlcClick(sr) } }
            val stableOnEditClick = remember(sr, onEditClick) { { onEditClick(sr) } }
            val stableOnDeleteClick = remember(sr, onDeleteClick) { { onDeleteClick(sr) } }
            val stableOnToggleFavorite = remember(sr, onToggleFavorite) { { onToggleFavorite(sr) } }
            val stableOnToggleSelect = remember(plc.id, onToggleSelect) { { onToggleSelect(plc.id) } }
            val stableOnActivateSelectMode = remember(onActivateSelectMode) { onActivateSelectMode }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            if (isSelectMode) stableOnToggleSelect()
                            else stableOnPlcClick()
                        },
                        onLongClick = {
                            if (!isSelectMode) {
                                stableOnActivateSelectMode()
                                stableOnToggleSelect()
                            }
                        }
                    ),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        plc.favorite -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                        else -> MaterialTheme.colorScheme.surface
                    }
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    if (isSelectMode) {
                        Box(
                            modifier = Modifier.width(40.dp).fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                contentDescription = stringResource(R.string.action_select),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = plc.item.ifBlank { "-" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = plc.atelier.ifBlank { "-" },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (plc.dp.isNotBlank()) {
                                Text(" · ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(plc.dp, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = plc.carte.ifBlank { "-" },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (plc.position.isNotBlank()) {
                                Text(" · ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(plc.position, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        if (plc.designation.isNotBlank() && plc.designation != plc.item) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = plc.designation,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (showScore) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${(sr.score * 100).toInt()}%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = matchColor
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = sr.matchType.name,
                                    fontSize = 9.sp,
                                    color = matchColor
                                )
                            }
                        }
                    }
                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = stableOnToggleFavorite,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (plc.favorite) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = stringResource(if (plc.favorite) R.string.detail_favorite_remove_cd else R.string.detail_favorite_add_cd),
                                    tint = if (plc.favorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = stableOnEditClick,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Edit, stringResource(R.string.action_modify), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                            IconButton(
                                onClick = stableOnDeleteClick,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Delete, stringResource(R.string.action_delete), tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
        ScrollToTopButton(
            listState = listState,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }
}
