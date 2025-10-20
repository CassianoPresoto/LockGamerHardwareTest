package org.example.project.ui

import androidx.compose.runtime.*
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

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
