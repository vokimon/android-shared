package net.canvoki.shared.storage

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * A composable function that opens the system file picker and returns a
 * content:// [Uri] for the selected file, without reading its contents.
 */
@Composable
fun rememberFileUriPicker(): (mimeTypes: Array<String>, onResult: (Uri?) -> Unit) -> Unit {
    var pendingCallback by remember { mutableStateOf<((Uri?) -> Unit)?>(null) }

    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
        ) { uri ->
            val callback = pendingCallback
            pendingCallback = null
            callback?.invoke(uri)
        }

    return remember(launcher) {
        { mimeTypes, onResult ->
            pendingCallback = onResult
            launcher.launch(mimeTypes)
        }
    }
}

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
    val launcher = rememberFileUriPicker()
    return remember(launcher) {
        object : OpenFilePicker {
            override fun open(
                mimeTypes: Array<String>,
                onResult: (ByteArray?) -> Unit,
            ) {
                launcher(mimeTypes) { uri ->
                    if (uri != null) {
                        try {
                            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            onResult(bytes)
                        } catch (e: Exception) {
                            onResult(null)
                        }
                    } else {
                        onResult(null)
                    }
                }
            }
        }
    }
}
