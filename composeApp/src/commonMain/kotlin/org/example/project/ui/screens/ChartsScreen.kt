package org.example.project.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.example.project.data.HardwareConfig
import org.example.project.util.formatNumber
import kotlin.math.abs
import kotlin.math.ceil

private data class DataPoint(val x: Double, val y: Double)

private data class ChartLineData(
    val label: String,
    val color: Color,
    val points: List<DataPoint>,
    val avgFps: Double,
    val lowFps1Percent: Double
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
    var selectedGame by remember { mutableStateOf<String?>(null) }
    var selectedGpu by remember { mutableStateOf<String?>(null) }
    var selectedCpu by remember { mutableStateOf<String?>(null) }
    var selectedResolution by remember { mutableStateOf<String?>(null) }
    var show1PercentLow by remember { mutableStateOf(false) }
    
    val gameGroups = configs.groupBy { it.rawData.info.gameName.ifBlank { "Jogo desconhecido" } }
    val allGames = gameGroups.keys.toList().sorted()
    val allGpus = configs.map { it.systemInfo.gpu }.distinct().sorted()
    val allCpus = configs.map { it.systemInfo.processor }.distinct().sorted()
    val allResolutions = configs.mapNotNull { it.systemInfo.resolutionInfo }.distinct().sorted()
    
    // Apply filters
    val filteredConfigs = configs.filter { config ->
        (selectedGame == null || config.rawData.info.gameName.ifBlank { "Jogo desconhecido" } == selectedGame) &&
        (selectedGpu == null || config.systemInfo.gpu == selectedGpu) &&
        (selectedCpu == null || config.systemInfo.processor == selectedCpu) &&
        (selectedResolution == null || config.systemInfo.resolutionInfo == selectedResolution)
    }
    
    val filteredGameGroups = filteredConfigs.groupBy { it.rawData.info.gameName.ifBlank { "Jogo desconhecido" } }
    val nonEmpty = filteredConfigs.filter { it.performanceStats.avgFps > 0 }
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
            item {
                FilterSection(
                    allGames = allGames,
                    allGpus = allGpus,
                    allCpus = allCpus,
                    allResolutions = allResolutions,
                    selectedGame = selectedGame,
                    selectedGpu = selectedGpu,
                    selectedCpu = selectedCpu,
                    selectedResolution = selectedResolution,
                    show1PercentLow = show1PercentLow,
                    onGameSelected = { selectedGame = it },
                    onGpuSelected = { selectedGpu = it },
                    onCpuSelected = { selectedCpu = it },
                    onResolutionSelected = { selectedResolution = it },
                    onToggle1PercentLow = { show1PercentLow = it },
                    onClearFilters = {
                        selectedGame = null
                        selectedGpu = null
                        selectedCpu = null
                        selectedResolution = null
                    }
                )
            }
            
            if (filteredConfigs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Nenhum resultado encontrado com os filtros selecionados",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredGameGroups.entries.toList(), key = { it.key }) { (gameName, gameConfigs) ->
                    GameComparisonCard(
                        gameName = gameName,
                        configs = gameConfigs,
                        maxAvg = maxAvg,
                        show1PercentLow = show1PercentLow
                    )
                }
            }
        }
    }
}

