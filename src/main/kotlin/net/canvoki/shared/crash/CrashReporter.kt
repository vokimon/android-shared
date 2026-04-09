package net.canvoki.shared.crash

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

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
        val safeTimestamp =
            java.time.Instant
                .now()
                .toString()
                .replace("T", "-")
                .replace(":", "-")
                .replace(".", "-")
        val report =
            CrashReport(
                appName = config.appName,
                appVersion = config.appVersion,
                androidVersion = "Android ${Build.VERSION.RELEASE}",
                deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
                currentActivity = CurrentActivityTracker.currentActivityClassName ?: "unknown",
                exceptionType = ex.javaClass.simpleName,
                exceptionMessage = ex.message,
                installMethod = getInstallMethod(context),
                stackTrace = ex.stackTraceToString(),
                timestamp = safeTimestamp,
            )

        try {
            context.openFileOutput(config.crashFileName, Context.MODE_PRIVATE).use { stream ->
                ObjectOutputStream(stream).use { it.writeObject(report) }
            }
        } catch (_: IOException) {
        }
    }

    fun loadCrashReport(
        context: Context,
        fileName: String,
    ): CrashReport? =
        runCatching {
            context.openFileInput(fileName).use { stream ->
                ObjectInputStream(stream).use { it.readObject() as CrashReport }
            }
        }.getOrNull()

    private fun getInstallMethod(context: Context): String =
        try {
            val installer = context.packageManager.getInstallerPackageName(context.packageName)
            when {
                installer == null -> {
                    if (isDebuggable(context)) {
                        "Built from Source"
                    } else {
                        "GitHub Release APK"
                    }
                }
                installer == "com.android.vending" -> "Google Play"
                installer == "org.fdroid.fdroid" -> "F-Droid"
                installer.contains("amazon") -> "Amazon Appstore"
                installer.contains("samsung") -> "Samsung Galaxy Store"
                else -> "Other ($installer)"
            }
        } catch (e: Exception) {
            "Unknown"
        }

    private fun isDebuggable(context: Context): Boolean =
        context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0
}
