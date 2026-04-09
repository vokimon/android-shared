// net.canvoki.shared.crash.CrashReporter.kt
package net.canvoki.shared.crash

import android.app.Application
import android.content.Context
import android.os.Build
import java.io.IOException

data class CrashReporterConfig(
    val appName: String,
    val appVersion: String,
    val crashFileName: String,
    val backends: List<CrashBackend> = emptyList(),
)

object CrashReporter {
    var config: CrashReporterConfig? = null
    private var originalHandler: Thread.UncaughtExceptionHandler? = null

    fun initialize(
        application: Application,
        config: CrashReporterConfig,
    ) {
        this.config = config
        originalHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, ex ->
            saveCrashReport(application, ex, config)
            originalHandler?.uncaughtException(thread, ex)
        }
    }

    private fun saveCrashReport(
        context: Context,
        ex: Throwable,
        config: CrashReporterConfig,
    ) {
        val report =
            """
            App: ${config.appName}
            Version: ${config.appVersion}
            Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
            Device: ${Build.MANUFACTURER} ${Build.MODEL}

            Exception: ${ex.javaClass.simpleName}: ${ex.message}

            Stack Trace:
            ${ex.stackTraceToString()}
            """.trimIndent()

        try {
            context.openFileOutput(config.crashFileName, Context.MODE_PRIVATE).use { out ->
                out.write(report.toByteArray())
            }
        } catch (_: IOException) {
        }
    }
}
