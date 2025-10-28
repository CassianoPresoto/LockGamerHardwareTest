package org.example.project.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
actual fun FilePicker(
    show: Boolean,
    fileExtensions: List<String>,
    onFileSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // TODO: Implementar UIDocumentPicker para iOS.
    LaunchedEffect(show) {
        if (show) {
            onDismiss()
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
    // TODO: Implementar UIDocumentPicker com múltipla seleção para iOS.
    LaunchedEffect(show) {
        if (show) {
            onDismiss()
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
    // TODO: Implementar UIDocumentPicker com seleção de pasta para iOS.
    LaunchedEffect(show) {
        if (show) {
            onDismiss()
        }
    }
}
