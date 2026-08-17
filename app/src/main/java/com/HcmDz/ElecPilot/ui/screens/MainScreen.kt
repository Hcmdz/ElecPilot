package com.HcmDz.ElecPilot.ui.screens

import android.content.Intent
import android.speech.RecognizerIntent
import androidx.compose.animation.Crossfade
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.HcmDz.ElecPilot.data.db.MotorEntity
import com.HcmDz.ElecPilot.ui.components.SearchMenuPopup
import com.HcmDz.ElecPilot.ui.viewmodel.MotorViewModel
import com.HcmDz.ElecPilot.util.SmartSearchResult
import com.HcmDz.ElecPilot.util.VoiceSearchResult
import com.HcmDz.ElecPilot.util.MatchType
import com.HcmDz.ElecPilot.ui.theme.matchTypeColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.HcmDz.ElecPilot.R

private data class FilterColumn(val key: String, val labelResId: Int)

private val FILTER_COLUMNS = listOf(
    FilterColumn("Atelier", R.string.filter_column_atelier),
    FilterColumn("TGBT", R.string.col_tgbt),
    FilterColumn("Types", R.string.filter_column_types),
    FilterColumn("Types_Départs", R.string.filter_column_t_departs)
)

private fun getColumnValue(motor: MotorEntity, key: String): String = when (key) {
    "Atelier" -> motor.atelier.trim()
    "TGBT" -> motor.tgbt.trim()
    "Types" -> motor.types.trim()
    "Types_Départs" -> motor.typesDeparts.trim()
    else -> ""
}

private fun columnLabelResId(key: String): Int = when (key) {
    "Atelier" -> R.string.filter_column_atelier
    "TGBT" -> R.string.col_tgbt
    "Types" -> R.string.filter_column_types
    "Types_Départs" -> R.string.filter_column_t_departs
    else -> R.string.filter_column_atelier
}

