package net.canvoki.shared.storage

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * Interface that abstracts saving content to a user-chosen location
 * via the system's file picker (Storage Access Framework).
 */
interface SaveFilePicker {
    /**
     * Launches the system file picker to save content.
     *
     * @param suggestedName the initial filename suggestion (e.g. "report.txt")
     * @param content the binary content to write
     */
    fun save(
        suggestedName: String,
        content: ByteArray,
    )
}

/**
 * Returns a [SaveFilePicker] that launches the system "Save As" file picker.
 *
 * This uses the Storage Access Framework under the hood but abstracts away
 * all implementation details. Works on Android 4.4 (API 19) and above.
 *
 * Example:
 * ```kotlin
 * val saver = rememberSaveFilePicker("text/plain")
 * Button(onClick = {
 *     saver.save("crash-report.txt", report.fullText.toByteArray())
 * }) { Text("Save as...") }
 * ```
 */
@Composable
fun rememberSaveFilePicker(mimeType: String = "application/octet-stream"): SaveFilePicker {
    val context = LocalContext.current
    var pendingContent by remember { mutableStateOf<ByteArray?>(null) }

    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument(mimeType),
        ) { uri ->
            uri?.let {
                pendingContent?.let { content ->
                    context.contentResolver.openOutputStream(it)?.use { stream ->
                        stream.write(content)
                    }
                }
            }
            pendingContent = null // cleanup
        }

    return remember(launcher) {
        object : SaveFilePicker {
            override fun save(
                suggestedName: String,
                content: ByteArray,
            ) {
                pendingContent = content
                launcher.launch(suggestedName)
            }
        }
    }
}
