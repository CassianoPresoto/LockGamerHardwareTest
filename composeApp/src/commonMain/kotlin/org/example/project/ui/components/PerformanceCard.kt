package org.example.project.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.data.PerformanceStats
import org.example.project.util.formatNumber

@Composable
fun PerformanceCard(stats: PerformanceStats, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Estatísticas de Performance",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            StatRow("FPS Médio", formatNumber(stats.avgFps, 2))
            StatRow("FPS Mínimo", formatNumber(stats.minFps, 2))
            StatRow("FPS Máximo", formatNumber(stats.maxFps, 2))
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            StatRow("1% Low", formatNumber(stats.percentile1, 2))
            StatRow("5% Low", formatNumber(stats.percentile5, 2))
            StatRow("95% High", formatNumber(stats.percentile95, 2))
            StatRow("99% High", formatNumber(stats.percentile99, 2))
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            StatRow("Frame Time Médio", formatNumber(stats.frameTimeAvg, 2) + " ms")
            StatRow("Frame Time Mín", formatNumber(stats.frameTimeMin, 2) + " ms")
            StatRow("Frame Time Máx", formatNumber(stats.frameTimeMax, 2) + " ms")
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            StatRow("Total de Frames", stats.totalFrames.toString())
            StatRow("Duração", formatNumber(stats.duration, 2) + " s")
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
