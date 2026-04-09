// net.canvoki.shared.crash.ShareFileCrashBackend.kt
package net.canvoki.shared.crash

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import net.canvoki.shared.R
import java.io.File

class ShareFileCrashBackend(
    private val filename: String = "crash-report.txt",
) : CrashBackend {
    override val labelResId: Int = R.string.crash_action_share_file

    @Composable
    override fun rememberHandler(
        report: CrashReport,
        context: Context,
    ): (suspend () -> Unit) {
        val scope = rememberCoroutineScope()

        return suspend {
            val file =
                File(context.cacheDir, filename).apply {
                    writeText(report.fullText)
                }
            val uri =
                androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file,
                )
            val intent =
                android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            context.startActivity(android.content.Intent.createChooser(intent, null))
        }
    }
}
