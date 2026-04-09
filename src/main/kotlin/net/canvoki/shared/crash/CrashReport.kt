package net.canvoki.shared.crash

import java.io.Serializable

data class CrashReport(
    val appName: String,
    val appVersion: String,
    val androidVersion: String,
    val deviceModel: String,
    val currentActivity: String,
    val exceptionType: String,
    val exceptionMessage: String?,
    val installMethod: String,
    val stackTrace: String,
) : Serializable {
    val fullText: String
        get() =
            buildString {
                appendLine("=== CRASH REPORT ===")
                appendLine("App: $appName $appVersion")
                appendLine("Android: $androidVersion")
                appendLine("Device: $deviceModel")
                appendLine("Current Activity: $currentActivity")
                appendLine("Install Method: $installMethod")
                appendLine()
                appendLine("Exception: $exceptionType: $exceptionMessage")
                appendLine()
                appendLine("Stack Trace:")
                append(stackTrace)
            }

    val summary: String
        get() = "Crash in $currentActivity: $exceptionType"
}
