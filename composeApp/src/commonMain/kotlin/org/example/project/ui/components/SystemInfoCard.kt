package org.example.project.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.data.SystemInfo

@Composable
fun SystemInfoCard(info: SystemInfo, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Informações do Sistema",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            InfoRow("Jogo", info.gameName)
            InfoRow("Processo", info.processName)
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            InfoRow("CPU", info.processor)
            InfoRow("GPU", info.gpu)
            InfoRow("RAM", info.systemRam)
            InfoRow("Placa-Mãe", info.motherboard)
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            InfoRow("Sistema Operacional", info.os)
            InfoRow("Driver GPU", info.gpuDriverVersion)
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            InfoRow("Resizable BAR", info.resizableBar)
            InfoRow("Game Mode", info.winGameMode)
            InfoRow("HAGS", info.hags)
            InfoRow("Modo de Apresentação", info.presentationMode)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
