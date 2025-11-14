package com.example.fluentread.service.file

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Enum class representing different media types with their associated MIME types and relative paths.
 */
enum class MediaType {
    VIDEO,
    AUDIO;

    /** Returns the MIME type associated with the media type. */
    val mimeType: String
        get() = when(this) {
            VIDEO -> "video/mp4"
            AUDIO -> "audio/mpeg"
        }

    /** Returns the relative path associated with the media type. */
    val relativePath: String
        get() = when(this) {
            VIDEO -> "Movies/FluentRead"
            AUDIO -> "Music/FluentRead"
        }

    /** Generates ContentValues for inserting media into MediaStore. */
    fun contentValues(fileName: String): ContentValues = when(this) {
            VIDEO -> ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Video.Media.MIME_TYPE, mimeType)
                put(MediaStore.Video.Media.RELATIVE_PATH, relativePath)
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
            AUDIO -> ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Audio.Media.MIME_TYPE, mimeType)
                put(MediaStore.Audio.Media.RELATIVE_PATH, relativePath)
                put(MediaStore.Audio.Media.IS_PENDING, 1)
            }
        }
    /** Returns the content URI associated with the media type. */
    val uri: Uri
        get() = when(this) {
            VIDEO -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            AUDIO -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
}

/**
 * Helper object for file-related operations.
 */
object FileHelper {

    const val TAG = "FileHelper"

    const val CACHE_DIR_NAME = "images"

    const val APP_NAME = "FluentRead"

    const val BITMAP_COMPRESS_QUALITY = 100
    /**
     * Retrieves the authority string for the AppFileProvider.
     *
     * @param context The application context.
     * @return The authority string of the AppFileProvider.
     */
    fun getFileAuthority(context: Context): String {
        val fileProvider = ComponentName(context, AppFileProvider::class.java)
        val info = context.packageManager.getProviderInfo(fileProvider, 0)
        return info.authority
    }

    /**
     * Gets the content URI for a given file using the AppFileProvider.
     *
     * @param context The application context.
     * @param getFile The file for which to get the URI.
     * @return The content URI of the file.
     */
    fun getFileUri(context: Context, getFile: File): Uri =
        FileProvider.getUriForFile(context, getFileAuthority(context), getFile)


    /**
     * Writes an ImageBitmap to a file and returns its URI use for sharing between apps (Messenger, Zalo, etc..).
     * @param context The application context.
     * @param bitmap The ImageBitmap to write to file.
     * @param getUri Optional lambda to customize URI generation from File.
     */
    suspend fun writeImageToFile(
        context: Context,
        bitmap: ImageBitmap,
        getUri:( (File) -> Uri)? = null
    ) {
        val getUriFile = getUri ?: { file: File ->
            getFileUri(context, file)
        }

        // Perform file writing on IO dispatcher
        withContext(Dispatchers.IO) {
            // Get or create cache directory
            getCacheDir(context)?.let {
                runCatching {
                    val file = File(it, "shared_image_${System.currentTimeMillis()}.png")
                    val outputStream = file.outputStream()
                    outputStream.use {
                        bitmap.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, BITMAP_COMPRESS_QUALITY, it)
                        it.flush()
                    }
                    getUriFile(file)
                }.onFailure { err ->
                    Log.e(TAG, "writeImageToFile: Failed to write image to file: ${err.message}" )
                }
            }
        }
    }

    // Gets or creates the cache directory for images.
    private fun getCacheDir(context: Context): File? = createCacheDir(context)

    // Saves a video file to the MediaStore and deletes the original file.
    fun saveFileToMediaStore(context: Context, file: File?, mediaType: MediaType): String? {
        file ?: return null

        val values = mediaType.contentValues(file.name)

        val resolver = context.contentResolver

        val uri = resolver.insert(mediaType.uri, values)
            ?: return null

        return try {
            resolver.openOutputStream(uri).use { outputStream ->
                file.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream!!)
                }
            }
            values.clear()
            if(mediaType == MediaType.VIDEO) {
                values.put(MediaStore.Video.Media.IS_PENDING, 0)
            } else {
                values.put(MediaStore.Audio.Media.IS_PENDING, 0)
            }
            resolver.update(uri, values, null, null)
            // Delete the original file
            file.delete()
            uri.toString()
        } catch (e: Exception) {
            Log.e(TAG, "saveFileToMediaStore: Failed to save file to MediaStore: ${e.message}" )
            null
        }
    }

    /**
     * Creates a cache directory for images if it doesn't already exist.
     */
    private fun createCacheDir(context: Context): File? {

        val cacheDir = File(context.cacheDir, CACHE_DIR_NAME)
        return if (!cacheDir.exists()) {
            val created = cacheDir.mkdirs()
            if (created) cacheDir else null
        } else {
            cacheDir
        }
    }

    /**
     * Creates a file in the cache directory with the specified file name.
     * @param context The application context.
     * @param fileName The name of the file to create.
     * @return The created file, or null if creation failed.
     */
    fun createFileInCache(context: Context, fileName: String): File? {
        return try {
            val cacheDir = getCacheDir(context)
            if (cacheDir != null) {
                val file = File(cacheDir, fileName)
                if (!file.exists()) {
                    file.createNewFile()
                }
                file
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "createFileInCache: Failed to create file in cache: ${e.message}" )
            null
        }
    }

    /**
     * Clears the image cache directory.
     */
    fun clearDirCache(context: Context) {
        runCatching {
            val cacheDir = File(context.cacheDir, CACHE_DIR_NAME)
            if (cacheDir.exists()) {
                cacheDir.deleteRecursively()
            }
        }.onFailure { err ->
            Log.e(TAG, "clearDirCache: Failed to clear cache directory: ${err.message}" )
        }
    }

    /**
     * Retrieves a file from the cache directory that satisfies the given condition.
     * @param context The application context.
     * @param cachedPrefix The prefix to identify the cached file.
     * @param fileName The name of the file to retrieve.
     */
    fun getFileFromCache(
        context: Context,
        cachedPrefix: String,
        // add hasCode if need to identify more specific file
        fileName: String,
    ): Uri? {
        runCatching {
            val cacheDir = getCacheDir(context)
            if(cacheDir == null) return@runCatching
            val file = File(cacheDir, "$cachedPrefix$fileName")

            if (file.exists()) {
                Log.d(TAG, "getFileFromCache: File found in cache: ${file.absolutePath}" )
                return getFileUri(context, file)
            }
            Log.d(TAG, "getFileFromCache: File not found in cache: ${file.absolutePath}" )
        }.onFailure {
            Log.d(TAG, "getFileFromCache: Failed to get file from cache: ${it.message}")
            throw it
        }
        return null
    }

    @SuppressLint("Recycle")
    fun logVideoFileInfo(context: Context) {
        Log.d(TAG, "logVideoFileInfo: Retrieving video file information from MediaStore")
        val projection =  arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DURATION
        )
        val cursor = context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )
        cursor?.use {
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val name = cursor.getString(nameIndex)
                val size = cursor.getLong(sizeIndex)
                val duration = cursor.getLong(durationIndex)

                Log.d(TAG, "Video File - ID: $id, Name: $name, Size: $size bytes, Duration: $duration ms")
            }
        }
    }
}