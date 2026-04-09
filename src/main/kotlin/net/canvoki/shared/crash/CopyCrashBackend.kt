// net.canvoki.shared.crash.CopyCrashBackend.kt
package net.canvoki.shared.crash

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.Composable
import net.canvoki.shared.R
import net.canvoki.shared.usermessage.UserMessage

class CopyCrashBackend : CrashBackend {
    override val labelResId: Int = R.string.crash_action_copy_report
    override val iconResId: Int = R.drawable.ic_content_copy

    @Composable
    override fun rememberHandler(
        report: CrashReport,
        context: Context,
    ): (suspend () -> Unit) {
        // Get system clipboard service
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        return suspend {
            clipboard.setPrimaryClip(ClipData.newPlainText("Crash Report", report.fullText))
            UserMessage.Info(context.getString(R.string.crash_message_copied)).post()
        }
    }
}