@Composable
private fun GameComparisonCard(
    gameName: String,
    configs: List<HardwareConfig>,
    maxAvg: Double,
    show1PercentLow: Boolean
) {
    var expanded by remember { mutableStateOf(true) }
    var showExportDialog by remember { mutableStateOf(false) }
    
    val chartLines = configs.mapIndexedNotNull { index, config ->
        val points = extractFpsSeries(config)
        if (points.size < 2) return@mapIndexedNotNull null
        ChartLineData(
            label = config.name,
            color = chartPalette[index % chartPalette.size],
            points = points,
            avgFps = config.performanceStats.avgFps,
            lowFps1Percent = config.performanceStats.percentile1
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = gameName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Exportar gráficos",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Recolher" else "Expandir"
                        )
                    }
                }
            }
            
            if (showExportDialog) {
                ExportChartDialog(
                    gameName = gameName,
                    configs = configs,
                    onDismiss = { showExportDialog = false },
                    onExport = { format ->
                        // Export functionality will be platform-specific
                        exportChartData(gameName, configs, format)
                        showExportDialog = false
                    }
                )
            }

            if (expanded) {
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
                            LegendRow(line = line, show1PercentLow = show1PercentLow)
                        }
                    }
                }

                if (configs.size >= 2) {
                    Text(
                        text = "Comparação de Performance",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    PerformanceBarChart(configs = configs, show1PercentLow = show1PercentLow)
                }

                configs.forEach { config ->
                    ConfigDetailCard(config = config, maxAvg = maxAvg)
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

    val animProgress = remember { Animatable(0f) }
    
    LaunchedEffect(chartLines) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        )
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
                val animatedPointCount = (line.points.size * animProgress.value).toInt().coerceAtLeast(1)
                val visiblePoints = line.points.take(animatedPointCount)
                
                visiblePoints.forEachIndexed { index, point ->
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
private fun PerformanceBarChart(configs: List<HardwareConfig>, show1PercentLow: Boolean) {
    val maxFps = configs.maxOfOrNull { 
        if (show1PercentLow) it.performanceStats.percentile1 else it.performanceStats.avgFps 
    } ?: 0.0
    configs.minOfOrNull {
        if (show1PercentLow) it.performanceStats.percentile1 else it.performanceStats.avgFps 
    } ?: 0.0
    
    val animProgress = remember { Animatable(0f) }
    
    LaunchedEffect(configs) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        configs.forEachIndexed { index, config ->
            val fps = if (show1PercentLow) config.performanceStats.percentile1 else config.performanceStats.avgFps
            val percentage = if (maxFps > 0) ((fps / maxFps) * 100).toFloat() else 0f
            val diffFromMax = ((fps - maxFps) / maxFps * 100)
            val color = chartPalette[index % chartPalette.size]

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Text(
                            text = config.name,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "${formatNumber(fps, 1)} FPS",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val barWidth = size.width * (percentage / 100f) * animProgress.value
                        drawRoundRect(
                            color = color.copy(alpha = 0.2f),
                            size = Size(size.width, size.height),
                            cornerRadius = CornerRadius(8.dp.toPx())
                        )
                        drawRoundRect(
                            color = color,
                            size = Size(barWidth, size.height),
                            cornerRadius = CornerRadius(8.dp.toPx())
                        )
                    }
                    
                    if (abs(diffFromMax) > 0.1) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 8.dp)
                        ) {
                            Text(
                                text = if (diffFromMax < 0) "${formatNumber(diffFromMax, 1)}%" else "Ref",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (diffFromMax < 0) 
                                    MaterialTheme.colorScheme.error 
                                else 
                                    MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfigDetailCard(config: HardwareConfig, maxAvg: Double) {
    val value = config.performanceStats.avgFps
    val progress = if (maxAvg > 0) (value / maxAvg).toFloat().coerceIn(0f, 1f) else 0f
    
    val animProgress = remember { Animatable(0f) }
    
    LaunchedEffect(config) {
        animProgress.animateTo(
            targetValue = progress,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = config.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ConfigChip(
                    label = config.graphicsPreset.label,
                    color = MaterialTheme.colorScheme.primary
                )
                if (config.rtxEnabled) {
                    ConfigChip(
                        label = "RTX",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                if (config.frameGenEnabled) {
                    ConfigChip(
                        label = "Frame Gen",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                if (config.upscaling.type.label != "OFF") {
                    ConfigChip(
                        label = config.upscaling.type.label,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "FPS Médio: ${formatNumber(value, 2)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Min: ${formatNumber(config.performanceStats.minFps, 1)} | Max: ${formatNumber(config.performanceStats.maxFps, 1)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            LinearProgressIndicator(
                progress = { animProgress.value },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ConfigChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FilterSection(
    allGames: List<String>,
    allGpus: List<String>,
    allCpus: List<String>,
    allResolutions: List<String>,
    selectedGame: String?,
    selectedGpu: String?,
    selectedCpu: String?,
    selectedResolution: String?,
    show1PercentLow: Boolean,
    onGameSelected: (String?) -> Unit,
    onGpuSelected: (String?) -> Unit,
    onCpuSelected: (String?) -> Unit,
    onResolutionSelected: (String?) -> Unit,
    onToggle1PercentLow: (Boolean) -> Unit,
    onClearFilters: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val hasActiveFilters = selectedGame != null || selectedGpu != null || selectedCpu != null || selectedResolution != null
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasActiveFilters) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filtros",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (hasActiveFilters) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = listOf(selectedGame, selectedGpu, selectedCpu, selectedResolution).count { it != null }.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (hasActiveFilters) {
                        Text(
                            text = "Limpar",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { onClearFilters() }
                                .padding(8.dp)
                        )
                    }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Recolher" else "Expandir"
                        )
                    }
                }
            }
            
            if (expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Exibir 1% Low FPS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Switch(
                            checked = show1PercentLow,
                            onCheckedChange = onToggle1PercentLow
                        )
                    }
                    
                    if (allGames.size > 1) {
                        Text(
                            text = "Jogo/Aplicação",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                label = "Todos",
                                selected = selectedGame == null,
                                onClick = { onGameSelected(null) }
                            )
                            allGames.take(3).forEach { game ->
                                FilterChip(
                                    label = game.take(20),
                                    selected = selectedGame == game,
                                    onClick = { onGameSelected(if (selectedGame == game) null else game) }
                                )
                            }
                        }
                    }
                    
                    if (allGpus.size > 1) {
                        Text(
                            text = "GPU",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                label = "Todas",
                                selected = selectedGpu == null,
                                onClick = { onGpuSelected(null) }
                            )
                            allGpus.take(3).forEach { gpu ->
                                FilterChip(
                                    label = gpu.take(20),
                                    selected = selectedGpu == gpu,
                                    onClick = { onGpuSelected(if (selectedGpu == gpu) null else gpu) }
                                )
                            }
                        }
                    }
                    
                    if (allCpus.size > 1) {
                        Text(
                            text = "CPU",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                label = "Todas",
                                selected = selectedCpu == null,
                                onClick = { onCpuSelected(null) }
                            )
                            allCpus.take(3).forEach { cpu ->
                                FilterChip(
                                    label = cpu.take(20),
                                    selected = selectedCpu == cpu,
                                    onClick = { onCpuSelected(if (selectedCpu == cpu) null else cpu) }
                                )
                            }
                        }
                    }
                    
                    if (allResolutions.isNotEmpty()) {
                        Text(
                            text = "Resolução",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                label = "Todas",
                                selected = selectedResolution == null,
                                onClick = { onResolutionSelected(null) }
                            )
                            allResolutions.take(4).forEach { resolution ->
                                FilterChip(
                                    label = resolution,
                                    selected = selectedResolution == resolution,
                                    onClick = { onResolutionSelected(if (selectedResolution == resolution) null else resolution) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .background(
                if (selected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) 
                MaterialTheme.colorScheme.onPrimary 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun ExportChartDialog(
    gameName: String,
    configs: List<HardwareConfig>,
    onDismiss: () -> Unit,
    onExport: (ExportFormat) -> Unit
) {
    var selectedFormat by remember { mutableStateOf(ExportFormat.CSV) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Exportar Dados de $gameName")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Selecione o formato de exportação:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                ExportFormat.entries.forEach { format ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedFormat = format }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedFormat == format,
                            onClick = { selectedFormat = format }
                        )
                        Column {
                            Text(
                                text = format.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = format.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Text(
                    text = "${configs.size} configurações serão exportadas",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onExport(selectedFormat) }) {
                Text("Exportar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private enum class ExportFormat(val displayName: String, val description: String, val extension: String) {
    CSV("CSV", "Dados tabulares para análise", "csv"),
    JSON("JSON", "Dados estruturados completos", "json"),
    TXT("TXT", "Relatório de texto simples", "txt")
}

private fun exportChartData(gameName: String, configs: List<HardwareConfig>, format: ExportFormat) {
    val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
    val fileName = "${gameName.replace(" ", "_")}_${timestamp}.${format.extension}"
    
    val content = when (format) {
        ExportFormat.CSV -> generateCSV(configs)
        ExportFormat.JSON -> generateJSON(configs)
        ExportFormat.TXT -> generateTXT(gameName, configs)
    }
    
    // Platform-specific file saving will be handled here
    println("Exporting to $fileName")
    println(content)
    // TODO: Implement platform-specific file saving
}

private fun generateCSV(configs: List<HardwareConfig>): String {
    val header = "Config Name,GPU,CPU,Resolution,Graphics Preset,RTX,Frame Gen,Upscaling,Avg FPS,Min FPS,Max FPS,1% Low,5% Low,99% High"
    val rows = configs.map { config ->
        listOf(
            config.name,
            config.systemInfo.gpu,
            config.systemInfo.processor,
            config.systemInfo.resolutionInfo ?: "N/A",
            config.graphicsPreset.label,
            if (config.rtxEnabled) "Yes" else "No",
            if (config.frameGenEnabled) "Yes" else "No",
            config.upscaling.type.label,
            formatNumber(config.performanceStats.avgFps, 2),
            formatNumber(config.performanceStats.minFps, 2),
            formatNumber(config.performanceStats.maxFps, 2),
            formatNumber(config.performanceStats.percentile1, 2),
            formatNumber(config.performanceStats.percentile5, 2),
            formatNumber(config.performanceStats.percentile99, 2)
        ).joinToString(",")
    }
    return (listOf(header) + rows).joinToString("\n")
}

private fun generateJSON(configs: List<HardwareConfig>): String {
    // Simple JSON generation without kotlinx.serialization to avoid circular dependencies
    val jsonObjects = configs.map { config ->
        """
        {
            "name": "${config.name}",
            "gpu": "${config.systemInfo.gpu}",
            "cpu": "${config.systemInfo.processor}",
            "resolution": "${config.systemInfo.resolutionInfo ?: "N/A"}",
            "graphicsPreset": "${config.graphicsPreset.label}",
            "rtxEnabled": ${config.rtxEnabled},
            "frameGenEnabled": ${config.frameGenEnabled},
            "upscaling": "${config.upscaling.type.label}",
            "performance": {
                "avgFps": ${config.performanceStats.avgFps},
                "minFps": ${config.performanceStats.minFps},
                "maxFps": ${config.performanceStats.maxFps},
                "percentile1": ${config.performanceStats.percentile1},
                "percentile5": ${config.performanceStats.percentile5},
                "percentile99": ${config.performanceStats.percentile99}
            }
        }
        """.trimIndent()
    }
    return "[\n${jsonObjects.joinToString(",\n")}\n]"
}

private fun generateTXT(gameName: String, configs: List<HardwareConfig>): String {
    val lines = mutableListOf<String>()
    lines.add("=".repeat(80))
    lines.add("RELATÓRIO DE PERFORMANCE - $gameName")
    lines.add("=".repeat(80))
    lines.add("")
    
    configs.forEachIndexed { index, config ->
        lines.add("\n[${index + 1}] ${config.name}")
        lines.add("-".repeat(80))
        lines.add("GPU: ${config.systemInfo.gpu}")
        lines.add("CPU: ${config.systemInfo.processor}")
        lines.add("Resolução: ${config.systemInfo.resolutionInfo ?: "N/A"}")
        lines.add("Preset Gráfico: ${config.graphicsPreset.label}")
        lines.add("RTX: ${if (config.rtxEnabled) "Ativado" else "Desativado"}")
        lines.add("Frame Generation: ${if (config.frameGenEnabled) "Ativado" else "Desativado"}")
        lines.add("Upscaling: ${config.upscaling.type.label}")
        lines.add("")
        lines.add("PERFORMANCE:")
        lines.add("  FPS Médio: ${formatNumber(config.performanceStats.avgFps, 2)}")
        lines.add("  FPS Mínimo: ${formatNumber(config.performanceStats.minFps, 2)}")
        lines.add("  FPS Máximo: ${formatNumber(config.performanceStats.maxFps, 2)}")
        lines.add("  1% Low: ${formatNumber(config.performanceStats.percentile1, 2)}")
        lines.add("  5% Low: ${formatNumber(config.performanceStats.percentile5, 2)}")
        lines.add("  99% High: ${formatNumber(config.performanceStats.percentile99, 2)}")
    }
    
    lines.add("\n" + "=".repeat(80))
    lines.add("Relatório gerado em: ${kotlinx.datetime.Clock.System.now()}")
    lines.add("=".repeat(80))
    
    return lines.joinToString("\n")
}

@Composable
private fun LegendRow(line: ChartLineData, show1PercentLow: Boolean) {
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
            if (show1PercentLow) {
                Text(
                    text = "Médio: ${formatNumber(line.avgFps, 2)} FPS | 1% Low: ${formatNumber(line.lowFps1Percent, 2)} FPS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Médio: ${formatNumber(line.avgFps, 2)} FPS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
