package net.canvoki.shared.crash

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable

interface CrashBackend {
    @get:DrawableRes
    val iconResId: Int

    @get:StringRes
    val labelResId: Int

    @Composable
    fun rememberHandler(
        report: CrashReport,
        context: Context,
    ): (suspend () -> Unit)
}
