package net.canvoki.shared.crash

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import kotlinx.coroutines.launch
import net.canvoki.shared.R
import net.canvoki.shared.component.openUri

/**
 * Crash backend that opens a GitHub issue creation page with crash details pre-filled
 * via URL parameters.
 *
 * Requires a GitHub issue template at `.github/ISSUE_TEMPLATE/crash_report.yaml`
 * with the following fields (all of type `input` or `textarea`):
 *
 * - `title` (auto-filled via URL): "💥 Crash: {exception_type} thrown in {current_activity}"
 * - `app_version` (input): application version string
 * - `android_version` (input): Android version (e.g., "Android 14")
 * - `device` (input): device model (e.g., "Google Pixel 6")
 * - `install_method` (input): installation method (e.g., "F-Droid", "GitHub Release APK")
 * - `current_activity` (input): name of the activity at crash time
 * - `exception_type` (input): simple class name of the exception
 * - `logs` (textarea): full crash log including stack trace
 */
class GitHubCrashBackend(
    private val repoUrl: String,
) : CrashBackend {
    override val labelResId: Int = R.string.crash_action_report_github
    override val iconResId: Int = R.drawable.ic_bug_report

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
            context.openUri("$repoUrl/issues/new?$params")
        }

    private companion object {
        // Maximum characters for the 'logs' field in GitHub issue URL prefill.
        // Total practical URL length limit is ~8192 chars (Android WebView / Chrome).
        // Fixed parameters (title, device, etc.) consume ~500 chars.
        // 8192 - 500 = ~7692 → we use 6000 to ensure safety across all devices and network conditions.
        private const val MAX_LOG_CHARS_IN_URL = 6000
    }
}
