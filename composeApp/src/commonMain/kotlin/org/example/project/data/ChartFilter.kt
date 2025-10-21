package org.example.project.data

/**
 * Filtros para geração de gráficos comparativos
 */
data class ChartFilter(
    val gameName: String? = null,
    val gpu: String? = null,
    val processor: String? = null,
    val graphicsPreset: GraphicsPreset? = null,
    val upscalingType: UpscalingType? = null,
    val compareBy: CompareBy = CompareBy.PRESET
)

/**
 * Dimensão de comparação para gráficos
 */
enum class CompareBy(val label: String) {
    PRESET("Preset Gráfico"),
    UPSCALING("Upscaling"),
    GPU("GPU"),
    CPU("CPU"),
    GAME("Jogo")
}

/**
 * Resultado agregado para gráficos
 */
data class ChartDataPoint(
    val label: String,
    val avgFps: Double,
    val avg1Low: Double,
    val testCount: Long
)
