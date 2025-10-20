package org.example.project.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.data.HardwareConfig
import org.example.project.util.formatNumber

@Composable
fun ComparisonScreen(
    configs: List<HardwareConfig>,
    modifier: Modifier = Modifier
) {
    if (configs.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = "Selecione configurações para comparar",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Comparação de Configurações",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Performance Comparison
            ComparisonTable(
                title = "Performance",
                configs = configs,
                rows = listOf(
                    "FPS Médio" to { formatNumber(it.performanceStats.avgFps, 2) },
                    "FPS Mínimo" to { formatNumber(it.performanceStats.minFps, 2) },
                    "FPS Máximo" to { formatNumber(it.performanceStats.maxFps, 2) },
                    "1% Low" to { formatNumber(it.performanceStats.percentile1, 2) },
                    "5% Low" to { formatNumber(it.performanceStats.percentile5, 2) },
                    "95% High" to { formatNumber(it.performanceStats.percentile95, 2) },
                    "99% High" to { formatNumber(it.performanceStats.percentile99, 2) },
                    "Frame Time Médio" to { formatNumber(it.performanceStats.frameTimeAvg, 2) + " ms" },
                    "Total Frames" to { it.performanceStats.totalFrames.toString() },
                    "Duração" to { formatNumber(it.performanceStats.duration, 2) + " s" }
                )
            )
            
            // System Info Comparison
            ComparisonTable(
                title = "Hardware",
                configs = configs,
                rows = listOf(
                    "CPU" to { it.systemInfo.Processor },
                    "GPU" to { it.systemInfo.GPU },
                    "RAM" to { it.systemInfo.SystemRam },
                    "Driver GPU" to { it.systemInfo.GPUDriverVersion },
                    "Resizable BAR" to { it.systemInfo.ResizableBar },
                    "Game Mode" to { it.systemInfo.WinGameMode },
                    "HAGS" to { it.systemInfo.HAGS }
                )
            )
        }
    }
}

@Composable
private fun ComparisonTable(
    title: String,
    configs: List<HardwareConfig>,
    rows: List<Pair<String, (HardwareConfig) -> String>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                // Labels column
                Column(
                    modifier = Modifier.width(150.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.height(40.dp)
                    )
                    rows.forEach { (label, _) ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }
                
                // Config columns
                configs.forEach { config ->
                    Column(
                        modifier = Modifier
                            .width(200.dp)
                            .padding(start = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = config.name,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.height(40.dp)
                        )
                        rows.forEach { (_, getValue) ->
                            Text(
                                text = getValue(config),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.height(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
