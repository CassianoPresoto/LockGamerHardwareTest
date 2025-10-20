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
    // Por enquanto, apenas descarta quando solicitado para não quebrar a compilação.
    LaunchedEffect(show) {
        if (show) {
            onDismiss()
        }
    }
}