private fun applyFilters(results: List<SmartSearchResult<MotorEntity>>, filters: Map<String, Set<String>>, powerFilter: String?): List<SmartSearchResult<MotorEntity>> {
    if (filters.values.all { it.isEmpty() } && powerFilter == null) return results
    return results.filter { sr ->
        val motor = sr.item
        val matchesFilters = filters.all { (column, selected) ->
            selected.isEmpty() || getColumnValue(motor, column) in selected
        }
        val matchesPower = if (powerFilter == null) true else {
            val kw = motor.puissanceKW.toFloatOrNull()
            if (kw == null) false
            else when (powerFilter) {
                "≤10" -> kw <= 10f
                "10-50" -> kw > 10f && kw <= 50f
                ">50" -> kw > 50f
                else -> true
            }
        }
        matchesFilters && matchesPower
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MotorViewModel,
    themeMode: String,
    onToggleDarkMode: () -> Unit,
    onMenuClick: () -> Unit,
    onAdd: () -> Unit,
    onImport: () -> Unit,
    onExportCsv: () -> Unit,
    onExportExcel: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchedMotors by viewModel.motors.collectAsState()
    val selectedMotor by viewModel.selectedMotor.collectAsState()
    val hasLoadedOnce by viewModel.hasLoadedOnce.collectAsState()
    var editingMotor by remember { mutableStateOf<MotorEntity?>(null) }
    var motorToDelete by remember { mutableStateOf<MotorEntity?>(null) }

    val columnFilters = remember {
        mutableStateMapOf<String, Set<String>>().apply {
            FILTER_COLUMNS.forEach { put(it.key, emptySet()) }
        }
    }
    var powerFilter by remember { mutableStateOf<String?>(null) }
    var isSelectMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(emptySet<Long>()) }
    var showBatchEdit by remember { mutableStateOf(false) }
    var showBatchDelete by remember { mutableStateOf(false) }
    var showFavoritesOnly by remember { mutableStateOf(false) }
    var showExportChoiceDialog by remember { mutableStateOf(false) }

    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val sortPrefs = remember(context) { context.getSharedPreferences("sort", android.content.Context.MODE_PRIVATE) }
    var isCardView by remember { mutableStateOf(sortPrefs.getBoolean("card_view", false)) }

    val isSearchActive = searchQuery.isNotBlank()
    val hasActiveFilters = columnFilters.values.any { it.isNotEmpty() } || powerFilter != null || showFavoritesOnly

    val motors by remember {
        derivedStateOf {
            val filtered = applyFilters(searchedMotors, columnFilters, powerFilter)
            if (showFavoritesOnly) filtered.filter { it.item.favorite } else filtered
        }
    }

    var sortColumn by remember { mutableStateOf(sortPrefs.getInt("column", -1)) }
    var sortAscending by remember { mutableStateOf(sortPrefs.getBoolean("ascending", true)) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
        }
    }

    var isRefreshing by remember { mutableStateOf(false) }

    val voiceSearchLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        if (!matches.isNullOrEmpty()) {
            viewModel.processVoiceSearch(matches[0])
        }
    }

    val voiceSearchResult by viewModel.voiceSearchResult.collectAsState()
    val searchSuggestions by viewModel.suggestions.collectAsState()

    val importPreviewMotors by viewModel.pendingImportMotors.collectAsState()

    BackHandler(enabled = selectedMotor != null) {
        viewModel.selectMotor(null)
    }

    if (selectedMotor != null) {
        val motor = selectedMotor!!
        val copiedMessage = stringResource(R.string.snackbar_value_copied)
        val shareTitle = stringResource(R.string.detail_share_title_motor)
        val shareMotor = stringResource(R.string.detail_field_designation)
        val shareAtelier = stringResource(R.string.detail_field_atelier)
        val shareTGBT = stringResource(R.string.detail_field_tgbt)
        val sharePositionTGBT = stringResource(R.string.detail_field_position_tgbt)
        val sharePower = stringResource(R.string.detail_field_power)
        val shareType = stringResource(R.string.field_types)
        val shareTypeDeparts = stringResource(R.string.field_types_departs)
        val shareCable = stringResource(R.string.detail_field_cable)
        MotorDetailScreen(
                motor = motor,
                onBack = { viewModel.selectMotor(null) },
                onCopyValue = { value ->
                    clipboardManager.setText(AnnotatedString(value))
                    scope.launch { snackbarHostState.showSnackbar(copiedMessage, duration = SnackbarDuration.Short) }
                },
                onShare = {
                    val text = """
                        $shareMotor: ${motor.designation}
                        $shareAtelier: ${motor.atelier}
                        $shareTGBT: ${motor.tgbt}
                        $sharePositionTGBT: ${motor.positionTGBT}
                        $sharePower: ${motor.puissanceKW} kW
                        $shareType: ${motor.types}
                        $shareTypeDeparts: ${motor.typesDeparts}
                        $shareCable: ${motor.cable} (${motor.typeCable})
                    """.trimIndent()
                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, text)
                    }
                    context.startActivity(android.content.Intent.createChooser(intent, shareTitle))
                },
                onDuplicate = {
                    editingMotor = motor.copy(id = 0)
                },
                onToggleFavorite = { viewModel.toggleFavorite(motor) }
            )
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
            Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                Header(
                    motorsCount = motors.size,
                    themeMode = themeMode,
                    onToggleDarkMode = onToggleDarkMode,
                    onMenuClick = onMenuClick,
                    isCardView = isCardView,
                    onToggleView = {
                        isCardView = !isCardView
                        sortPrefs.edit().putBoolean("card_view", isCardView).apply()
                    }
                )
                val voiceSearchPrompt = stringResource(R.string.search_voice_prompt)
                TopBar(
                    searchQuery = searchQuery,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onImport = onImport,
                    onExport = { showExportChoiceDialog = true },
                    onAdd = onAdd,
                    onVoiceSearch = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR")
                            putExtra(RecognizerIntent.EXTRA_PROMPT, voiceSearchPrompt)
                        }
                        try { voiceSearchLauncher.launch(intent) }
                        catch (_: Exception) { }
                    },
                    onToggleSelectMode = {
                        isSelectMode = !isSelectMode
                        if (!isSelectMode) selectedIds = emptySet()
                    },
                    isSelectMode = isSelectMode,
                    searchHistory = viewModel.searchHistory.collectAsState().value,
                    onHistoryItemClick = { viewModel.setSearchQuery(it) },
                    suggestions = searchSuggestions.map { sr -> sr.item.item.ifBlank { sr.item.designation } },
                    onSuggestionClick = {
                        viewModel.setSearchQuery(it)
                        viewModel.clearSuggestions()
                    }
                )
                voiceSearchResult?.let { vResult ->
                    VoiceSearchSuggestion(
                        result = vResult,
                        onSelectTag = { tag ->
                            viewModel.setSearchQuery(tag)
                            viewModel.clearVoiceSearchResult()
                        },
                        onDismiss = { viewModel.clearVoiceSearchResult() }
                    )
                }
                if (!isSelectMode) {
                    FilterBar(
                        columnFilters = columnFilters,
                        searchedMotors = searchedMotors,
                        powerFilter = powerFilter,
                        onPowerFilterChange = { powerFilter = it },
                        showFavoritesOnly = showFavoritesOnly,
                        onShowFavoritesChange = { showFavoritesOnly = it }
                    )
                }
                if (isSelectMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.selection_count, selectedIds.size, motors.size), fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (selectedIds.size < motors.size) {
                                ActionChip(
                                    modifier = Modifier.width(110.dp),
                                    icon = Icons.Default.CheckBox,
                                    label = stringResource(R.string.action_select_all),
                                    onClick = { selectedIds = motors.map { it.item.id }.toSet() }
                                )
                            }
                            if (selectedIds.isNotEmpty()) {
                                ActionChip(
                                    modifier = Modifier.width(110.dp),
                                    icon = Icons.Default.Edit,
                                    label = stringResource(R.string.action_modify),
                                    onClick = { showBatchEdit = true }
                                )
                                ActionChip(
                                    modifier = Modifier.width(110.dp),
                                    icon = Icons.Default.Delete,
                                    label = stringResource(R.string.action_delete),
                                    onClick = { showBatchDelete = true }
                                )
                                IconButton(
                                    onClick = { selectedIds = emptySet() },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = stringResource(R.string.action_deselect),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        scope.launch {
                            isRefreshing = true
                            val q = viewModel.searchQuery.value
                            viewModel.setSearchQuery("")
                            viewModel.setSearchQuery(q)
                            delay(400)
                            isRefreshing = false
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isCardView) {
                        MotorCardView(
                            motors = motors,
                            showScore = isSearchActive,
                            onMotorClick = { viewModel.selectMotor(it.item) },
                            onEditClick = { editingMotor = it.item },
                            onDeleteClick = { motorToDelete = it.item },
                            onToggleFavorite = { viewModel.toggleFavorite(it.item) },
                            onActivateSelectMode = {
                                isSelectMode = true
                            },
                            isSelectMode = isSelectMode,
                            selectedIds = selectedIds,
                            onToggleSelect = { id ->
                                selectedIds = if (id in selectedIds) selectedIds - id
                                else selectedIds + id
                            },
                            isLoading = !hasLoadedOnce,
                            onImport = onImport,
                            searchQuery = searchQuery,
                            hasActiveFilters = hasActiveFilters
                        )
                    } else {
                        MotorTable(
                            motors = motors,
                            onMotorClick = { viewModel.selectMotor(it.item) },
                            onEditClick = { editingMotor = it.item },
                            onDeleteClick = { motorToDelete = it.item },
                            onImport = onImport,
                            onActivateSelectMode = {
                                isSelectMode = true
                            },
                            isSelectMode = isSelectMode,
                            selectedIds = selectedIds,
                            onToggleSelect = { id ->
                                selectedIds = if (id in selectedIds) selectedIds - id
                                else selectedIds + id
                            },
                            onToggleFavorite = { viewModel.toggleFavorite(it.item) },
                            searchQuery = searchQuery,
                            hasActiveFilters = hasActiveFilters,
                            sortColumn = sortColumn,
                            sortAscending = sortAscending,
                            onSortChange = { col, asc ->
                                sortColumn = col
                                sortAscending = asc
                                sortPrefs.edit().putInt("column", col).putBoolean("ascending", asc).apply()
                            },
                            isLoading = !hasLoadedOnce
                        )
                    }
                }

            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
    editingMotor?.let { motor ->
        val departDuplicated = stringResource(R.string.snackbar_depart_duplicated)
        val departUpdated = stringResource(R.string.snackbar_depart_updated)
        MotorFormDialog(
            initialMotor = motor,
            onDismiss = { editingMotor = null },
            onConfirm = { updated ->
                if (updated.id == 0L) {
                    viewModel.addMotor(updated)
                    scope.launch { snackbarHostState.showSnackbar(departDuplicated, duration = SnackbarDuration.Short) }
                } else {
                    viewModel.updateMotor(updated)
                    scope.launch { snackbarHostState.showSnackbar(departUpdated, duration = SnackbarDuration.Short) }
                }
                editingMotor = null
            }
        )
    }

    motorToDelete?.let { motor ->
        AlertDialog(
            onDismissRequest = { motorToDelete = null },
            title = { Text(stringResource(R.string.dialog_confirm_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.dialog_delete_confirm_depart, motor.designation.ifBlank { motor.atelier })) },
            confirmButton = {
                val deleteMsg = stringResource(R.string.snackbar_depart_deleted)
                val undoLabel = stringResource(R.string.snackbar_undo)
                TextButton(onClick = {
                    viewModel.deleteMotor(motor)
                    motorToDelete = null
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = deleteMsg,
                            actionLabel = undoLabel,
                            duration = SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.addMotor(motor)
                        }
                    }
                }) { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { motorToDelete = null }) { Text(stringResource(R.string.close)) }
            }
        )
    }

    if (showBatchEdit) {
        val batchEditMotors = remember(selectedIds, motors) {
            motors.filter { it.item.id in selectedIds }.map { it.item }
        }
        val batchEditCount = selectedIds.size
        val batchEditSuccessMsg = stringResource(R.string.snackbar_batch_edit_success, batchEditCount)
        val batchEditUndoLabel = stringResource(R.string.snackbar_undo)
        val fAtelier = stringResource(R.string.field_atelier)
        val fTgbt = stringResource(R.string.field_tgbt)
        val fPosTgbt = stringResource(R.string.field_position_tgbt)
        val fItem = stringResource(R.string.field_item)
        val fDesignation = stringResource(R.string.field_designation)
        val fPowerKw = stringResource(R.string.field_power_kw)
        val fTypes = stringResource(R.string.field_types)
        val fTypesDeparts = stringResource(R.string.field_types_departs)
        val fCable = stringResource(R.string.field_cable)
        val fCableType = stringResource(R.string.field_cable_type)
        BatchEditDialog(
            selectedCount = batchEditCount,
            onDismiss = { showBatchEdit = false },
            onConfirm = { field, value ->
                val updateField: (MotorEntity) -> MotorEntity = when (field) {
                    fAtelier -> { m -> m.copy(atelier = value) }
                    fTgbt -> { m -> m.copy(tgbt = value) }
                    fPosTgbt -> { m -> m.copy(positionTGBT = value) }
                    fItem -> { m -> m.copy(item = value) }
                    fDesignation -> { m -> m.copy(designation = value) }
                    fPowerKw -> { m -> m.copy(puissanceKW = value) }
                    fTypes -> { m -> m.copy(types = value) }
                    fTypesDeparts -> { m -> m.copy(typesDeparts = value) }
                    fCable -> { m -> m.copy(cable = value) }
                    fCableType -> { m -> m.copy(typeCable = value) }
                    else -> { m -> m }
                }
                val originals = batchEditMotors.map { it.copy() }
                val updated = batchEditMotors.map { updateField(it) }
                viewModel.updateMotors(updated)
                showBatchEdit = false
                isSelectMode = false
                selectedIds = emptySet()
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = batchEditSuccessMsg,
                        actionLabel = batchEditUndoLabel,
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.updateMotors(originals)
                    }
                }
            }
        )
    }

    if (showBatchDelete) {
        val count = selectedIds.size
        val batchDeleteSuccessMsg = stringResource(R.string.snackbar_batch_delete_success, count)
        val batchDeleteUndoLabel = stringResource(R.string.snackbar_undo)
        AlertDialog(
            onDismissRequest = { showBatchDelete = false },
            title = { Text(stringResource(R.string.dialog_confirm_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.dialog_delete_confirm_batch_departs, count)) },
            confirmButton = {
                TextButton(onClick = {
                    val deletedIds = selectedIds.toList()
                    val deletedMotors = motors.filter { it.item.id in deletedIds }.map { it.item }
                    viewModel.deleteMotorsByIds(deletedIds)
                    showBatchDelete = false
                    isSelectMode = false
                    selectedIds = emptySet()
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = batchDeleteSuccessMsg,
                            actionLabel = batchDeleteUndoLabel,
                            duration = SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            deletedMotors.forEach { viewModel.addMotor(it) }
                        }
                    }
                }) { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDelete = false }) { Text(stringResource(R.string.close)) }
            }
        )
    }

    importPreviewMotors?.let { previewMotors ->
        AlertDialog(
            onDismissRequest = { viewModel.clearPendingImportMotors() },
            title = { Text(stringResource(R.string.import_title), fontWeight = FontWeight.Bold) },
            text = {
                Text(stringResource(R.string.import_lines_detected, previewMotors.size) + " " + stringResource(R.string.import_departs_confirm_body))
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.confirmImport()
                }) { Text(stringResource(R.string.action_import), color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearPendingImportMotors() }) { Text(stringResource(R.string.close)) }
            }
        )
    }

    if (showExportChoiceDialog) {
        AlertDialog(
            onDismissRequest = { showExportChoiceDialog = false },
            title = { Text(stringResource(R.string.export_choice_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.export_choice_message)) },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        showExportChoiceDialog = false
                        onExportExcel()
                    }) { Text("Excel") }
                    TextButton(onClick = {
                        showExportChoiceDialog = false
                        onExportCsv()
                    }) { Text("CSV") }
                    TextButton(onClick = { showExportChoiceDialog = false }) {
                        Text(stringResource(R.string.close))
                    }
                }
            }
        )
    }
}

