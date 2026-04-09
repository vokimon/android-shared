// net.canvoki.shared.crash.CrashReporter.kt
package net.canvoki.shared.crash

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import java.io.IOException

data class CrashReporterConfig(
    val appName: String,
    val appVersion: String,
    val crashFileName: String,
    val backends: List<CrashBackend> = emptyList(),
)

object CurrentActivityTracker {
    @Volatile
    var currentActivityClassName: String? = null
        private set

    fun register(application: Application) {
        application.registerActivityLifecycleCallbacks(
            object : Application.ActivityLifecycleCallbacks {
                override fun onActivityResumed(activity: Activity) {
                    currentActivityClassName = activity::class.java.simpleName
                }

                override fun onActivityPaused(activity: Activity) {
                    // Leave as null if you want only "currently resumed" activity
                }

                override fun onActivityCreated(
                    activity: Activity,
                    savedInstanceState: Bundle?,
                ) {}

                override fun onActivityStarted(activity: Activity) {}

                override fun onActivityStopped(activity: Activity) {}

                override fun onActivitySaveInstanceState(
                    activity: Activity,
                    outState: Bundle,
                ) {}

                override fun onActivityDestroyed(activity: Activity) {}
            },
        )
    }
}

object CrashReporter {
    var config: CrashReporterConfig? = null
    private var originalHandler: Thread.UncaughtExceptionHandler? = null

    fun initialize(
        application: Application,
        config: CrashReporterConfig,
    ) {
        this.config = config
        CurrentActivityTracker.register(application)

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
        val currentActivity = CurrentActivityTracker.currentActivityClassName ?: "unknown"
        val report =
            """
            App: ${config.appName}
            Version: ${config.appVersion}
            Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
            Device: ${Build.MANUFACTURER} ${Build.MODEL}
            Current Activity: $currentActivity

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
