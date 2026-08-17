package com.HcmDz.ElecPilot.ui.views.plc

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.HcmDz.ElecPilot.data.db.PlcEntity
import com.HcmDz.ElecPilot.ui.components.ScrollToTopButton
import com.HcmDz.ElecPilot.ui.theme.IndustrialBlue
import com.HcmDz.ElecPilot.ui.theme.White
import com.HcmDz.ElecPilot.ui.theme.matchTypeColor
import com.HcmDz.ElecPilot.util.MatchType
import com.HcmDz.ElecPilot.util.SearchIndex
import com.HcmDz.ElecPilot.util.SmartSearchResult
import com.HcmDz.ElecPilot.R

private data class ColumnDef(
    val name: String,
    val shortNameResId: Int,
    val getValue: (PlcEntity) -> String,
    val weight: Float
)

private val COLUMNS = listOf(
    ColumnDef("Item", R.string.col_item, { it.item }, 1.5f),
    ColumnDef("Atelier", R.string.col_atelier, { it.atelier }, 1.2f),
    ColumnDef("DP", R.string.col_dp, { it.dp }, 1f),
    ColumnDef("Carte", R.string.col_carte, { it.carte }, 1f),
    ColumnDef("Position", R.string.col_pos, { it.position }, 0.8f),
    ColumnDef("Désignation", R.string.col_designation, { it.designation }, 1.8f)
)

@Composable
fun PlcTable(
    plcResults: List<SmartSearchResult<PlcEntity>>,
    onPlcClick: (SmartSearchResult<PlcEntity>) -> Unit = {},
    onEditClick: (SmartSearchResult<PlcEntity>) -> Unit = {},
    onDeleteClick: (SmartSearchResult<PlcEntity>) -> Unit = {},
    onImport: () -> Unit = {},
    onActivateSelectMode: () -> Unit = {},
    isSelectMode: Boolean = false,
    selectedIds: Set<Long> = emptySet(),
    onToggleSelect: (Long) -> Unit = {},
    onToggleFavorite: (SmartSearchResult<PlcEntity>) -> Unit = {},
    searchQuery: String = "",
    hasActiveFilters: Boolean = false,
    sortColumn: Int = -1,
    sortAscending: Boolean = true,
    onSortChange: (Int, Boolean) -> Unit = { _, _ -> },
    isLoading: Boolean = false
) {
    val scrollState = rememberScrollState()

    val sortedPlc = remember(plcResults, sortColumn, sortAscending, searchQuery) {
        if (searchQuery.isNotBlank()) {
            plcResults.sortedByDescending { it.score }
        } else if (sortColumn < 0 || sortColumn >= COLUMNS.size) {
            plcResults
        } else {
            val sorted = plcResults.sortedBy { COLUMNS[sortColumn].getValue(it.item).lowercase() }
            if (sortAscending) sorted else sorted.reversed()
        }
    }

    if (sortedPlc.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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

    Column(modifier = Modifier.fillMaxSize()) {
        TableHeader(
            columns = COLUMNS,
            sortColumn = sortColumn,
            sortAscending = sortAscending,
            scrollState = scrollState,
            onSort = { col ->
                val newAsc = if (sortColumn == col) !sortAscending else true
                onSortChange(col, newAsc)
            },
            isSelectMode = isSelectMode
        )

        val listState = rememberLazyListState()

        val orderKey = remember(sortedPlc) { sortedPlc.map { it.item.id to it.item.favorite } }

        LaunchedEffect(searchQuery, hasActiveFilters, if (searchQuery.isBlank() && sortColumn < 0) orderKey else Unit) {
            listState.scrollToItem(0)
        }

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                itemsIndexed(sortedPlc, key = { _, m -> m.item.id }) { index, sr ->
                    val stableOnClick = remember(sr, onPlcClick) { { onPlcClick(sr) } }
                    val stableOnEdit = remember(sr, onEditClick) { { onEditClick(sr) } }
                    val stableOnDelete = remember(sr, onDeleteClick) { { onDeleteClick(sr) } }
                    val stableOnToggleFav = remember(sr, onToggleFavorite) { { onToggleFavorite(sr) } }
                    val plcId = sr.item.id
                    val stableOnToggleSelect = remember(plcId, onToggleSelect) { { onToggleSelect(plcId) } }
                    DataRow(
                        result = sr,
                        columns = COLUMNS,
                        rowIndex = index,
                        scrollState = scrollState,
                        onClick = stableOnClick,
                        onEdit = stableOnEdit,
                        onDelete = stableOnDelete,
                        onActivateSelectMode = onActivateSelectMode,
                        isSelectMode = isSelectMode,
                        isSelected = plcId in selectedIds,
                        onToggleSelect = stableOnToggleSelect,
                        onToggleFavorite = stableOnToggleFav,
                        searchQuery = searchQuery
                    )
                }
            }
            ScrollToTopButton(
                listState = listState,
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            )
        }
    }
}

