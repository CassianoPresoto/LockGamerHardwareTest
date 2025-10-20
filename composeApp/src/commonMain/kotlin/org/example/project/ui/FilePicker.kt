package org.example.project.ui

import androidx.compose.runtime.Composable

@Composable
expect fun FilePicker(
    show: Boolean,
    fileExtensions: List<String> = listOf("json"),
    onFileSelected: (String) -> Unit,
    onDismiss: () -> Unit
)
