package net.canvoki.shared.crash

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import net.canvoki.shared.R
import net.canvoki.shared.storage.MediaCollection
import net.canvoki.shared.storage.rememberSaveToMediaStore
import net.canvoki.shared.usermessage.UserMessage
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Crash backend that saves the full crash report as a plain text file
 * to the user's public Downloads folder with a timestamped filename.
 *
 * Filename format: <applicationId>-crash-<YYYY-MM-DD-HH-MM-SSZ>.txt
 * Example: net.canvoki.carburoid-crash-2026-04-09-14-30-45Z.txt
 *
 * Uses MediaStore on Android 10+ and direct file access on older versions.
 * No permissions required.
 */
class SaveFileCrashBackend : CrashBackend {
    override val labelResId: Int = R.string.crash_action_save_file

    @Composable
    override fun rememberHandler(
        report: CrashReport,
        context: Context,
    ): (suspend () -> Unit) {
        val scope = rememberCoroutineScope()
        val saveToDownloads =
            rememberSaveToMediaStore(
                collection = MediaCollection.DOWNLOADS,
                mimeType = "text/plain",
            )

        val appId = context.packageName
        val timestamp =
            Instant
                .now()
                .atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ssX"))

        val filename = "$appId-crash-$timestamp.txt"

        return suspend {
            try {
                saveToDownloads(filename, report.fullText.toByteArray())
                UserMessage.Info("Saved to Downloads").post()
            } catch (e: Exception) {
                UserMessage.Info("Failed to save file").post()
            }
        }
    }
}
