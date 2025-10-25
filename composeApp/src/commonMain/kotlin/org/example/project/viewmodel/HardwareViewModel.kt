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
import org.example.project.data.SaveConfigData
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
        allowSpecialFloatingPointValues = true
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
                println("[HardwareViewModel] Erro ao carregar configurações: ${e.message}")
                e.printStackTrace()
                uiState = uiState.copy(error = "Erro ao carregar configurações: ${e.message}")
            }
        }
    }

    fun parseJsonFile(jsonContent: String) {
        viewModelScope.launch {
            try {
                uiState = uiState.copy(isLoading = true, error = null)

                val capFrameXData = json.decodeFromString<CapFrameXData>(jsonContent)

                if (capFrameXData.runs.isEmpty()) {
                    uiState = uiState.copy(
                        isLoading = false,
                        error = "Nenhum dado de captura encontrado no arquivo"
                    )
                    return@launch
                }

                val stats = PerformanceStats.fromCaptureData(capFrameXData.runs[0].captureData)

                uiState = uiState.copy(
                    isLoading = false,
                    currentData = capFrameXData,
                    currentStats = stats,
                    showSaveDialog = true
                )
            } catch (e: Exception) {
                println("[HardwareViewModel] Erro ao processar arquivo JSON:")
                println("Mensagem: ${e.message}")
                println("Tipo: ${e::class.simpleName}")
                e.printStackTrace()
                uiState = uiState.copy(
                    isLoading = false,
                    error = "Erro ao processar arquivo JSON: ${e.message}"
                )
            }
        }
    }

    fun saveCurrentConfig(configData: SaveConfigData) {
        viewModelScope.launch {
            try {
                val data = uiState.currentData ?: return@launch
                val stats = uiState.currentStats ?: return@launch

                val config = HardwareConfig(
                    id = data.info.id,
                    name = configData.name,
                    timestamp = Clock.System.now().toEpochMilliseconds(),
                    systemInfo = data.info,
                    performanceStats = stats,
                    rawData = data,
                    graphicsPreset = configData.graphicsPreset,
                    rtxEnabled = configData.rtxEnabled,
                    frameGenEnabled = configData.frameGenEnabled,
                    upscaling = configData.upscaling
                )
                
                repository.saveConfig(config)
                loadConfigs()
                
                uiState = uiState.copy(
                    showSaveDialog = false,
                    currentData = null,
                    currentStats = null
                )
            } catch (e: Exception) {
                println("[HardwareViewModel] Erro ao salvar configuração: ${e.message}")
                e.printStackTrace()
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

