package org.example.project.repository

import org.example.project.data.HardwareConfig

interface ConfigRepository {
    suspend fun saveConfig(config: HardwareConfig)
    suspend fun getAllConfigs(): List<HardwareConfig>
    suspend fun getConfigById(id: String): HardwareConfig?
    suspend fun deleteConfig(id: String)
}

class InMemoryConfigRepository : ConfigRepository {
    private val configs = mutableListOf<HardwareConfig>()
    
    override suspend fun saveConfig(config: HardwareConfig) {
        configs.removeAll { it.id == config.id }
        configs.add(config)
    }
    
    override suspend fun getAllConfigs(): List<HardwareConfig> {
        return configs.sortedByDescending { it.timestamp }
    }
    
    override suspend fun getConfigById(id: String): HardwareConfig? {
        return configs.find { it.id == id }
    }
    
    override suspend fun deleteConfig(id: String) {
        configs.removeAll { it.id == id }
    }
}
