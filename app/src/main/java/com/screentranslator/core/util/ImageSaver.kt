package com.screentranslator.core.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility for saving captured and translated screen bitmaps to device storage (Pictures/ScreenTranslator).
 */
object ImageSaver {

    suspend fun saveImage(
        context: Context,
        bitmap: Bitmap,
        isTranslated: Boolean
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val prefix = if (isTranslated) "ScreenTranslate_Translated" else "ScreenTranslate_Original"
            val filename = "${prefix}_${timeStamp}.png"

            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveImageQAndAbove(context, bitmap, filename)
            } else {
                saveImageLegacy(context, bitmap, filename)
            }

            if (uri != null) {
                withContext(Dispatchers.Main) {
                    val message = if (isTranslated) {
                        "Saved translated screen to Pictures/ScreenTranslator"
                    } else {
                        "Saved original screen to Pictures/ScreenTranslator"
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
                Result.success(uri)
            } else {
                Result.failure(IllegalStateException("Failed to create image storage Uri"))
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed to save image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            Result.failure(e)
        }
    }

    private fun saveImageQAndAbove(context: Context, bitmap: Bitmap, filename: String): Uri? {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ScreenTranslator")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return null
        resolver.openOutputStream(uri)?.use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }

        contentValues.clear()
        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, contentValues, null, null)
        return uri
    }

    private fun saveImageLegacy(context: Context, bitmap: Bitmap, filename: String): Uri? {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "ScreenTranslator"
        )
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, filename)
        FileOutputStream(file).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
        return Uri.fromFile(file)
    }
}
