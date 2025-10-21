package org.example.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.data.CapFrameXData
import org.example.project.data.GraphicsPreset
import org.example.project.data.PerformanceStats
import org.example.project.data.SaveConfigData
import org.example.project.data.SaveConfigOptions
import org.example.project.data.UpscalingQuality
import org.example.project.data.UpscalingSettings
import org.example.project.data.UpscalingType
import org.example.project.ui.components.PerformanceCard
import org.example.project.ui.components.SystemInfoCard


@ExperimentalMaterial3Api
@Composable
fun SaveConfigDialog(
    data: CapFrameXData,
    stats: PerformanceStats,
    onSave: (SaveConfigData) -> Unit,
    onDismiss: () -> Unit
) {
    var configName by remember { mutableStateOf("") }
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
                    Text("RTX")
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
                    Text("Frame Generation")
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
                
                HorizontalDivider()
                
                SystemInfoCard(info = data.info)
                PerformanceCard(stats = stats)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        SaveConfigData(
                            name = configName,
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
                enabled = configName.isNotBlank() && (!upscalingType.supportsQuality || upscalingQuality != null)
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
