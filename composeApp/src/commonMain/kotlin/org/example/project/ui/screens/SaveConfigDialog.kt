package org.example.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.data.CapFrameXData
import org.example.project.data.PerformanceStats
import org.example.project.ui.components.PerformanceCard
import org.example.project.ui.components.SystemInfoCard

@Composable
fun SaveConfigDialog(
    data: CapFrameXData,
    stats: PerformanceStats,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var configName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Salvar Configuração")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = configName,
                    onValueChange = { configName = it },
                    label = { Text("Nome da Configuração") },
                    placeholder = { Text("Ex: RTX 4090 - Ultra Settings") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                SystemInfoCard(info = data.info)
                PerformanceCard(stats = stats)
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(configName) },
                enabled = configName.isNotBlank()
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
