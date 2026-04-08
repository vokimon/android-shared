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
 * Interface that abstracts reading content from a user-chosen file
 * via the system's file picker (Storage Access Framework).
 */
interface OpenFilePicker {
    /**
     * Launches the system file picker to select and read a file.
     *
     * @param mimeTypes allowed MIME types (e.g. arrayOf("text/plain", "application/json"))
     * @param onResult callback invoked with the file content as [ByteArray], or null if cancelled
     */
    fun open(
        mimeTypes: Array<String> = arrayOf("*/*"),
        onResult: (ByteArray?) -> Unit,
    )
}

/**
 * Returns a [OpenFilePicker] that launches the system "Open File" picker.
 *
 * This uses the Storage Access Framework under the hood but abstracts away
 * all implementation details. Works on Android 4.4 (API 19) and above.
 *
 * Example:
 * ```kotlin
 * val opener = rememberOpenFilePicker()
 * Button(onClick = {
 *     opener.open(arrayOf("text/plain")) { content ->
 *         if (content != null) {
 *             val text = content.decodeToString()
 *             // process text
 *         }
 *     }
 * }) { Text("Open report") }
 * ```
 */
@Composable
fun rememberOpenFilePicker(): OpenFilePicker {
    val context = LocalContext.current
    var pendingCallback by remember { mutableStateOf<((ByteArray?) -> Unit)?>(null) }

    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
        ) { uri ->
            val callback = pendingCallback
            pendingCallback = null // cleanup

            if (uri != null) {
                try {
                    val content = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    callback?.invoke(content)
                } catch (e: Exception) {
                    callback?.invoke(null)
                }
            } else {
                callback?.invoke(null)
            }
        }

    return remember(launcher) {
        object : OpenFilePicker {
            override fun open(
                mimeTypes: Array<String>,
                onResult: (ByteArray?) -> Unit,
            ) {
                pendingCallback = onResult
                launcher.launch(mimeTypes)
            }
        }
    }
}
