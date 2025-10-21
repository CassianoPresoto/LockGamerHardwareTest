package org.example.project.data

import kotlinx.serialization.Serializable

@Serializable
enum class GraphicsPreset(val label: String) {
    LOW("Baixo"),
    MEDIUM("Médio"),
    HIGH("Alto"),
    MAXIMUM("Máximo");
}

@Serializable
enum class UpscalingType(val label: String, val supportsQuality: Boolean) {
    OFF("OFF", false),
    DLSS("DLSS", true),
    FSR("FSR", true),
    XESS("XESS", true),
    NATIVE("Nativo", true);
}

@Serializable
enum class UpscalingQuality(val label: String) {
    ULTRA_PERFORMANCE("Ultra Desempenho"),
    PERFORMANCE("Desempenho"),
    BALANCED("Equilibrado"),
    QUALITY("Qualidade");
}

@Serializable
data class UpscalingSettings(
    val type: UpscalingType = UpscalingType.OFF,
    val quality: UpscalingQuality? = null
)

object SaveConfigOptions {
    val presets: List<GraphicsPreset> = GraphicsPreset.entries
    val upscalingTypes: List<UpscalingType> = UpscalingType.entries
    val upscalingQualities: List<UpscalingQuality> = UpscalingQuality.entries
}

@Serializable
data class SaveConfigData(
    val name: String,
    val graphicsPreset: GraphicsPreset,
    val rtxEnabled: Boolean,
    val frameGenEnabled: Boolean,
    val upscaling: UpscalingSettings
)