@Composable
private fun FilterBar(
    columnFilters: MutableMap<String, Set<String>>,
    searchedMotors: List<SmartSearchResult<MotorEntity>>,
    powerFilter: String?,
    onPowerFilterChange: (String?) -> Unit,
    showFavoritesOnly: Boolean,
    onShowFavoritesChange: (Boolean) -> Unit
) {
    var filterDialogColumn by remember { mutableStateOf<String?>(null) }
    var dialogSelected by remember { mutableStateOf(emptySet<String>()) }

    val activeCount = columnFilters.values.count { it.isNotEmpty() } + (if (powerFilter != null) 1 else 0) + (if (showFavoritesOnly) 1 else 0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.FilterAlt,
            contentDescription = stringResource(R.string.filter_icon_cd),
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        if (activeCount > 0) {
            Text(
                text = "$activeCount",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        FilterChip(
            selected = showFavoritesOnly,
            onClick = { onShowFavoritesChange(!showFavoritesOnly) },
            label = { Text(stringResource(R.string.filter_favorites), fontSize = 11.sp) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        )
        FILTER_COLUMNS.forEach { col ->
            val selected = columnFilters[col.key] ?: emptySet()
            val label = stringResource(col.labelResId)
            val chipLabel = if (selected.isEmpty()) label
            else "$label (${selected.size})"
            FilterChip(
                selected = selected.isNotEmpty(),
                onClick = {
                    filterDialogColumn = col.key
                    dialogSelected = selected
                },
                label = { Text(chipLabel, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
        val powerLowLabel = stringResource(R.string.filter_power_low)
        val powerMediumLabel = stringResource(R.string.filter_power_medium)
        val powerHighLabel = stringResource(R.string.filter_power_high)
        listOf("≤10" to powerLowLabel, "10-50" to powerMediumLabel, ">50" to powerHighLabel).forEach { (value, label) ->
            FilterChip(
                selected = powerFilter == value,
                onClick = {
                    onPowerFilterChange(if (powerFilter == value) null else value)
                },
                label = { Text(label, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }
    }

    filterDialogColumn?.let { column ->
        val values = remember(searchedMotors, column, powerFilter, showFavoritesOnly) {
            val others = columnFilters.filterKeys { it != column }
            val candidates = applyFilters(searchedMotors, others, powerFilter)
                .filter { !showFavoritesOnly || it.item.favorite }
            val available = candidates.map { getColumnValue(it.item, column) }
                .filter { it.isNotBlank() }
            (available + columnFilters[column].orEmpty())
                .distinct()
                .sorted()
        }
        AlertDialog(
            onDismissRequest = { filterDialogColumn = null },
            title = { Text(stringResource(R.string.filter_title, stringResource(columnLabelResId(column))), fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { dialogSelected = emptySet() }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = dialogSelected.isEmpty(),
                            onCheckedChange = { dialogSelected = emptySet() }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.filter_select_all), fontSize = 14.sp)
                    }
                    values.forEach { value ->
                        val checked = value in dialogSelected
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    dialogSelected = if (checked) dialogSelected - value
                                    else dialogSelected + value
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = {
                                    dialogSelected = if (checked) dialogSelected - value
                                    else dialogSelected + value
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(value, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    columnFilters[column] = dialogSelected
                    filterDialogColumn = null
                }) { Text(stringResource(R.string.action_apply)) }
            },
            dismissButton = {
                TextButton(onClick = { filterDialogColumn = null }) { Text(stringResource(R.string.close)) }
            }
        )
    }
}

@Composable
private fun Header(
    motorsCount: Int,
    themeMode: String,
    onToggleDarkMode: () -> Unit,
    onMenuClick: () -> Unit,
    isCardView: Boolean = false,
    onToggleView: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 14.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = stringResource(R.string.header_menu_cd),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.header_departs_title),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

            }
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "$motorsCount",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(
                onClick = onToggleView,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f))
            ) {
                Icon(
                    imageVector = if (isCardView) Icons.Default.ViewList else Icons.Default.ViewModule,
                    contentDescription = if (isCardView) stringResource(R.string.header_view_list_cd) else stringResource(R.string.header_view_cards_cd),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(
                onClick = onToggleDarkMode,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f)
                    )
            ) {
                Icon(
                    imageVector = when (themeMode) {
                        "dark" -> Icons.Default.LightMode
                        "light" -> Icons.Default.DarkMode
                        "materialYou" -> Icons.Default.Palette
                        else -> Icons.Default.BrightnessAuto
                    },
                    contentDescription = stringResource(R.string.header_theme_cd),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun TopBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onImport: () -> Unit,
    onExport: () -> Unit,
    onAdd: () -> Unit,
    onVoiceSearch: () -> Unit = {},
    onToggleSelectMode: () -> Unit = {},
    isSelectMode: Boolean = false,
    searchHistory: List<String> = emptyList(),
    onHistoryItemClick: (String) -> Unit = {},
    suggestions: List<String> = emptyList(),
    onSuggestionClick: (String) -> Unit = {}
) {
    var historyExpanded by remember { mutableStateOf(false) }
    var focused by remember { mutableStateOf(false) }
    var suggestionsVisible by remember { mutableStateOf(true) }
    var searchBarWidth by remember { mutableStateOf(0) }
    val searchBarWidthDp = with(LocalDensity.current) { searchBarWidth.toDp() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier.onSizeChanged { searchBarWidth = it.width }
        ) {
            TextField(
                value = searchQuery,
                onValueChange = {
                    onSearchChange(it)
                    historyExpanded = false
                    suggestionsVisible = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focused = it.isFocused },
                placeholder = {
                    Text(stringResource(R.string.search_hint_departs), fontSize = 13.sp)
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = stringResource(R.string.search_icon_cd),
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                onSearchChange("")
                                historyExpanded = false
                            }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = stringResource(R.string.search_clear_cd),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        if (searchQuery.isBlank() && searchHistory.isNotEmpty()) {
                            IconButton(onClick = { historyExpanded = !historyExpanded }) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = stringResource(R.string.search_history_cd),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        IconButton(onClick = onVoiceSearch) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = stringResource(R.string.search_voice_cd),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            SearchMenuPopup(
                expanded = historyExpanded && searchHistory.isNotEmpty(),
                onDismissRequest = { historyExpanded = false },
                items = searchHistory.take(5),
                onPick = {
                    onHistoryItemClick(it)
                    historyExpanded = false
                },
                contentWidth = searchBarWidthDp
            )
            SearchMenuPopup(
                expanded = focused && searchQuery.isNotBlank() && suggestions.isNotEmpty() && suggestionsVisible,
                onDismissRequest = { suggestionsVisible = false },
                items = suggestions.take(6),
                onPick = {
                    onSuggestionClick(it)
                    suggestionsVisible = false
                },
                contentWidth = searchBarWidthDp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ActionChip(
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.FileUpload,
                    label = stringResource(R.string.action_import),
                    onClick = onImport
                )
                ActionChip(
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.FileDownload,
                    label = stringResource(R.string.action_export),
                    onClick = onExport
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ActionChip(
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.Add,
                    label = stringResource(R.string.action_add),
                    onClick = onAdd
                )
                ActionChip(
                    modifier = Modifier.fillMaxWidth(),
                    icon = if (isSelectMode) Icons.Default.Close else Icons.Default.CheckBox,
                    label = if (isSelectMode) stringResource(R.string.action_quit_select) else stringResource(R.string.action_select),
                    onClick = onToggleSelectMode
                )
            }
        }
    }
}

@Composable
private fun VoiceSearchSuggestion(
    result: VoiceSearchResult,
    onSelectTag: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = stringResource(R.string.search_voice_cd),
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.search_voice_title),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Close, stringResource(R.string.close),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = result.originalQuery,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (result.reconstructedTag.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.voice_tag_reconstructed),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp
                        )
Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = result.reconstructedTag,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }

            if (result.candidates.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.voice_suggestions),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 2.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                result.candidates.take(5).forEach { candidate ->
                    val matchColor = matchTypeColor(candidate.matchType)
                    val matchLabel = when (candidate.matchType) {
                        MatchType.EXACT -> stringResource(R.string.match_exact)
                        MatchType.NORMALIZED -> stringResource(R.string.match_normalized)
                        MatchType.STRIPPED -> stringResource(R.string.match_format)
                        MatchType.VOICE_NORMALIZED -> stringResource(R.string.match_voice)
                        MatchType.SUBSTRING -> stringResource(R.string.match_substring)
                        MatchType.FUZZY -> stringResource(R.string.match_approach)
                    }
                    Surface(
                        onClick = { onSelectTag(candidate.tag) },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = candidate.tag,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .width(50.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(candidate.score)
                                        .background(matchColor, RoundedCornerShape(2.dp))
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${(candidate.score * 100).toInt()}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = matchColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = matchColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = matchLabel,
                                    fontSize = 9.sp,
                                    color = matchColor,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }

                if (result.candidates.size > 5) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.voice_more_results, result.candidates.size - 5),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionChip(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(32.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = label,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
