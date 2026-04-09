package net.canvoki.shared.crash

import android.content.Context
import android.net.Uri
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
    ): (suspend () -> Unit) =
        suspend {
            // Build logs with \n (GitHub will render as real line breaks in textarea)
            val logs = "${report.exceptionType}: ${report.exceptionMessage ?: ""}\n\n${report.stackTrace}"

            val safeLogs =
                if (logs.length > MAX_LOG_CHARS_IN_URL) {
                    logs.take(MAX_LOG_CHARS_IN_URL) + "\n[TRUNCATED – full log available in app]"
                } else {
                    logs
                }

            val params =
                listOf(
                    "template" to "crash_report.yaml",
                    "title" to "💥 Crash: ${report.exceptionType} thrown in ${report.currentActivity}",
                    "app_version" to report.appVersion,
                    "android_version" to report.androidVersion,
                    "device" to report.deviceModel,
                    "install_method" to report.installMethod,
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

    private companion object {
        // Maximum characters for the 'logs' field in GitHub issue URL prefill.
        // Total practical URL length limit is ~8192 chars (Android WebView / Chrome).
        // Fixed parameters (title, device, etc.) consume ~500 chars.
        // 8192 - 500 = ~7692 → we use 6000 to ensure safety across all devices and network conditions.
        private const val MAX_LOG_CHARS_IN_URL = 6000
    }
}
