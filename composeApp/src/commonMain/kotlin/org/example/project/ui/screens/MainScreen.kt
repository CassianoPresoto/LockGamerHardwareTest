package org.example.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.ui.FilePicker
import org.example.project.viewmodel.HardwareViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: HardwareViewModel = koinInject()) {
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
                    icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Configurações") },
                    label = { Text("Configurações") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Lista") },
                    label = { Text("Lista") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
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
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.InsertChart, contentDescription = "Gráficos") },
                    label = { Text("Gráficos") },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 }
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
                1 -> AllTestsScreen(
                    configs = uiState.savedConfigs,
                    selectedConfigs = uiState.selectedForComparison,
                    onConfigClick = { viewModel.selectConfigForComparison(it) }
                )
                2 -> ComparisonScreen(
                    configs = uiState.selectedForComparison
                )
                3 -> ChartsScreen(
                    configs = uiState.savedConfigs
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
                    onSave = { configData -> viewModel.saveCurrentConfig(configData) },
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
