package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.data.model.FolderItem
import com.example.data.model.VideoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import android.content.ContentValues

data class VideoTechnicalInfo(
    val videoCodec: String,
    val audioCodec: String,
    val frameRate: String,
    val bitrate: String,
    val width: Int,
    val height: Int,
    val durationMs: Long
)

class MediaRepository(private val context: Context) {

    suspend fun getDeviceVideos(): List<VideoItem> = withContext(Dispatchers.IO) {
        val videos = mutableListOf<VideoItem>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATE_ADDED,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.RELATIVE_PATH
            } else {
                MediaStore.Video.Media.DATA
            }
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        try {
            val cursor: Cursor? = context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                sortOrder
            )

            cursor?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val titleCol = c.getColumnIndex(MediaStore.Video.Media.TITLE)
                val nameCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val durCol = c.getColumnIndex(MediaStore.Video.Media.DURATION)
                val sizeCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val widthCol = c.getColumnIndex(MediaStore.Video.Media.WIDTH)
                val heightCol = c.getColumnIndex(MediaStore.Video.Media.HEIGHT)
                val mimeCol = c.getColumnIndex(MediaStore.Video.Media.MIME_TYPE)
                val dateCol = c.getColumnIndex(MediaStore.Video.Media.DATE_ADDED)
                val pathCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    c.getColumnIndex(MediaStore.Video.Media.RELATIVE_PATH)
                } else {
                    c.getColumnIndex(MediaStore.Video.Media.DATA)
                }

                while (c.moveToNext()) {
                    val id = c.getLong(idCol)
                    val contentUri = ContentUris.withAppendedId(collection, id)
                    val displayName = c.getString(nameCol) ?: "Video_$id"
                    val title = if (titleCol != -1) c.getString(titleCol) ?: displayName else displayName
                    val duration = if (durCol != -1) c.getLong(durCol) else 0L
                    val size = c.getLong(sizeCol)
                    val width = if (widthCol != -1) c.getInt(widthCol) else 0
                    val height = if (heightCol != -1) c.getInt(heightCol) else 0
                    val mime = if (mimeCol != -1) c.getString(mimeCol) ?: "video/mp4" else "video/mp4"
                    val dateAdded = if (dateCol != -1) c.getLong(dateCol) else 0L
                    val rawPath = if (pathCol != -1) c.getString(pathCol) ?: "" else ""

                    val folderName = when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                            rawPath.trim('/').split('/').lastOrNull()?.ifEmpty { "Videos" } ?: "Videos"
                        }
                        rawPath.isNotEmpty() -> {
                            File(rawPath).parentFile?.name ?: "Videos"
                        }
                        else -> "Videos"
                    }

                    videos.add(
                        VideoItem(
                            id = id,
                            uri = contentUri,
                            title = title,
                            displayName = displayName,
                            durationMs = duration,
                            sizeBytes = size,
                            width = width,
                            height = height,
                            mimeType = mime,
                            dateAdded = dateAdded,
                            folderName = folderName,
                            path = rawPath
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        videos
    }

    suspend fun getVideoFromUri(uri: Uri): VideoItem? = withContext(Dispatchers.IO) {
        var videoItem: VideoItem? = null
        try {
            val cursor = context.contentResolver.query(
                uri,
                arrayOf(
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.SIZE,
                    MediaStore.Video.Media.MIME_TYPE
                ),
                null,
                null,
                null
            )
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val idCol = c.getColumnIndex(MediaStore.Video.Media._ID)
                    val nameCol = c.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
                    val sizeCol = c.getColumnIndex(MediaStore.Video.Media.SIZE)
                    val mimeCol = c.getColumnIndex(MediaStore.Video.Media.MIME_TYPE)

                    val id = if (idCol != -1) c.getLong(idCol) else 0L
                    val displayName = if (nameCol != -1) c.getString(nameCol) ?: "Selected Video" else "Selected Video"
                    val size = if (sizeCol != -1) c.getLong(sizeCol) else 0L
                    val mime = if (mimeCol != -1) c.getString(mimeCol) ?: "video/mp4" else "video/mp4"

                    // Try to extract duration and dimensions with retriever
                    var duration = 0L
                    var width = 0
                    var height = 0
                    try {
                        val retriever = MediaMetadataRetriever()
                        retriever.setDataSource(context, uri)
                        val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        val wStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                        val hStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                        duration = durStr?.toLongOrNull() ?: 0L
                        width = wStr?.toIntOrNull() ?: 0
                        height = hStr?.toIntOrNull() ?: 0
                        retriever.release()
                    } catch (_: Exception) {}

                    videoItem = VideoItem(
                        id = id,
                        uri = uri,
                        title = displayName,
                        displayName = displayName,
                        durationMs = duration,
                        sizeBytes = size,
                        width = width,
                        height = height,
                        mimeType = mime,
                        dateAdded = System.currentTimeMillis() / 1000,
                        folderName = "Selected",
                        path = uri.path ?: ""
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        videoItem ?: VideoItem(
            id = 0L,
            uri = uri,
            title = uri.lastPathSegment ?: "Video",
            displayName = uri.lastPathSegment ?: "Video",
            durationMs = 0L,
            sizeBytes = 0L,
            width = 0,
            height = 0,
            mimeType = "video/*",
            dateAdded = System.currentTimeMillis() / 1000,
            folderName = "External",
            path = uri.toString()
        )
    }

    fun groupVideosByFolder(videos: List<VideoItem>): List<FolderItem> {
        val grouped = videos.groupBy { it.folderName }
        return grouped.map { (folderName, items) ->
            FolderItem(
                name = folderName,
                path = items.firstOrNull()?.path ?: folderName,
                videoCount = items.size,
                representativeUri = items.firstOrNull()?.uri
            )
        }.sortedByDescending { it.videoCount }
    }

    suspend fun extractTechnicalInfo(uri: Uri): VideoTechnicalInfo = withContext(Dispatchers.IO) {
        var videoCodec = "H.264 / AVC"
        var audioCodec = "AAC"
        var frameRate = "30 fps"
        var bitrate = "Not available"
        var width = 0
        var height = 0
        var durationMs = 0L

        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val wStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val hStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val brStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
            val mimeStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)

            width = wStr?.toIntOrNull() ?: 0
            height = hStr?.toIntOrNull() ?: 0
            durationMs = durStr?.toLongOrNull() ?: 0L

            if (brStr != null) {
                val bps = brStr.toLongOrNull() ?: 0L
                if (bps > 0) {
                    val kbps = bps / 1000
                    bitrate = "$kbps kbps"
                }
            }

            if (mimeStr != null) {
                videoCodec = when {
                    mimeStr.contains("hevc", ignoreCase = true) || mimeStr.contains("h265", ignoreCase = true) -> "H.265 / HEVC"
                    mimeStr.contains("avc", ignoreCase = true) || mimeStr.contains("h264", ignoreCase = true) -> "H.264 / AVC"
                    mimeStr.contains("vp9", ignoreCase = true) -> "VP9"
                    mimeStr.contains("av01", ignoreCase = true) -> "AV1"
                    else -> mimeStr
                }
            }

            // Estimate framerate or read if API 23+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val captureRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)
                if (!captureRate.isNullOrBlank()) {
                    frameRate = "$captureRate fps"
                }
            }

            retriever.release()
        } catch (_: Exception) {}

        VideoTechnicalInfo(
            videoCodec = videoCodec,
            audioCodec = audioCodec,
            frameRate = frameRate,
            bitrate = bitrate,
            width = width,
            height = height,
            durationMs = durationMs
        )
    }

    suspend fun renameVideo(video: VideoItem, newName: String): Boolean = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            // Ensure new name has correct extension if original had one
            val ext = video.displayName.substringAfterLast('.', "")
            val nameWithExt = if (ext.isNotEmpty() && !newName.endsWith(".$ext")) {
                "$newName.$ext"
            } else {
                newName
            }
            put(MediaStore.Video.Media.DISPLAY_NAME, nameWithExt)
            put(MediaStore.Video.Media.TITLE, newName)
        }
        context.contentResolver.update(video.uri, values, null, null) > 0
    }

    suspend fun moveFileToRecycleBin(video: VideoItem): String? = withContext(Dispatchers.IO) {
        val recycleBinDir = File(context.getExternalFilesDir(null), "recycle_bin")
        if (!recycleBinDir.exists()) recycleBinDir.mkdirs()

        val fileName = "recycled_${System.currentTimeMillis()}_${video.displayName}"
        val destFile = File(recycleBinDir, fileName)

        context.contentResolver.openInputStream(video.uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }

        if (destFile.exists()) {
            // Now delete the original
            context.contentResolver.delete(video.uri, null, null)
            destFile.absolutePath
        } else {
            null
        }
    }

    suspend fun restoreFile(recycledPath: String, originalDisplayName: String): Uri? = withContext(Dispatchers.IO) {
        try {
            val recycledFile = File(recycledPath)
            if (!recycledFile.exists()) return@withContext null

            val values = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, originalDisplayName)
                put(MediaStore.Video.Media.MIME_TYPE, "video/*")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/VideonicRestored")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }
            }

            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            val uri = context.contentResolver.insert(collection, values) ?: return@withContext null

            context.contentResolver.openOutputStream(uri)?.use { output ->
                FileInputStream(recycledFile).use { input ->
                    input.copyTo(output)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Video.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, values, null, null)
            }

            recycledFile.delete()
            uri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun deletePermanently(path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            File(path).delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
