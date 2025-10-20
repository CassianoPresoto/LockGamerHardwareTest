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
            
            InfoRow("Jogo", info.GameName)
            InfoRow("Processo", info.ProcessName)
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            InfoRow("CPU", info.Processor)
            InfoRow("GPU", info.GPU)
            InfoRow("RAM", info.SystemRam)
            InfoRow("Placa-Mãe", info.Motherboard)
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            InfoRow("Sistema Operacional", info.OS)
            InfoRow("Driver GPU", info.GPUDriverVersion)
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            InfoRow("Resizable BAR", info.ResizableBar)
            InfoRow("Game Mode", info.WinGameMode)
            InfoRow("HAGS", info.HAGS)
            InfoRow("Modo de Apresentação", info.PresentationMode)
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
