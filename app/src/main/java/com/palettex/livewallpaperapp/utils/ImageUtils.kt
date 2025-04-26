package com.palettex.livewallpaperapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlin.math.max
import kotlin.math.min

object ImageUtils {
    fun loadImageFromUri(context: Context, uri: Uri, maxWidth: Int, maxHeight: Int): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                // First decode with inJustDecodeBounds=true to check dimensions
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeStream(inputStream, null, options)

                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)

                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false

                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, options)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val ratio = min(
            maxWidth.toFloat() / width.toFloat(),
            maxHeight.toFloat() / height.toFloat()
        )

        val newWidth = max(1, (width * ratio).toInt())
        val newHeight = max(1, (height * ratio).toInt())

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}