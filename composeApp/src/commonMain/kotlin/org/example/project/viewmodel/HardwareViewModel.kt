package org.example.project.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import org.example.project.data.CapFrameXData
import org.example.project.data.HardwareConfig
import org.example.project.data.PerformanceStats
import org.example.project.repository.ConfigRepository
import org.example.project.repository.InMemoryConfigRepository

class HardwareViewModel(
    private val repository: ConfigRepository = InMemoryConfigRepository()
) {
    var uiState by mutableStateOf(HardwareUiState())
        private set

    private val viewModelScope = CoroutineScope(Dispatchers.Default)

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    init {
        loadConfigs()
    }

    fun loadConfigs() {
        viewModelScope.launch {
            try {
                val configs = repository.getAllConfigs()
                uiState = uiState.copy(savedConfigs = configs)
            } catch (e: Exception) {
                uiState = uiState.copy(error = "Erro ao carregar configurações: ${e.message}")
            }
        }
    }

    fun parseJsonFile(jsonContent: String) {
        viewModelScope.launch {
            try {
                uiState = uiState.copy(isLoading = true, error = null)

                val capFrameXData = json.decodeFromString<CapFrameXData>(jsonContent)

                if (capFrameXData.Runs.isEmpty()) {
                    uiState = uiState.copy(
                        isLoading = false,
                        error = "Nenhum dado de captura encontrado no arquivo"
                    )
                    return@launch
                }

                val stats = PerformanceStats.fromCaptureData(capFrameXData.Runs[0].CaptureData)

                uiState = uiState.copy(
                    isLoading = false,
                    currentData = capFrameXData,
                    currentStats = stats,
                    showSaveDialog = true
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = "Erro ao processar arquivo JSON: ${e.message}"
                )
            }
        }
    }

    fun saveCurrentConfig(name: String) {
        viewModelScope.launch {
            try {
                val data = uiState.currentData ?: return@launch
                val stats = uiState.currentStats ?: return@launch

                val config = HardwareConfig(
                    id = generateUUID(),
                    name = name,
                    timestamp = Clock.System.now().toEpochMilliseconds(),
                    systemInfo = data.Info,
                    performanceStats = stats,
                    rawData = data
                )
                
                repository.saveConfig(config)
                loadConfigs()
                
                uiState = uiState.copy(
                    showSaveDialog = false,
                    currentData = null,
                    currentStats = null
                )
            } catch (e: Exception) {
                uiState = uiState.copy(error = "Erro ao salvar configuração: ${e.message}")
            }
        }
    }
    
    fun cancelSave() {
        uiState = uiState.copy(
            showSaveDialog = false,
            currentData = null,
            currentStats = null
        )
    }
    
    fun deleteConfig(id: String) {
        viewModelScope.launch {
            repository.deleteConfig(id)
            loadConfigs()
        }
    }
    
    fun selectConfigForComparison(config: HardwareConfig) {
        val selected = uiState.selectedForComparison.toMutableList()
        if (selected.contains(config)) {
            selected.remove(config)
        } else if (selected.size < 4) {
            selected.add(config)
        }
        uiState = uiState.copy(selectedForComparison = selected)
    }
    
    fun clearError() {
        uiState = uiState.copy(error = null)
    }
}

data class HardwareUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentData: CapFrameXData? = null,
    val currentStats: PerformanceStats? = null,
    val showSaveDialog: Boolean = false,
    val savedConfigs: List<HardwareConfig> = emptyList(),
    val selectedForComparison: List<HardwareConfig> = emptyList()
)

private fun generateUUID(): String {
    val chars = "0123456789abcdef"
    return buildString {
        repeat(8) { append(chars.random()) }
        append('-')
        repeat(4) { append(chars.random()) }
        append('-')
        append('4')
        repeat(3) { append(chars.random()) }
        append('-')
        append(chars.random())
        repeat(3) { append(chars.random()) }
        append('-')
        repeat(12) { append(chars.random()) }
    }
}
