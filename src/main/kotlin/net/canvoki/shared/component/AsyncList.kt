package net.canvoki.shared.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.canvoki.shared.R

/**
 * A composable for displaying a list of items asynchronously with loading/error/empty states.
 *
 * @param refreshKeys list of values that will trigger reload when changed
 * @param loader suspend function returning a list of items
 * @param itemKey function to get a unique key for each item
 * @param groupBy optional function to group items by a key; enables sticky headers
 * @param notFoundMessage message to show when list is empty
 * @param unknownErrorMessage message to show on error
 * @param listState optional LazyListState to preserve scroll position across recompositions and configuration changes
 * @param headerContent composable to render group headers (only used if groupBy is provided)
 * @param content composable to render each item
 */
@Composable
fun <T> AsyncList(
    refreshKeys: List<Any> = emptyList(),
    loader: suspend () -> List<T>,
    itemKey: ((T) -> Any)? = null,
    groupBy: ((T) -> String)? = null,
    notFoundMessage: String? = null,
    unknownErrorMessage: String? = null,
    listState: LazyListState = rememberLazyListState(),
    headerContent: @Composable (String) -> Unit = { group ->
        Text(
            text = group,
            style = MaterialTheme.typography.titleMedium,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp, 8.dp),
        )
    },
    content: @Composable (T) -> Unit,
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
            error = e.message
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
                    text = error ?: unknownErrorMessage ?: stringResource(R.string.async_list_unknown_error),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        items.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = notFoundMessage ?: stringResource(R.string.async_list_not_found),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
            ) {
                if (groupBy != null) {
                    val grouped = items.groupBy(groupBy)
                    grouped.forEach { (group, groupItems) ->
                        stickyHeader(key = "header_$group") {
                            headerContent(group)
                        }
                        if (itemKey != null) {
                            items(groupItems, key = itemKey) { content(it) }
                        } else {
                            items(groupItems) { content(it) }
                        }
                    }
                } else if (itemKey != null) {
                    items(items, key = itemKey) { content(it) }
                } else {
                    items(items) { content(it) }
                }
            }
        }
    }
}
