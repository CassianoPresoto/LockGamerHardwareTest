package org.example.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.ui.FilePicker
import org.example.project.viewmodel.HardwareViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: HardwareViewModel = remember { HardwareViewModel() }) {
    val uiState = viewModel.uiState
    var selectedTab by remember { mutableStateOf(0) }
    var showFilePicker by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hardware Performance Comparator") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Configurações") },
                    label = { Text("Configurações") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = {
                        BadgedBox(
                            badge = {
                                if (uiState.selectedForComparison.isNotEmpty()) {
                                    Badge { Text(uiState.selectedForComparison.size.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.BarChart, contentDescription = "Comparação")
                        }
                    },
                    label = { Text("Comparação") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showFilePicker = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Importar JSON")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> ConfigListScreen(
                    configs = uiState.savedConfigs,
                    selectedConfigs = uiState.selectedForComparison,
                    onConfigClick = { viewModel.selectConfigForComparison(it) },
                    onDeleteConfig = { viewModel.deleteConfig(it) }
                )
                1 -> ComparisonScreen(
                    configs = uiState.selectedForComparison
                )
            }
            
            // Loading indicator
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            // Error snackbar
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(error)
                }
            }
            
            // Save dialog
            if (uiState.showSaveDialog && uiState.currentData != null && uiState.currentStats != null) {
                SaveConfigDialog(
                    data = uiState.currentData,
                    stats = uiState.currentStats,
                    onSave = { name -> viewModel.saveCurrentConfig(name) },
                    onDismiss = { viewModel.cancelSave() }
                )
            }
        }
    }
    
    // File picker
    FilePicker(
        show = showFilePicker,
        fileExtensions = listOf("json"),
        onFileSelected = { content ->
            viewModel.parseJsonFile(content)
            showFilePicker = false
        },
        onDismiss = { showFilePicker = false }
    )
}
