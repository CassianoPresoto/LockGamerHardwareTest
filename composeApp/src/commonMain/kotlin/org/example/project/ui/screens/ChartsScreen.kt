package org.example.project.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import org.example.project.data.HardwareConfig
import org.example.project.util.formatNumber
import kotlin.math.ceil

private data class DataPoint(val x: Double, val y: Double)

private data class ChartLineData(
    val label: String,
    val color: Color,
    val points: List<DataPoint>,
    val avgFps: Double
)

private fun extractFpsSeries(config: HardwareConfig): List<DataPoint> {
    val run = config.rawData.runs.firstOrNull() ?: return emptyList()
    val captureData = run.captureData
    val times = captureData.timeInSeconds
    val frameTimes = captureData.msBetweenPresents
    if (times.isEmpty() || frameTimes.isEmpty()) return emptyList()

    val length = minOf(times.size, frameTimes.size)
    val points = mutableListOf<DataPoint>()
    for (index in 0 until length) {
        val time = times[index]
        val frameTime = frameTimes[index]
        if (frameTime > 0) {
            points.add(DataPoint(time, 1000.0 / frameTime))
        }
    }
    return points.downsample()
}

private fun List<DataPoint>.downsample(maxPoints: Int = 200): List<DataPoint> {
    if (size <= maxPoints) return this
    val step = ceil(size.toDouble() / maxPoints.toDouble()).toInt().coerceAtLeast(1)
    val sampled = filterIndexed { index, _ -> index % step == 0 }
    return if (sampled.isNotEmpty() && sampled.last() == last()) sampled else sampled + last()
}

private val chartPalette = listOf(
    Color(0xFF4CAF50),
    Color(0xFF2196F3),
    Color(0xFFFF9800),
    Color(0xFFE91E63),
    Color(0xFF9C27B0),
    Color(0xFF00BCD4)
)

@Composable
fun ChartsScreen(
    configs: List<HardwareConfig>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp)
) {
    val gameGroups = configs.groupBy { it.rawData.info.gameName.ifBlank { "Jogo desconhecido" } }
    val nonEmpty = configs.filter { it.performanceStats.avgFps > 0 }
    val maxAvg = nonEmpty.maxOfOrNull { it.performanceStats.avgFps } ?: 0.0

    if (configs.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Nenhum dado para gerar gráficos",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(gameGroups.entries.toList(), key = { it.key }) { (gameName, gameConfigs) ->
                GameComparisonCard(
                    gameName = gameName,
                    configs = gameConfigs,
                    maxAvg = maxAvg
                )
            }
        }
    }
}

@Composable
private fun GameComparisonCard(
    gameName: String,
    configs: List<HardwareConfig>,
    maxAvg: Double
) {
    val chartLines = configs.mapIndexedNotNull { index, config ->
        val points = extractFpsSeries(config)
        if (points.size < 2) return@mapIndexedNotNull null
        ChartLineData(
            label = config.name,
            color = chartPalette[index % chartPalette.size],
            points = points,
            avgFps = config.performanceStats.avgFps
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = gameName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (chartLines.isEmpty()) {
                Text(
                    text = "Sem dados de linha do tempo para este jogo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                PerformanceLineChart(chartLines = chartLines)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    chartLines.forEach { line ->
                        LegendRow(line)
                    }
                }
            }

            configs.forEach { config ->
                val value = config.performanceStats.avgFps
                val progress = if (maxAvg > 0) (value / maxAvg).toFloat().coerceIn(0f, 1f) else 0f
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = config.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "FPS Médio: ${formatNumber(value, 2)} (ref máx: ${formatNumber(maxAvg, 2)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PerformanceLineChart(chartLines: List<ChartLineData>) {
    val allPoints = chartLines.flatMap { it.points }
    val minX = allPoints.minOfOrNull { it.x } ?: 0.0
    val maxX = allPoints.maxOfOrNull { it.x } ?: minX
    val maxY = allPoints.maxOfOrNull { it.y } ?: 0.0

    if (maxX <= minX || maxY <= 0) {
        Text(
            text = "Gráfico indisponível: dados insuficientes.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val leftPadding = 16.dp
    val bottomPadding = 16.dp
    val axisColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val chartWidth = size.width - leftPadding.toPx()
            val chartHeight = size.height - bottomPadding.toPx()
            if (chartWidth <= 0f || chartHeight <= 0f) return@Canvas

            val origin = Offset(x = leftPadding.toPx(), y = chartHeight)

            // Axes
            drawLine(
                color = axisColor,
                start = Offset(origin.x, origin.y),
                end = Offset(origin.x, 0f),
                strokeWidth = 1f
            )
            drawLine(
                color = axisColor,
                start = Offset(origin.x, origin.y),
                end = Offset(size.width, origin.y),
                strokeWidth = 1f
            )

            chartLines.forEach { line ->
                val path = Path()
                line.points.forEachIndexed { index, point ->
                    val xRatio = ((point.x - minX) / (maxX - minX)).toFloat()
                    val yRatio = (point.y / maxY).toFloat()
                    val x = origin.x + (chartWidth * xRatio)
                    val y = (chartHeight * (1 - yRatio))
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = line.color,
                    style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "0 s",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${formatNumber(maxX - minX, 1)} s",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "Pico: ${formatNumber(maxY, 0)} FPS",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LegendRow(line: ChartLineData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .padding(0.dp)
                .background(line.color)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = line.label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Médio: ${formatNumber(line.avgFps, 2)} FPS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
