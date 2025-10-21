package org.example.project.data

import kotlinx.serialization.Serializable

@Serializable
data class HardwareConfig(
    val id: String,
    val name: String,
    val timestamp: Long,
    val systemInfo: SystemInfo,
    val performanceStats: PerformanceStats,
    val rawData: CapFrameXData,
    val graphicsPreset: GraphicsPreset,
    val rtxEnabled: Boolean,
    val frameGenEnabled: Boolean,
    val upscaling: UpscalingSettings
)
