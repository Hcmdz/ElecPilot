@file:OptIn(FlowPreview::class)

package com.HcmDz.ElecPilot.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.HcmDz.ElecPilot.data.db.PlcDatabase
import com.HcmDz.ElecPilot.data.db.PlcEntity
import com.HcmDz.ElecPilot.data.repository.PlcRepository
import com.HcmDz.ElecPilot.R
import com.HcmDz.ElecPilot.util.ExcelUtil
import com.HcmDz.ElecPilot.util.MatchType
import com.HcmDz.ElecPilot.util.SearchCandidate
import com.HcmDz.ElecPilot.util.SearchIndex
import com.HcmDz.ElecPilot.util.SmartSearchResult
import com.HcmDz.ElecPilot.util.VoiceSearchEngine
import com.HcmDz.ElecPilot.util.VoiceSearchResult
import com.HcmDz.ElecPilot.util.localizedContext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlcViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PlcRepository
    val voiceSearchEngine = VoiceSearchEngine()

    private val _voiceSearchResult = MutableStateFlow<VoiceSearchResult?>(null)
    val voiceSearchResult: StateFlow<VoiceSearchResult?> = _voiceSearchResult

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _allPlc = MutableStateFlow<List<PlcEntity>>(emptyList())
    val allPlc: StateFlow<List<PlcEntity>> = _allPlc
    private val _favoriteIds = MutableStateFlow<Set<Long>>(emptySet())

    @Volatile
    private var searchIndex: SearchIndex<PlcEntity>? = null

    private fun getIndex(plcList: List<PlcEntity>): SearchIndex<PlcEntity> {
        val idx = searchIndex
        if (idx != null && idx.items == plcList) return idx
        return SearchIndex(plcList, PLC_FIELD_EXTRACTORS, PLC_COMBINED).also { searchIndex = it }
    }

    private val _plcResults = MutableStateFlow<List<SmartSearchResult<PlcEntity>>>(emptyList())
    val plcResults: StateFlow<List<SmartSearchResult<PlcEntity>>> = _plcResults

    private val _selectedPlc = MutableStateFlow<PlcEntity?>(null)
    val selectedPlc: StateFlow<PlcEntity?> = _selectedPlc

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _hasLoadedOnce = MutableStateFlow(false)
    val hasLoadedOnce: StateFlow<Boolean> = _hasLoadedOnce

    private val _pendingImportPlc = MutableStateFlow<List<PlcEntity>?>(null)
    val pendingImportPlc: StateFlow<List<PlcEntity>?> = _pendingImportPlc

    private val _isImporting = MutableStateFlow(false)

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory

    private val _suggestions = MutableStateFlow<List<SmartSearchResult<PlcEntity>>>(emptyList())
    val suggestions: StateFlow<List<SmartSearchResult<PlcEntity>>> = _suggestions

    init {
        val db = PlcDatabase.getInstance(application)
        repository = PlcRepository(db.plcDao())
        val prefs = application.getSharedPreferences("plc_settings", Context.MODE_PRIVATE)
        val history = prefs.getString("search_history", "") ?: ""
        _searchHistory.value = if (history.isNotBlank()) history.split("|||") else emptyList()

        viewModelScope.launch {
            repository.getAllPlc().collect {
                _allPlc.value = it
                _favoriteIds.value = it.filter { p -> p.favorite }.map { p -> p.id }.toSet()
                _hasLoadedOnce.value = true
            }
        }

        viewModelScope.launch {
            combine(
                _searchQuery.debounce(300).distinctUntilChanged(),
                _allPlc,
                _favoriteIds
            ) { query, all, favIds -> Triple(query, all, favIds) }
                .collect { (query, all, favIds) ->
                    val tagged = all.map {
                        if (it.id in favIds) it.copy(favorite = true) else it.copy(favorite = false)
                    }
                    if (query.isBlank()) {
                        searchIndex = null
                        _plcResults.value = tagged.map { SmartSearchResult<PlcEntity>(it, 0f, MatchType.EXACT, "") }
                    } else {
                        val results = withContext(Dispatchers.Default) {
                            val index = getIndex(all)
                            voiceSearchEngine.smartSearch(index, query).map { sr ->
                                sr.copy(item = if (sr.item.id in favIds) sr.item.copy(favorite = true) else sr.item.copy(favorite = false))
                            }
                        }
                        _plcResults.value = results
                    }
                }
        }

        @OptIn(FlowPreview::class)
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    val sug = if (query.isBlank()) {
                        emptyList()
                    } else {
                        withContext(Dispatchers.Default) {
                            val index = getIndex(_allPlc.value)
                            voiceSearchEngine.autocompleteSuggest(index, query, 8)
                        }
                    }
                    _suggestions.value = sug
                }
        }

        @OptIn(FlowPreview::class)
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .filter { it.isNotBlank() }
                .collect { query ->
                    val hasResults = _plcResults.value.any { it.score > 0f }
                    if (!hasResults) return@collect
                    val current = _searchHistory.value.toMutableList()
                    current.remove(query)
                    current.add(0, query)
                    _searchHistory.value = current.take(10)
                    saveHistoryJob?.cancel()
                    saveHistoryJob = viewModelScope.launch {
                        delay(500)
                        val trimmed = _searchHistory.value
                        prefs.edit().putString("search_history", trimmed.joinToString("|||")).apply()
                    }
                }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSuggestions() {
        _suggestions.value = emptyList()
    }

    fun selectPlc(plc: PlcEntity?) {
        _selectedPlc.value = plc
    }

    fun updatePlc(plc: PlcEntity) {
        viewModelScope.launch {
            repository.update(plc)
        }
    }

    fun deletePlc(plc: PlcEntity) {
        viewModelScope.launch {
            repository.delete(plc)
        }
    }

    fun deletePlcByIds(ids: List<Long>) {
        viewModelScope.launch {
            repository.deletePlcByIds(ids)
        }
    }

    fun deleteAllPlc() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

    suspend fun deleteAllPlcNow() {
        repository.deleteAll()
    }

    suspend fun count(): Int = repository.count()

    fun addPlc(plc: PlcEntity) {
        viewModelScope.launch {
            repository.insert(plc)
        }
    }

    fun updatePlcs(plcList: List<PlcEntity>) {
        viewModelScope.launch {
            repository.updateAll(plcList)
        }
    }

    fun getCurrentPlcs(): List<PlcEntity> = _allPlc.value

    private fun PlcEntity.importKey(): String = listOf(atelier, dp, carte, position, item, designation)
        .joinToString("|") { it.trim().replace("\\s+".toRegex(), " ") }

    fun importPlc(plcList: List<PlcEntity>, manageLoading: Boolean = true) {
        viewModelScope.launch {
            if (manageLoading) setLoading(true)
            try {
                val existing = repository.getAllPlcOnce()
                val existingKeys = existing.map { it.importKey() }.toSet()
                val newPlc = plcList.filter { it.importKey() !in existingKeys }
                if (newPlc.isNotEmpty()) {
                    repository.insertAll(newPlc)
                    val skipped = plcList.size - newPlc.size
                    showSnackbar(localizedContext(getApplication()).getString(R.string.import_success_plc, newPlc.size, skipped))
                } else {
                    showSnackbar(localizedContext(getApplication()).getString(R.string.import_no_new_plc, plcList.size))
                }
                _pendingImportPlc.value = null
                _searchQuery.value = ""
            } catch (e: Exception) {
                android.util.Log.e("ElecPilot", "Import failed", e)
                showSnackbar(localizedContext(getApplication()).getString(R.string.snackbar_import_error))
            } finally {
                _isImporting.value = false
                if (manageLoading) setLoading(false)
            }
        }
    }

    suspend fun importPlcDirect(plcList: List<PlcEntity>): Int {
        return try {
            val existing = repository.getAllPlcOnce()
            val existingKeys = existing.map { it.importKey() }.toSet()
            val newPlc = plcList.filter { it.importKey() !in existingKeys }
            if (newPlc.isNotEmpty()) {
                repository.insertAll(newPlc)
                _searchQuery.value = ""
                newPlc.size
            } else {
                0
            }
        } catch (e: Exception) {
            android.util.Log.e("ElecPilot", "Error in importPlcDirect", e)
            throw e
        }
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    private val _snackbarEvent = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    fun showSnackbar(message: String) {
        _snackbarEvent.tryEmit(message)
    }

    fun requestImportWithPreview(uri: Uri, isExcel: Boolean) {
        viewModelScope.launch {
            setLoading(true)
            try {
                val parsedPlc = withContext(Dispatchers.IO) {
                    ExcelUtil.importPlcsFromUri(getApplication(), uri, isExcel)
                }
                if (parsedPlc.isEmpty()) {
                    showSnackbar(localizedContext(getApplication()).getString(R.string.import_no_data))
                    return@launch
                }
                val dbCount = repository.count()
                if (dbCount > 0) {
                    _pendingImportPlc.value = parsedPlc
                } else {
                    importPlc(parsedPlc, manageLoading = false)
                    return@launch
                }
            } catch (e: Exception) {
                android.util.Log.e("ElecPilot", "Excel import failed", e)
                showSnackbar(localizedContext(getApplication()).getString(R.string.snackbar_import_error))
            } finally {
                setLoading(false)
            }
        }
    }

    fun confirmImport() {
        if (_isImporting.value) return
        _pendingImportPlc.value?.let { plcList ->
            _isImporting.value = true
            setLoading(true)
            importPlc(plcList, manageLoading = false)
        }
    }

    fun clearPendingImportPlc() {
        _pendingImportPlc.value = null
    }

    fun toggleFavorite(plc: PlcEntity) {
        val newFav = plc.id !in _favoriteIds.value
        _favoriteIds.value = if (newFav) {
            _favoriteIds.value + plc.id
        } else {
            _favoriteIds.value - plc.id
        }
        if (_selectedPlc.value?.id == plc.id) {
            _selectedPlc.value = plc.copy(favorite = newFav)
        }
        viewModelScope.launch {
            try {
                repository.updateFavorite(plc.id, newFav)
            } catch (e: Exception) {
                _favoriteIds.value = if (!newFav) {
                    _favoriteIds.value + plc.id
                } else {
                    _favoriteIds.value - plc.id
                }
                if (_selectedPlc.value?.id == plc.id) {
                    _selectedPlc.value = plc.copy(favorite = !newFav)
                }
                android.util.Log.e("ElecPilot", "Favorite toggle failed", e)
                showSnackbar(localizedContext(getApplication()).getString(R.string.snackbar_favorite_error))
            }
        }
    }

    private var saveHistoryJob: Job? = null

    fun processVoiceSearch(rawText: String) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) { voiceSearchEngine.processVoiceInput(rawText) }
            val all = _allPlc.value.map {
                if (it.id in _favoriteIds.value) it.copy(favorite = true) else it.copy(favorite = false)
            }
            if (all.isNotEmpty()) {
                val (index, smartResults) = withContext(Dispatchers.Default) {
                    val idx = getIndex(all)
                    idx to voiceSearchEngine.smartSearch(idx, rawText, 8)
                }
                val candidates = smartResults.map { sr ->
                    val tag = when {
                        sr.matchedOn == "designation" -> sr.item.designation
                        sr.matchedOn == "item" -> sr.item.item
                        else -> "${sr.item.atelier}${sr.item.dp}${sr.item.designation}"
                    }
                    SearchCandidate(tag.ifBlank { sr.item.designation }, sr.score, sr.matchType)
                }
                _voiceSearchResult.value = result.copy(candidates = candidates)
            } else {
                _voiceSearchResult.value = result.copy(candidates = emptyList())
            }
            if (result.reconstructedTag.isNotBlank()) {
                _searchQuery.value = result.reconstructedTag
            }
        }
    }

    fun clearVoiceSearchResult() {
        _voiceSearchResult.value = null
    }
}

private val PLC_FIELD_EXTRACTORS = listOf<Pair<String, (PlcEntity) -> String>>(
    "item" to { it.item }
)

private val PLC_COMBINED: (PlcEntity) -> String = { it.item }
