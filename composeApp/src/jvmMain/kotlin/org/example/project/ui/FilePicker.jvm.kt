package org.example.project.ui

import androidx.compose.runtime.*
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
actual fun FilePicker(
    show: Boolean,
    fileExtensions: List<String>,
    onFileSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    LaunchedEffect(show) {
        if (show) {
            val fileDialog = FileDialog(null as Frame?, "Selecione um arquivo JSON", FileDialog.LOAD)
            fileDialog.file = "*.json"
            fileDialog.isVisible = true
            
            val selectedFile = fileDialog.file
            val selectedDir = fileDialog.directory
            
            if (selectedFile != null && selectedDir != null) {
                val file = File(selectedDir, selectedFile)
                try {
                    val content = file.readText()
                    onFileSelected(content)
                } catch (e: Exception) {
                    e.printStackTrace()
                    onDismiss()
                }
            } else {
                onDismiss()
            }
        }
    }
}

@Composable
actual fun MultiFilePicker(
    show: Boolean,
    fileExtensions: List<String>,
    onFilesSelected: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    LaunchedEffect(show) {
        if (show) {
            val fileChooser = JFileChooser()
            fileChooser.isMultiSelectionEnabled = true
            fileChooser.dialogTitle = "Selecione múltiplos arquivos JSON"
            
            val filter = FileNameExtensionFilter(
                "Arquivos JSON (*.json)",
                *fileExtensions.toTypedArray()
            )
            fileChooser.fileFilter = filter
            
            val result = fileChooser.showOpenDialog(null)
            
            if (result == JFileChooser.APPROVE_OPTION) {
                val selectedFiles = fileChooser.selectedFiles
                try {
                    val contents = selectedFiles.map { it.readText() }
                    onFilesSelected(contents)
                } catch (e: Exception) {
                    e.printStackTrace()
                    onDismiss()
                }
            } else {
                onDismiss()
            }
        }
    }
}

@Composable
actual fun FolderPicker(
    show: Boolean,
    fileExtensions: List<String>,
    onFilesSelected: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    LaunchedEffect(show) {
        if (show) {
            val fileChooser = JFileChooser()
            fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            fileChooser.dialogTitle = "Selecione uma pasta com arquivos JSON"
            
            val result = fileChooser.showOpenDialog(null)
            
            if (result == JFileChooser.APPROVE_OPTION) {
                val selectedFolder = fileChooser.selectedFile
                try {
                    val jsonFiles = selectedFolder.listFiles { file ->
                        file.isFile && fileExtensions.any { ext -> 
                            file.name.endsWith(".$ext", ignoreCase = true) 
                        }
                    }
                    
                    if (jsonFiles != null && jsonFiles.isNotEmpty()) {
                        val contents = jsonFiles.map { it.readText() }
                        onFilesSelected(contents)
                    } else {
                        onDismiss()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    onDismiss()
                }
            } else {
                onDismiss()
            }
        }
    }
}
