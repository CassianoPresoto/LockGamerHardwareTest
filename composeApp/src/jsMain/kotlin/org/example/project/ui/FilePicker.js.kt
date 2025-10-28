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

@Composable
actual fun MultiFilePicker(
    show: Boolean,
    fileExtensions: List<String>,
    onFilesSelected: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    LaunchedEffect(show) {
        if (show) {
            val input = document.createElement("input") as HTMLInputElement
            input.type = "file"
            input.multiple = true
            input.accept = (fileExtensions.joinToString(",") { ".${it}" }) + ",application/json"

            input.onchange = {
                val files = input.files
                if (files != null && files.length > 0) {
                    val contents = mutableListOf<String>()
                    var filesRead = 0
                    
                    for (i in 0 until files.length) {
                        val file = files[i]
                        if (file != null) {
                            val reader = FileReader()
                            reader.onload = {
                                val content = reader.result as? String
                                if (content != null) {
                                    contents.add(content)
                                }
                                filesRead++
                                if (filesRead == files.length) {
                                    if (contents.isNotEmpty()) {
                                        onFilesSelected(contents)
                                    } else {
                                        onDismiss()
                                    }
                                }
                            }
                            reader.onerror = {
                                filesRead++
                                if (filesRead == files.length) {
                                    if (contents.isNotEmpty()) {
                                        onFilesSelected(contents)
                                    } else {
                                        onDismiss()
                                    }
                                }
                            }
                            reader.readAsText(file)
                        }
                    }
                } else {
                    onDismiss()
                }
            }

            input.click()
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
    // Web doesn't support folder picker with file reading
    // Fallback to multi-file picker
    MultiFilePicker(show, fileExtensions, onFilesSelected, onDismiss)
}
