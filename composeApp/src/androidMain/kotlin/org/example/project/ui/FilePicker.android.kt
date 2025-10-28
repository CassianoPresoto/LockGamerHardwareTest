package org.example.project.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun FilePicker(
    show: Boolean,
    fileExtensions: List<String>,
    onFileSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val content = context.contentResolver.openInputStream(uri)?.use {
                    it.bufferedReader().readText()
                }
                if (content != null) {
                    onFileSelected(content)
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
    
    LaunchedEffect(show) {
        if (show) {
            launcher.launch("application/json")
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
    val context = LocalContext.current
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            try {
                val contents = uris.mapNotNull { uri ->
                    context.contentResolver.openInputStream(uri)?.use {
                        it.bufferedReader().readText()
                    }
                }
                if (contents.isNotEmpty()) {
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
    
    LaunchedEffect(show) {
        if (show) {
            launcher.launch("application/json")
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
    // Android doesn't have direct folder picker with file reading
    // Fallback to multi-file picker
    MultiFilePicker(show, fileExtensions, onFilesSelected, onDismiss)
}
