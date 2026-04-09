package net.canvoki.shared.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.canvoki.shared.crash.CrashReportDialog
import net.canvoki.shared.settings.ThemeSettings
import net.canvoki.shared.usermessage.UserMessageSnackbarHost

/**
 * Top level Activity component with all the whistles:
 *
 * - All what material3 Scaffold already provides
 * - Material theme with color scheme from preferences
 * - CrashReportDialog (requires a Reporter in Application)
 * - SnackBar that shows all usermessages
 * - Applies device insets for modern Androids
 * - Column layout
 */

@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    MaterialTheme(
        colorScheme = ThemeSettings.effectiveColorScheme(),
    ) {
        CrashReportDialog()
        Scaffold(
            topBar = topBar,
            snackbarHost = { UserMessageSnackbarHost() },
            contentWindowInsets = WindowInsets.safeDrawing,
        ) { padding ->
            Column(
                modifier =
                    modifier
                        .fillMaxSize()
                        .padding(padding),
            ) {
                content()
            }
        }
    }
}
