package org.example.project.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.project.data.CapFrameXData
import org.example.project.data.GraphicsPreset
import org.example.project.data.HardwareConfig
import org.example.project.data.PerformanceStats
import org.example.project.data.UpscalingQuality
import org.example.project.data.UpscalingSettings
import org.example.project.data.UpscalingType
import org.example.project.database.HardwareDatabase
import org.example.project.database.Hardware_config

class SqlDelightConfigRepository(
    private val database: HardwareDatabase
) : ConfigRepository {
    
    private val queries = database.hardwareConfigQueries
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    override suspend fun saveConfig(config: HardwareConfig) = withContext(Dispatchers.Default) {
        queries.insertConfig(
            id = config.id,
            name = config.name,
            timestamp = config.timestamp,
            game_name = config.systemInfo.gameName,
            processor = config.systemInfo.processor,
            gpu = config.systemInfo.gpu,
            os = config.systemInfo.os,
            system_ram = config.systemInfo.systemRam,
            gpu_driver_version = config.systemInfo.gpuDriverVersion,
            motherboard = config.systemInfo.motherboard,
            resizable_bar = config.systemInfo.resizableBar,
            win_game_mode = config.systemInfo.winGameMode,
            hags = config.systemInfo.hags,
            api_info = config.systemInfo.apiInfo,
            graphics_preset = config.graphicsPreset.name,
            rtx_enabled = config.rtxEnabled,
            framegen_enabled = config.frameGenEnabled,
            upscaling_type = config.upscaling.type.name,
            upscaling_quality = config.upscaling.quality?.name,
            avg_fps = config.performanceStats.avgFps,
            min_fps = config.performanceStats.minFps,
            max_fps = config.performanceStats.maxFps,
            percentile_1 = config.performanceStats.percentile1,
            percentile_5 = config.performanceStats.percentile5,
            percentile_95 = config.performanceStats.percentile95,
            percentile_99 = config.performanceStats.percentile99,
            total_frames = config.performanceStats.totalFrames.toLong(),
            duration = config.performanceStats.duration,
            raw_json = json.encodeToString(config.rawData)
        )
    }
    
    override suspend fun getAllConfigs(): List<HardwareConfig> = withContext(Dispatchers.Default) {
        queries.getAllConfigs().executeAsList().map { it.toHardwareConfig() }
    }
    
    override suspend fun getConfigById(id: String): HardwareConfig? = withContext(Dispatchers.Default) {
        queries.getConfigById(id).executeAsOneOrNull()?.toHardwareConfig()
    }
    
    override suspend fun deleteConfig(id: String) = withContext(Dispatchers.Default) {
        queries.deleteConfig(id)
    }
    
    // Queries adicionais para filtros e gráficos
    
    suspend fun getConfigsByGame(gameName: String): List<HardwareConfig> = withContext(Dispatchers.Default) {
        queries.getConfigsByGame(gameName).executeAsList().map { it.toHardwareConfig() }
    }
    
    suspend fun getConfigsByGpu(gpu: String): List<HardwareConfig> = withContext(Dispatchers.Default) {
        queries.getConfigsByGpu(gpu).executeAsList().map { it.toHardwareConfig() }
    }
    
    suspend fun getConfigsByCpu(processor: String): List<HardwareConfig> = withContext(Dispatchers.Default) {
        queries.getConfigsByCpu(processor).executeAsList().map { it.toHardwareConfig() }
    }
    
    suspend fun getConfigsByPreset(preset: GraphicsPreset): List<HardwareConfig> = withContext(Dispatchers.Default) {
        queries.getConfigsByPreset(preset.name).executeAsList().map { it.toHardwareConfig() }
    }
    
    suspend fun getConfigsByUpscaling(type: UpscalingType): List<HardwareConfig> = withContext(Dispatchers.Default) {
        queries.getConfigsByUpscaling(type.name).executeAsList().map { it.toHardwareConfig() }
    }
    
    suspend fun getConfigsByGameAndGpu(gameName: String, gpu: String): List<HardwareConfig> = withContext(Dispatchers.Default) {
        queries.getConfigsByGameAndGpu(gameName, gpu).executeAsList().map { it.toHardwareConfig() }
    }
    
    // Queries para gráficos agregados
    
    suspend fun getAvgFpsByPreset(gameName: String, gpu: String) = withContext(Dispatchers.Default) {
        queries.getAvgFpsByPreset(gameName, gpu).executeAsList()
    }
    
    suspend fun getAvgFpsByUpscaling(gameName: String, gpu: String, preset: GraphicsPreset) = withContext(Dispatchers.Default) {
        queries.getAvgFpsByUpscaling(gameName, gpu, preset.name).executeAsList()
    }
    
    suspend fun getAvgFpsByGpu(gameName: String, preset: GraphicsPreset) = withContext(Dispatchers.Default) {
        queries.getAvgFpsByGpu(gameName, preset.name).executeAsList()
    }
    
    // Queries para filtros de UI
    
    suspend fun getUniqueGames(): List<String> = withContext(Dispatchers.Default) {
        queries.getUniqueGames().executeAsList()
    }
    
    suspend fun getUniqueGpus(): List<String> = withContext(Dispatchers.Default) {
        queries.getUniqueGpus().executeAsList()
    }
    
    suspend fun getUniqueCpus(): List<String> = withContext(Dispatchers.Default) {
        queries.getUniqueCpus().executeAsList()
    }
    
    // Flow para observar mudanças em tempo real
    
    fun observeAllConfigs(): Flow<List<HardwareConfig>> {
        return queries.getAllConfigs()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list -> list.map { it.toHardwareConfig() } }
    }
    
    // Conversão de entidade do banco para HardwareConfig
    
    private fun Hardware_config.toHardwareConfig(): HardwareConfig {
        val rawData = json.decodeFromString<CapFrameXData>(raw_json)
        
        return HardwareConfig(
            id = id,
            name = name,
            timestamp = timestamp,
            systemInfo = rawData.info,
            performanceStats = PerformanceStats(
                avgFps = avg_fps,
                minFps = min_fps,
                maxFps = max_fps,
                percentile1 = percentile_1,
                percentile5 = percentile_5,
                percentile95 = percentile_95,
                percentile99 = percentile_99,
                totalFrames = total_frames.toInt(),
                duration = duration,
                frameTimeAvg = TODO(),
                frameTimeMin = TODO(),
                frameTimeMax = TODO()
            ),
            rawData = rawData,
            graphicsPreset = GraphicsPreset.valueOf(graphics_preset),
            rtxEnabled = rtx_enabled,
            frameGenEnabled = framegen_enabled,
            upscaling = UpscalingSettings(
                type = UpscalingType.valueOf(upscaling_type),
                quality = upscaling_quality?.let { UpscalingQuality.valueOf(it) }
            )
        )
    }
}
