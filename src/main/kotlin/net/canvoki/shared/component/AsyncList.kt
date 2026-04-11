package net.canvoki.shared.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

/**
 * Composable genérico para listas cuyos elementos se obtienen de forma suspendible.
 *
 * @param T Tipo de los elementos.
 * @param refreshKeys Lista de valores que al cambiar disparan la recarga de la lista.
 * @param itemKey Función para obtener una key única por elemento.
 * @param loader Función suspendible que devuelve la lista de elementos.
 * @param itemContent Composable que renderiza cada elemento.
 */
@Composable
fun <T> AsyncList(
    refreshKeys: List<Any> = emptyList(),
    itemKey: (T) -> Any = { it.hashCode() },
    loader: suspend () -> List<T>,
    modifier: Modifier = Modifier,
    itemContent: @Composable (T) -> Unit,
) {
    var items by remember { mutableStateOf<List<T>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(refreshKeys) {
        isLoading = true
        error = null
        try {
            items = loader()
        } catch (e: Exception) {
            error = e.message ?: "Unknown error"
        } finally {
            isLoading = false
        }
    }

    when {
        isLoading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        error != null -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        }
        items.isEmpty() -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No items found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        else -> {
            LazyColumn(modifier = modifier.fillMaxSize()) {
                items(items = items, key = itemKey) { item ->
                    itemContent(item)
                }
            }
        }
    }
}
