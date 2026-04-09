// net.canvoki.shared.crash.GitHubCrashBackend.kt
package net.canvoki.shared.crash

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import net.canvoki.shared.R

class GitHubCrashBackend(
    private val repoUrl: String,
) : CrashBackend {
    override val labelResId: Int = R.string.crash_action_report_github

    @Composable
    override fun rememberHandler(
        report: CrashReport,
        context: Context,
    ): (suspend () -> Unit) {
        val scope = rememberCoroutineScope()

        return suspend {
            val body = android.net.Uri.encode(report.fullText)
            val uri = android.net.Uri.parse("$repoUrl/issues/new?body=$body")
            val intent =
                android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    uri,
                )
            context.startActivity(intent)
        }
    }
}
