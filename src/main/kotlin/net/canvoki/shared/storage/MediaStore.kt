package net.canvoki.shared.storage

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.File

/**
 * Enum that represents public media collections managed by the system.
 *
 * Each collection corresponds to a well-known directory (e.g. Downloads, Pictures)
 * and provides the correct MediaStore URI, relative path, and column names for
 * both modern (API 29+) and legacy (API < 29) storage access.
 */
enum class MediaCollection(
    private val mimeTypeHint: String? = null,
) {
    /** Files saved to the user's "Downloads" folder */
    DOWNLOADS("application/octet-stream"),

    /** Files saved to the user's "Documents" folder */
    DOCUMENTS("application/octet-stream"),

    /** Image files saved to the "Pictures" folder */
    IMAGES("image/*"),

    /** Video files saved to the "Movies" folder */
    VIDEO("video/*"),

    /** Audio files saved to the "Music" folder */
    AUDIO("audio/*"),
    ;

    fun getContentUri(): Uri =
        when (this) {
            DOWNLOADS -> MediaStore.Downloads.EXTERNAL_CONTENT_URI
            DOCUMENTS -> MediaStore.Files.getContentUri("external")
            IMAGES -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            VIDEO -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            AUDIO -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

    fun getRelativePath(): String =
        when (this) {
            DOWNLOADS -> Environment.DIRECTORY_DOWNLOADS
            DOCUMENTS -> Environment.DIRECTORY_DOCUMENTS
            IMAGES -> Environment.DIRECTORY_PICTURES
            VIDEO -> Environment.DIRECTORY_MOVIES
            AUDIO -> Environment.DIRECTORY_MUSIC
        }

    internal fun displayNameColumn(): String =
        when (this) {
            DOWNLOADS -> MediaStore.Downloads.DISPLAY_NAME
            DOCUMENTS -> MediaStore.Files.FileColumns.DISPLAY_NAME
            else -> MediaStore.MediaColumns.DISPLAY_NAME
        }

    internal fun mimeTypeColumn(): String =
        when (this) {
            DOWNLOADS -> MediaStore.Downloads.MIME_TYPE
            DOCUMENTS -> MediaStore.Files.FileColumns.MIME_TYPE
            else -> MediaStore.MediaColumns.MIME_TYPE
        }

    internal fun supportsRelativePath(): Boolean =
        when (this) {
            DOWNLOADS, DOCUMENTS -> true
            else -> false
        }
}

/**
 * Returns a lambda that saves binary content to a public media collection
 * using the appropriate storage API for the current Android version.
 *
 * On Android 10+ (API 29+), it uses MediaStore with scoped storage.
 * On Android 9 and below, it writes directly to the public directory.
 *
 * The returned lambda is stable across recompositions as long as [collection]
 * and [mimeType] don't change.
 *
 * @param collection the target media collection (e.g. [MediaCollection.DOWNLOADS])
 * @param mimeType the MIME type of the content (default: "application/octet-stream")
 * @return a function `(filename: String, content: ByteArray) -> Unit`
 *
 * Example:
 * ```kotlin
 * @Composable
 * fun ExportScreen() {
 *     val saveToDownloads = rememberSaveToMediaStore(
 *         collection = MediaCollection.DOWNLOADS,
 *         mimeType = "text/plain"
 *     )
 *
 *     Button(onClick = {
 *         saveToDownloads("report.txt", "File content".toByteArray())
 *     }) {
 *         Text("Save to Downloads")
 *     }
 * }
 * ```
 */
@Composable
fun rememberSaveToMediaStore(
    collection: MediaCollection,
    mimeType: String = "application/octet-stream",
): (filename: String, content: ByteArray) -> Unit {
    val context = LocalContext.current
    return remember(context, collection, mimeType) {
        { filename, content ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+
                val resolver = context.contentResolver
                val contentValues =
                    ContentValues().apply {
                        put(collection.displayNameColumn(), filename)
                        put(collection.mimeTypeColumn(), mimeType)
                        if (collection.supportsRelativePath()) {
                            put(MediaStore.Downloads.RELATIVE_PATH, collection.getRelativePath())
                        }
                    }
                resolver.insert(collection.getContentUri(), contentValues)?.let { uri ->
                    resolver.openOutputStream(uri)?.use { it.write(content) }
                }
            } else {
                // Android 9-
                val publicDir =
                    Environment.getExternalStoragePublicDirectory(
                        collection.getRelativePath(),
                    )
                val file =
                    File(publicDir, filename).apply {
                        parentFile?.mkdirs()
                        writeBytes(content)
                    }
                context.sendBroadcast(
                    Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)),
                )
            }
        }
    }
}
