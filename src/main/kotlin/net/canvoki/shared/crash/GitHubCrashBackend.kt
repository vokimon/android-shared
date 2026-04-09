package net.canvoki.shared.crash

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import net.canvoki.shared.R

class GitHubCrashBackend(
    private val repoUrl: String, // e.g. "https://github.com/canvoki/carburoid"
) : CrashBackend {
    override val labelResId: Int = R.string.crash_action_report_github

    @Composable
    override fun rememberHandler(
        report: CrashReport,
        context: Context,
    ): (suspend () -> Unit) =
        suspend {
            // Build logs with \n (GitHub will render as real line breaks in textarea)
            val logs = "${report.exceptionType}: ${report.exceptionMessage ?: ""}\n\n${report.stackTrace}"

            // Truncate to stay within safe URL length (~8KB total; 3500 chars for logs is safe)
            val safeLogs = if (logs.length > 3500) logs.take(3500) + "\n[TRUNCATED]" else logs

            val params =
                listOf(
                    "template" to "crash_report.yaml",
                    "title" to "💥 Crash: ${report.exceptionType} thrown in ${report.currentActivity}",
                    "app_version" to report.appVersion,
                    "android_version" to report.androidVersion,
                    "device" to report.deviceModel,
                    "current_activity" to report.currentActivity,
                    "exception_type" to report.exceptionType,
                    "logs" to safeLogs,
                ).joinToString("&") { (key, value) ->
                    "${Uri.encode(key)}=${Uri.encode(value, "UTF-8")}"
                }

            val uri = Uri.parse("$repoUrl/issues/new?$params")
            val intent =
                android.content
                    .Intent(android.content.Intent.ACTION_VIEW, uri)
                    .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
}
