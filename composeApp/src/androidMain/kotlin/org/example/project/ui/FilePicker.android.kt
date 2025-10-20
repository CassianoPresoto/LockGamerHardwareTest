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