@Composable
private fun TableHeader(
    columns: List<ColumnDef>,
    sortColumn: Int,
    sortAscending: Boolean,
    scrollState: ScrollState,
    onSort: (Int) -> Unit,
    isSelectMode: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth().background(IndustrialBlue).padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectMode) {
            Spacer(modifier = Modifier.width(40.dp))
        }
        Row(
            modifier = Modifier.weight(1f).horizontalScroll(scrollState),
            verticalAlignment = Alignment.CenterVertically
        ) {
            columns.forEachIndexed { index, col ->
                TextButton(
                    onClick = { onSort(index) },
                    modifier = Modifier.width((col.weight * 100).dp).padding(horizontal = 2.dp)
                ) {
                    Text(
                        text = stringResource(col.shortNameResId),
                        color = White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (sortColumn == index) {
                        Icon(
                            imageVector = if (sortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = if (sortAscending) stringResource(R.string.col_sort_asc) else stringResource(R.string.col_sort_desc),
                            tint = White,
                            modifier = Modifier.padding(start = 1.dp).height(12.dp).width(12.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.width(80.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DataRow(
    result: SmartSearchResult<PlcEntity>,
    columns: List<ColumnDef>,
    rowIndex: Int,
    scrollState: ScrollState,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onActivateSelectMode: () -> Unit = {},
    isSelectMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    searchQuery: String = ""
) {
    val plc = result.item
    val matchColor = matchTypeColor(result.matchType)
    val highlightVariants = remember(searchQuery) {
        val stripped = SearchIndex.stripFormatting(searchQuery)
        val norm = SearchIndex.normalizeForIndex(searchQuery)
        buildList {
            if (searchQuery.isBlank()) return@buildList
            add(searchQuery)
            if (stripped.length >= 2 && !stripped.equals(searchQuery, ignoreCase = true)) add(stripped)
            if (norm.length >= 2 && !norm.equals(stripped, ignoreCase = true) && !norm.equals(searchQuery, ignoreCase = true)) add(norm)
        }.sortedByDescending { it.length }
    }
    val matchLabel = when (result.matchType) {
        MatchType.EXACT -> stringResource(R.string.match_exact)
        MatchType.NORMALIZED -> stringResource(R.string.match_normalized)
        MatchType.STRIPPED -> stringResource(R.string.match_format)
        MatchType.VOICE_NORMALIZED -> stringResource(R.string.match_voice)
        MatchType.SUBSTRING -> stringResource(R.string.match_substring)
        MatchType.FUZZY -> stringResource(R.string.match_approach)
    }
    val bgColor = if (plc.favorite)
        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
    else if (rowIndex % 2 == 0)
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    else
        MaterialTheme.colorScheme.surface

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else bgColor)
                .combinedClickable(
                    onClick = { if (isSelectMode) onToggleSelect() else onClick() },
                    onLongClick = {
                        if (!isSelectMode) {
                            onActivateSelectMode()
                            onToggleSelect()
                        }
                    }
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (searchQuery.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(matchColor)
                )
            }
            if (isSelectMode) {
                Box(modifier = Modifier.width(40.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                        contentDescription = stringResource(R.string.action_select),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Row(
                modifier = Modifier.weight(1f).horizontalScroll(scrollState),
                verticalAlignment = Alignment.CenterVertically
            ) {
                columns.forEach { col ->
                    CellBox(
                        value = col.getValue(plc),
                        weight = col.weight,
                        highlightVariants = highlightVariants
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (searchQuery.isNotBlank()) {
                    Column(
                        modifier = Modifier.width(70.dp).padding(horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (result.matchedOn.isNotBlank()) {
                            Text(
                                text = result.matchedOn,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "${(result.score * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = matchColor
                        )
                        Surface(
                            shape = RoundedCornerShape(3.dp),
                            color = matchColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = matchLabel,
                                fontSize = 11.sp,
                                color = matchColor,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (plc.favorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = stringResource(if (plc.favorite) R.string.detail_favorite_remove_cd else R.string.detail_favorite_add_cd),
                        tint = if (plc.favorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                ActionButtons(onEdit = onEdit, onDelete = onDelete)
            }
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
    }
}

@Composable
private fun CellBox(
    value: String,
    weight: Float,
    highlightVariants: List<String> = emptyList()
) {
    Box(
        modifier = Modifier
            .width((weight * 100).dp)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        val highlightBg = MaterialTheme.colorScheme.tertiaryContainer
        val annotated = remember(value, highlightVariants, highlightBg) {
            val hl = highlightVariants.firstOrNull { value.lowercase().contains(it.lowercase()) }
            if (hl != null) {
                buildAnnotatedString {
                    var start = 0
                    val lower = value.lowercase()
                    val h = hl.lowercase()
                    while (true) {
                        val idx = lower.indexOf(h, start)
                        if (idx < 0) {
                            append(value.substring(start))
                            break
                        }
                        append(value.substring(start, idx))
                        withStyle(SpanStyle(background = highlightBg, fontWeight = FontWeight.Bold)) {
                            append(value.substring(idx, idx + h.length))
                        }
                        start = idx + h.length
                    }
                }
            } else null
        }
        if (annotated != null) {
            Text(
                text = annotated,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
            )
        } else {
            Text(
                text = value,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun ActionButtons(
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.width(56.dp).padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onEdit,
            modifier = Modifier.width(24.dp).height(24.dp)
        ) {
            Icon(Icons.Default.Edit, stringResource(R.string.action_edit),
                tint = IndustrialBlue,
                modifier = Modifier.height(16.dp).width(16.dp))
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier.width(24.dp).height(24.dp)
        ) {
            Icon(Icons.Default.Delete, stringResource(R.string.action_delete),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.height(16.dp).width(16.dp))
        }
    }
}
