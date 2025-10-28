package org.example.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.ui.FilePicker
import org.example.project.ui.FolderPicker
import org.example.project.ui.MultiFilePicker
import org.example.project.viewmodel.HardwareViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: HardwareViewModel = remember { HardwareViewModel() }) {
    val uiState = viewModel.uiState
    var selectedTab by remember { mutableStateOf(0) }
    var showFilePicker by remember { mutableStateOf(false) }
    var showMultiFilePicker by remember { mutableStateOf(false) }
    var showFolderPicker by remember { mutableStateOf(false) }
    var showImportMenu by remember { mutableStateOf(false) }
    
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
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Configurações") },
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
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Gráficos") },
                    label = { Text("Gráficos") },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                    if (showImportMenu) {
                        SmallFloatingActionButton(
                            onClick = { 
                                showFolderPicker = true
                                showImportMenu = false
                            },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(Icons.Default.CreateNewFolder, contentDescription = "Importar Pasta")
                        }
                        
                        SmallFloatingActionButton(
                            onClick = { 
                                showMultiFilePicker = true
                                showImportMenu = false
                            },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Múltiplos Arquivos")
                        }
                        
                        SmallFloatingActionButton(
                            onClick = { 
                                showFilePicker = true
                                showImportMenu = false
                            },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Arquivo Único")
                        }
                    }
                    
                    FloatingActionButton(
                        onClick = { showImportMenu = !showImportMenu }
                    ) {
                        Icon(
                            if (showImportMenu) Icons.Default.MoreVert else Icons.Default.Add,
                            contentDescription = "Importar"
                        )
                    }
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
            
            // Loading indicator with progress
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (uiState.batchImportProgress != null) {
                                Text(
                                    text = "Importando arquivos...",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "${uiState.batchImportProgress.current} de ${uiState.batchImportProgress.total}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                LinearProgressIndicator(
                                    progress = { uiState.batchImportProgress.percentage },
                                    modifier = Modifier.width(200.dp)
                                )
                            } else {
                                CircularProgressIndicator()
                                Text(
                                    text = "Processando...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
            
            // Success message
            uiState.batchImportSuccess?.let { message ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    action = {
                        TextButton(onClick = { viewModel.clearBatchImportSuccess() }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(message)
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
            
            // Save dialog (single file)
            if (uiState.showSaveDialog && uiState.currentData != null && uiState.currentStats != null) {
                SaveConfigDialog(
                    data = uiState.currentData,
                    stats = uiState.currentStats,
                    onSave = { configData -> viewModel.saveCurrentConfig(configData) },
                    onDismiss = { viewModel.cancelSave() }
                )
            }
            
            // Batch config dialog (multiple files)
            if (uiState.showBatchConfigDialog && uiState.batchParsedData != null) {
                BatchConfigDialog(
                    fileCount = uiState.batchParsedData.size,
                    errors = uiState.batchImportErrors,
                    onSave = { configData -> viewModel.saveBatchConfigs(configData) },
                    onDismiss = { viewModel.cancelBatchImport() }
                )
            }
        }
    }
    
    // File pickers
    FilePicker(
        show = showFilePicker,
        fileExtensions = listOf("json"),
        onFileSelected = { content ->
            viewModel.parseJsonFile(content)
            showFilePicker = false
        },
        onDismiss = { showFilePicker = false }
    )
    
    MultiFilePicker(
        show = showMultiFilePicker,
        fileExtensions = listOf("json"),
        onFilesSelected = { contents ->
            viewModel.parseMultipleJsonFiles(contents)
            showMultiFilePicker = false
        },
        onDismiss = { showMultiFilePicker = false }
    )
    
    FolderPicker(
        show = showFolderPicker,
        fileExtensions = listOf("json"),
        onFilesSelected = { contents ->
            viewModel.parseMultipleJsonFiles(contents)
            showFolderPicker = false
        },
        onDismiss = { showFolderPicker = false }
    )
}
