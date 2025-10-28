package org.example.project.ui

import androidx.compose.runtime.Composable

@Composable
expect fun FilePicker(
    show: Boolean,
    fileExtensions: List<String> = listOf("json"),
    onFileSelected: (String) -> Unit,
    onDismiss: () -> Unit
)

@Composable
expect fun MultiFilePicker(
    show: Boolean,
    fileExtensions: List<String> = listOf("json"),
    onFilesSelected: (List<String>) -> Unit,
    onDismiss: () -> Unit
)

@Composable
expect fun FolderPicker(
    show: Boolean,
    fileExtensions: List<String> = listOf("json"),
    onFilesSelected: (List<String>) -> Unit,
    onDismiss: () -> Unit
)
