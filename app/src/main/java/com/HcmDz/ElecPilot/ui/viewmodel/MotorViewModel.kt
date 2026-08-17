@file:OptIn(FlowPreview::class)

package com.HcmDz.ElecPilot.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.HcmDz.ElecPilot.data.db.AppDatabase
import com.HcmDz.ElecPilot.data.db.MotorEntity
import com.HcmDz.ElecPilot.data.repository.MotorRepository
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

class MotorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MotorRepository
    val voiceSearchEngine = VoiceSearchEngine()

    private val _voiceSearchResult = MutableStateFlow<VoiceSearchResult?>(null)
    val voiceSearchResult: StateFlow<VoiceSearchResult?> = _voiceSearchResult

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _allMotors = MutableStateFlow<List<MotorEntity>>(emptyList())
    val allMotors: StateFlow<List<MotorEntity>> = _allMotors
    private val _favoriteIds = MutableStateFlow<Set<Long>>(emptySet())

    @Volatile
    private var motorIndex: SearchIndex<MotorEntity>? = null

    private fun getIndex(motors: List<MotorEntity>): SearchIndex<MotorEntity> {
        val idx = motorIndex
        if (idx != null && idx.items == motors) return idx
        return SearchIndex(motors, MOTOR_FIELD_EXTRACTORS, MOTOR_COMBINED).also { motorIndex = it }
    }

    private val _motors = MutableStateFlow<List<SmartSearchResult<MotorEntity>>>(emptyList())
    val motors: StateFlow<List<SmartSearchResult<MotorEntity>>> = _motors

    private val _selectedMotor = MutableStateFlow<MotorEntity?>(null)
    val selectedMotor: StateFlow<MotorEntity?> = _selectedMotor

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _hasLoadedOnce = MutableStateFlow(false)
    val hasLoadedOnce: StateFlow<Boolean> = _hasLoadedOnce

    private val _pendingImportMotors = MutableStateFlow<List<MotorEntity>?>(null)
    val pendingImportMotors: StateFlow<List<MotorEntity>?> = _pendingImportMotors

    private val _isImporting = MutableStateFlow(false)

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory

    private val _suggestions = MutableStateFlow<List<SmartSearchResult<MotorEntity>>>(emptyList())
    val suggestions: StateFlow<List<SmartSearchResult<MotorEntity>>> = _suggestions

    init {
        val db = AppDatabase.getInstance(application)
        repository = MotorRepository(db.motorDao())
        val prefs = application.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val history = prefs.getString("search_history", "") ?: ""
        _searchHistory.value = if (history.isNotBlank()) history.split("|||") else emptyList()

        viewModelScope.launch {
            repository.getAllMotors().collect {
                _allMotors.value = it
                _favoriteIds.value = it.filter { m -> m.favorite }.map { m -> m.id }.toSet()
                _hasLoadedOnce.value = true
            }
        }

        viewModelScope.launch {
            combine(
                _searchQuery.debounce(300).distinctUntilChanged(),
                _allMotors,
                _favoriteIds
            ) { query, all, favIds -> Triple(query, all, favIds) }
                .collect { (query, all, favIds) ->
                    val tagged = all.map {
                        if (it.id in favIds) it.copy(favorite = true) else it.copy(favorite = false)
                    }
                    if (query.isBlank()) {
                        motorIndex = null
                        _motors.value = tagged.map { SmartSearchResult<MotorEntity>(it, 0f, MatchType.EXACT, "") }
                    } else {
                        val results = withContext(Dispatchers.Default) {
                            val index = getIndex(all)
                            voiceSearchEngine.smartSearch(index, query).map { sr ->
                                sr.copy(item = if (sr.item.id in favIds) sr.item.copy(favorite = true) else sr.item.copy(favorite = false))
                            }
                        }
                        _motors.value = results
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
                            val index = getIndex(_allMotors.value)
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
                    val hasResults = _motors.value.any { it.score > 0f }
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

    fun selectMotor(motor: MotorEntity?) {
        _selectedMotor.value = motor
    }

    fun updateMotor(motor: MotorEntity) {
        viewModelScope.launch {
            repository.update(motor)
        }
    }

    fun updateMotors(motors: List<MotorEntity>) {
        viewModelScope.launch {
            repository.updateAll(motors)
        }
    }

    fun addMotor(motor: MotorEntity) {
        viewModelScope.launch {
            repository.insert(motor)
        }
    }

    fun deleteMotor(motor: MotorEntity) {
        viewModelScope.launch {
            repository.delete(motor)
        }
    }

    fun deleteMotorsByIds(ids: List<Long>) {
        viewModelScope.launch {
            repository.deleteMotorsByIds(ids)
        }
    }

    fun deleteAllMotors() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

    suspend fun deleteAllMotorsNow() {
        repository.deleteAll()
    }

    suspend fun count(): Int = repository.count()

    private fun MotorEntity.importKey(): String = listOf(
        atelier, tgbt, designation, item, positionTGBT, puissanceKW, types, typesDeparts, cable, typeCable
    ).joinToString("|") { it.trim().replace("\\s+".toRegex(), " ") }

    fun importMotors(motors: List<MotorEntity>, manageLoading: Boolean = true) {
        viewModelScope.launch {
            if (manageLoading) setLoading(true)
            try {
                val existing = repository.getAllMotorsOnce()
                val existingKeys = existing.map { it.importKey() }.toSet()
                val newMotors = motors.filter { it.importKey() !in existingKeys }
                if (newMotors.isNotEmpty()) {
                    repository.insertAll(newMotors)
                    val skipped = motors.size - newMotors.size
                    showSnackbar(localizedContext(getApplication()).getString(R.string.import_success_departs, newMotors.size, skipped))
                } else {
                    showSnackbar(localizedContext(getApplication()).getString(R.string.import_no_new_departs, motors.size))
                }
                _pendingImportMotors.value = null
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

    suspend fun importMotorsDirect(motors: List<MotorEntity>): Int {
        return try {
            val existing = repository.getAllMotorsOnce()
            val existingKeys = existing.map { it.importKey() }.toSet()
            val newMotors = motors.filter { it.importKey() !in existingKeys }
            if (newMotors.isNotEmpty()) {
                repository.insertAll(newMotors)
                _searchQuery.value = ""
                newMotors.size
            } else {
                0
            }
        } catch (e: Exception) {
            android.util.Log.e("ElecPilot", "Error in importMotorsDirect", e)
            throw e
        }
    }

    fun getCurrentMotors(): List<MotorEntity> = _allMotors.value

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
                val parsedMotors = withContext(Dispatchers.IO) {
                    ExcelUtil.importMotorsFromUri(getApplication(), uri, isExcel)
                }
                if (parsedMotors.isEmpty()) {
                    showSnackbar(localizedContext(getApplication()).getString(R.string.import_no_data))
                    return@launch
                }
                val dbCount = repository.count()
                if (dbCount > 0) {
                    _pendingImportMotors.value = parsedMotors
                } else {
                    importMotors(parsedMotors, manageLoading = false)
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
        _pendingImportMotors.value?.let { motors ->
            _isImporting.value = true
            setLoading(true)
            importMotors(motors, manageLoading = false)
        }
    }

    fun clearPendingImportMotors() {
        _pendingImportMotors.value = null
    }

    fun toggleFavorite(motor: MotorEntity) {
        val newFav = motor.id !in _favoriteIds.value
        _favoriteIds.value = if (newFav) {
            _favoriteIds.value + motor.id
        } else {
            _favoriteIds.value - motor.id
        }
        if (_selectedMotor.value?.id == motor.id) {
            _selectedMotor.value = motor.copy(favorite = newFav)
        }
        viewModelScope.launch {
            try {
                repository.updateFavorite(motor.id, newFav)
            } catch (e: Exception) {
                _favoriteIds.value = if (!newFav) {
                    _favoriteIds.value + motor.id
                } else {
                    _favoriteIds.value - motor.id
                }
                if (_selectedMotor.value?.id == motor.id) {
                    _selectedMotor.value = motor.copy(favorite = !newFav)
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
            val all = _allMotors.value.map {
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
                        sr.matchedOn == "atelier+tgbt" -> "${sr.item.atelier}${sr.item.tgbt}"
                        else -> "${sr.item.atelier}${sr.item.tgbt}${sr.item.designation}"
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

private val MOTOR_FIELD_EXTRACTORS = listOf<Pair<String, (MotorEntity) -> String>>(
    "designation" to { it.designation },
    "atelier" to { it.atelier },
    "tgbt" to { it.tgbt },
    "positionTGBT" to { it.positionTGBT },
    "item" to { it.item },
    "types" to { it.types },
    "typesDeparts" to { it.typesDeparts },
    "cable" to { it.cable },
    "typeCable" to { it.typeCable },
    "puissanceKW" to { it.puissanceKW },
    "atelier+tgbt" to { "${it.atelier}${it.tgbt}" },
    "types+departs" to { "${it.types}${it.typesDeparts}" },
    "designation+puissance" to { "${it.designation}${it.puissanceKW}" },
    "atelier+item" to { "${it.atelier}${it.item}" },
    "fullTag" to { it.item.ifBlank { "${it.atelier}${it.tgbt}${it.designation}" } }
)

private val MOTOR_COMBINED: (MotorEntity) -> String = { "${it.atelier}${it.tgbt}${it.designation}${it.item}" }
