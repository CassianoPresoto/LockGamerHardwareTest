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
    
    fun parseMultipleJsonFiles(jsonContents: List<String>) {
        viewModelScope.launch {
            try {
                uiState = uiState.copy(isLoading = true, error = null)
                
                val parsedDataList = mutableListOf<Pair<CapFrameXData, PerformanceStats>>()
                val errors = mutableListOf<String>()
                
                jsonContents.forEachIndexed { index, jsonContent ->
                    try {
                        val capFrameXData = json.decodeFromString<CapFrameXData>(jsonContent)
                        
                        if (capFrameXData.runs.isNotEmpty()) {
                            val stats = PerformanceStats.fromCaptureData(capFrameXData.runs[0].captureData)
                            parsedDataList.add(capFrameXData to stats)
                        } else {
                            errors.add("Arquivo ${index + 1}: Sem dados de captura")
                        }
                    } catch (e: Exception) {
                        errors.add("Arquivo ${index + 1}: ${e.message}")
                        println("[HardwareViewModel] Erro no arquivo ${index + 1}: ${e.message}")
                    }
                }
                
                if (parsedDataList.isEmpty()) {
                    uiState = uiState.copy(
                        isLoading = false,
                        error = "Nenhum arquivo válido encontrado\n${errors.take(3).joinToString("\n")}"
                    )
                    return@launch
                }
                
                // Store parsed data and show batch config dialog
                uiState = uiState.copy(
                    isLoading = false,
                    batchParsedData = parsedDataList,
                    showBatchConfigDialog = true,
                    batchImportErrors = errors.ifEmpty { null }
                )
            } catch (e: Exception) {
                println("[HardwareViewModel] Erro na importação em massa: ${e.message}")
                e.printStackTrace()
                uiState = uiState.copy(
                    isLoading = false,
                    error = "Erro na importação em massa: ${e.message}"
                )
            }
        }
    }
    
    fun saveBatchConfigs(configData: SaveConfigData) {
        viewModelScope.launch {
            try {
                val parsedData = uiState.batchParsedData ?: return@launch
                
                uiState = uiState.copy(
                    isLoading = true,
                    showBatchConfigDialog = false,
                    batchImportProgress = BatchImportProgress(0, parsedData.size)
                )
                
                var successCount = 0
                
                parsedData.forEachIndexed { index, (capFrameXData, stats) ->
                    try {
                        // Generate name with game and hardware info
                        val gameName = capFrameXData.info.gameName.ifBlank { "Unknown" }
                        val gpuName = capFrameXData.info.gpu.split(" ").takeLast(2).joinToString(" ")
                        val configName = "$gameName - $gpuName"
                        
                        val config = HardwareConfig(
                            id = capFrameXData.info.id,
                            name = configName,
                            timestamp = Clock.System.now().toEpochMilliseconds(),
                            systemInfo = capFrameXData.info,
                            performanceStats = stats,
                            rawData = capFrameXData,
                            graphicsPreset = configData.graphicsPreset,
                            rtxEnabled = configData.rtxEnabled,
                            frameGenEnabled = configData.frameGenEnabled,
                            upscaling = configData.upscaling
                        )
                        
                        repository.saveConfig(config)
                        successCount++
                    } catch (e: Exception) {
                        println("[HardwareViewModel] Erro ao salvar config ${index + 1}: ${e.message}")
                    }
                    
                    // Update progress
                    uiState = uiState.copy(
                        batchImportProgress = BatchImportProgress(index + 1, parsedData.size)
                    )
                }
                
                loadConfigs()
                
                val errorCount = parsedData.size - successCount
                val message = buildString {
                    append("Importação concluída: ")
                    append("$successCount sucesso(s)")
                    if (errorCount > 0) {
                        append(", $errorCount erro(s)")
                    }
                    val previousErrors = uiState.batchImportErrors
                    if (previousErrors != null && previousErrors.isNotEmpty()) {
                        append("\n${previousErrors.take(3).joinToString("\n")}")
                    }
                }
                
                uiState = uiState.copy(
                    isLoading = false,
                    batchImportProgress = null,
                    batchParsedData = null,
                    batchImportErrors = null,
                    batchImportSuccess = message
                )
            } catch (e: Exception) {
                println("[HardwareViewModel] Erro ao salvar configurações em lote: ${e.message}")
                e.printStackTrace()
                uiState = uiState.copy(
                    isLoading = false,
                    batchImportProgress = null,
                    error = "Erro ao salvar configurações: ${e.message}"
                )
            }
        }
    }
    
    fun cancelBatchImport() {
        uiState = uiState.copy(
            showBatchConfigDialog = false,
            batchParsedData = null,
            batchImportErrors = null
        )
    }
    
    fun clearBatchImportSuccess() {
        uiState = uiState.copy(batchImportSuccess = null)
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
    val selectedForComparison: List<HardwareConfig> = emptyList(),
    val batchImportProgress: BatchImportProgress? = null,
    val batchImportSuccess: String? = null,
    val batchParsedData: List<Pair<CapFrameXData, PerformanceStats>>? = null,
    val showBatchConfigDialog: Boolean = false,
    val batchImportErrors: List<String>? = null
)

data class BatchImportProgress(
    val current: Int,
    val total: Int
) {
    val percentage: Float get() = if (total > 0) (current.toFloat() / total.toFloat()) else 0f
}

