package net.canvoki.shared.crash

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.canvoki.shared.R
import java.io.IOException

@Composable
fun CrashReportScreen() {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var crashReport by remember { mutableStateOf<CrashReport?>(null) }
    val scope = rememberCoroutineScope()

    // Load crash report once
    if (crashReport == null && !showDialog) {
        runCatching {
            val config = CrashReporter.config ?: return@runCatching
            val report = CrashReporter.loadCrashReport(context, config.crashFileName)
            if (report != null) {
                crashReport = report
                showDialog = true
            }
        }.onFailure { /* silent fail */ }
    }

    if (showDialog && crashReport != null) {
        val report = crashReport!!
        AlertDialog(
            onDismissRequest = { /* Keep open until user acts */ },
            title = { Text(stringResource(R.string.crash_title)) },
            text = {
                Column(
                    modifier =
                        Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(top = 8.dp),
                ) {
                    Text(stringResource(R.string.crash_message))
                    Spacer(modifier = Modifier.height(16.dp))
                    CrashReporter.config?.backends?.forEach { backend ->
                        val handler = backend.rememberHandler(report, context)
                        Button(
                            onClick = {
                                scope.launch { handler() }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        ) {
                            Icon(
                                painter = painterResource(backend.iconResId),
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp),
                            )
                            Text(stringResource(backend.labelResId))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    crashReport = null
                    runCatching {
                        val config = CrashReporter.config ?: return@TextButton
                        context.deleteFile(config.crashFileName)
                    }
                }) {
                    Text(stringResource(R.string.crash_dialog_done))
                }
            },
            confirmButton = {},
        )
    }
}
