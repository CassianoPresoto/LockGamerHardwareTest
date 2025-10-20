package org.example.project.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader
import org.w3c.files.get

@Composable
actual fun FilePicker(
    show: Boolean,
    fileExtensions: List<String>,
    onFileSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    LaunchedEffect(show) {
        if (show) {
            val input = document.createElement("input") as HTMLInputElement
            input.type = "file"
            input.accept = (fileExtensions.joinToString(",") { ".${it}" }) + ",application/json"

            input.onchange = {
                val file = input.files?.get(0)
                if (file != null) {
                    val reader = FileReader()
                    reader.onload = {
                        val content = reader.result as? String
                        if (content != null) onFileSelected(content) else onDismiss()
                    }
                    reader.onerror = { onDismiss() }
                    reader.readAsText(file)
                } else {
                    onDismiss()
                }
            }

            input.click()
        }
    }
}
