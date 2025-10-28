package org.example.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.data.GraphicsPreset
import org.example.project.data.SaveConfigData
import org.example.project.data.SaveConfigOptions
import org.example.project.data.UpscalingQuality
import org.example.project.data.UpscalingSettings
import org.example.project.data.UpscalingType

@ExperimentalMaterial3Api
@Composable
fun BatchConfigDialog(
    fileCount: Int,
    errors: List<String>?,
    onSave: (SaveConfigData) -> Unit,
    onDismiss: () -> Unit
) {
    var graphicsPreset by remember { mutableStateOf(GraphicsPreset.MAXIMUM) }
    var rtxEnabled by remember { mutableStateOf(false) }
    var frameGenEnabled by remember { mutableStateOf(false) }
    var upscalingType by remember { mutableStateOf(UpscalingType.OFF) }
    var upscalingQuality by remember { mutableStateOf<UpscalingQuality?>(null) }
    
    var expandedPreset by remember { mutableStateOf(false) }
    var expandedUpscaling by remember { mutableStateOf(false) }
    var expandedQuality by remember { mutableStateOf(false) }
    
    val presetOptions = SaveConfigOptions.presets
    val upscalingOptions = SaveConfigOptions.upscalingTypes
    val qualityOptions = SaveConfigOptions.upscalingQualities
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Configuração para Importação em Lote")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📦 $fileCount arquivo(s) serão importados",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "As configurações abaixo serão aplicadas a todos os arquivos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        if (errors != null && errors.isNotEmpty()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Text(
                                text = "⚠️ ${errors.size} arquivo(s) com erro foram ignorados",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                
                Text(
                    text = "Configurações Gráficas",
                    style = MaterialTheme.typography.titleSmall
                )
                
                // Preset Graphic
                ExposedDropdownMenuBox(
                    expanded = expandedPreset,
                    onExpandedChange = { expandedPreset = it }
                ) {
                    OutlinedTextField(
                        value = graphicsPreset.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Preset Gráfico") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPreset) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPreset,
                        onDismissRequest = { expandedPreset = false }
                    ) {
                        presetOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = {
                                    graphicsPreset = option
                                    expandedPreset = false
                                }
                            )
                        }
                    }
                }
                
                // RTX Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("RTX (Ray Tracing)")
                        Text(
                            text = "Ativar ray tracing",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = rtxEnabled,
                        onCheckedChange = { rtxEnabled = it }
                    )
                }
                
                // Frame Generation Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Frame Generation")
                        Text(
                            text = "DLSS 3 / FSR 3",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = frameGenEnabled,
                        onCheckedChange = { frameGenEnabled = it }
                    )
                }
                
                // Upscaling Type
                ExposedDropdownMenuBox(
                    expanded = expandedUpscaling,
                    onExpandedChange = { expandedUpscaling = it }
                ) {
                    OutlinedTextField(
                        value = upscalingType.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de Upscaling") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUpscaling) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedUpscaling,
                        onDismissRequest = { expandedUpscaling = false }
                    ) {
                        upscalingOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = {
                                    upscalingType = option
                                    upscalingQuality = if (option.supportsQuality) {
                                        upscalingQuality ?: qualityOptions.last()
                                    } else {
                                        null
                                    }
                                    expandedUpscaling = false
                                }
                            )
                        }
                    }
                }
                
                if (upscalingType.supportsQuality) {
                    // Quality Upscaling
                    ExposedDropdownMenuBox(
                        expanded = expandedQuality,
                        onExpandedChange = { expandedQuality = it }
                    ) {
                        OutlinedTextField(
                            value = (upscalingQuality ?: qualityOptions.last()).label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Qualidade do Upscaling") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedQuality) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedQuality,
                            onDismissRequest = { expandedQuality = false }
                        ) {
                            qualityOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label) },
                                    onClick = {
                                        upscalingQuality = option
                                        expandedQuality = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        SaveConfigData(
                            name = "", // Will be auto-generated per file
                            graphicsPreset = graphicsPreset,
                            rtxEnabled = rtxEnabled,
                            frameGenEnabled = frameGenEnabled,
                            upscaling = UpscalingSettings(
                                type = upscalingType,
                                quality = if (upscalingType.supportsQuality) upscalingQuality else null
                            )
                        )
                    )
                },
                enabled = !upscalingType.supportsQuality || upscalingQuality != null
            ) {
                Text("Importar Todos")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
