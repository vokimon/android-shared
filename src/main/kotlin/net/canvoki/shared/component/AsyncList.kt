package net.canvoki.shared.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A composable for displaying a list of items asynchronously with loading/error/empty states.
 *
 * @param refreshKeys list of values that will trigger reload when changed
 * @param loader suspend function returning a list of items
 * @param itemKey function to get a unique key for each item
 * @param notFoundMessage message to show when list is empty
 * @param unknownErrorMessage message to show on error
 * @param content composable to render each item
 */
@Composable
fun <T> AsyncList(
    refreshKeys: List<Any> = emptyList(),
    loader: suspend () -> List<T>,
    itemKey: ((T) -> Any)? = null,
    notFoundMessage: String = "No items found",
    unknownErrorMessage: String = "Unknown error",
    content: @Composable (T) -> Unit
) {
    var items by remember { mutableStateOf<List<T>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(refreshKeys) {
        isLoading = true
        error = null
        try {
            items = withContext(Dispatchers.IO) { loader() }
        } catch (e: Exception) {
            error = e.message ?: unknownErrorMessage
        } finally {
            isLoading = false
        }
    }

    when {
        isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = error ?: unknownErrorMessage,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        items.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = notFoundMessage,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        else -> {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (itemKey != null) {
                    items(items, key = itemKey) { content(it) }
                } else {
                    items(items) { content(it) }
                }
            }
        }
    }
}
