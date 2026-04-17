package net.canvoki.shared.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.canvoki.shared.R

sealed class AsyncState<out T> {
    data object Loading : AsyncState<Nothing>()
    data class Success<T>(val data: T) : AsyncState<T>()
    data class Error(val message: String? = null) : AsyncState<Nothing>()
    data object Empty : AsyncState<Nothing>()
}

@Composable
fun <T> Async(
    state: AsyncState<T>,
    modifier: Modifier = Modifier,
    loadingContent: @Composable () -> Unit = {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    },
    errorContent: @Composable (String?) -> Unit = { msg ->
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text(text = msg ?: stringResource(R.string.async_list_unknown_error))
        }
    },
    emptyContent: @Composable () -> Unit = {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = stringResource(R.string.async_list_not_found))
        }
    },
    content: @Composable (T) -> Unit,
) {
    Box(modifier = modifier) {
        when (state) {
            is AsyncState.Loading -> loadingContent()
            is AsyncState.Success -> content(state.data)
            is AsyncState.Error -> errorContent(state.message)
            is AsyncState.Empty -> emptyContent()
        }
    }
}
