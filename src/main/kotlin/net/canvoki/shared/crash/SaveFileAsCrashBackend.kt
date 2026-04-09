// net.canvoki.shared.crash.SaveFileAsCrashBackend.kt
package net.canvoki.shared.crash

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import net.canvoki.shared.R
import net.canvoki.shared.storage.rememberSaveFilePicker
import net.canvoki.shared.usermessage.UserMessage
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Crash backend that lets the user choose where to save the crash report
 * using the system's "Save As" file picker (Storage Access Framework).
 *
 * The filename is auto-generated as: <applicationId>-crash-<timestamp>.txt
 * Example: net.canvoki.carburoid-crash-2026-04-09-14-30-45Z.txt
 */
class SaveFileAsCrashBackend : CrashBackend {
    override val labelResId: Int = R.string.crash_action_save_as

    @Composable
    override fun rememberHandler(
        report: CrashReport,
        context: Context,
    ): (suspend () -> Unit) {
        val scope = rememberCoroutineScope()
        val appId = context.packageName
        val timestamp =
            Instant
                .now()
                .atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ssX"))
        val filename = "$appId-crash-$timestamp.txt"

        val saver = rememberSaveFilePicker("text/plain")

        return suspend {
            try {
                saver.save(filename, report.fullText.toByteArray())
                UserMessage.Info("Report saved").post()
            } catch (e: Exception) {
                UserMessage.Info("Failed to save file").post()
            }
        }
    }
}
