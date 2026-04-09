package net.canvoki.shared.crash

import android.content.Context
import androidx.compose.runtime.Composable

interface CrashBackend {
    val labelResId: Int

    @Composable
    fun rememberHandler(
        report: CrashReport,
        context: Context,
    ): (suspend () -> Unit)
}
