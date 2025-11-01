package com.ddl.unirides.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Utilidad para comprimir y redimensionar imágenes
 */
object ImageCompressor {

    /**
     * Comprime una imagen desde una URI
     * @param context Contexto de la aplicación
     * @param imageUri URI de la imagen
     * @param maxWidth Ancho máximo de la imagen (default: 800px)
     * @param maxHeight Alto máximo de la imagen (default: 800px)
     * @param quality Calidad de compresión (0-100, default: 80)
     * @return Uri de la imagen comprimida en caché
     */
    fun compressImage(
        context: Context,
        imageUri: Uri,
        maxWidth: Int = 800,
        maxHeight: Int = 800,
        quality: Int = 80
    ): Uri? {
        try {
            // Leer la imagen original
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) return null

            // Obtener la orientación de la imagen
            val orientation = getImageOrientation(context, imageUri)

            // Redimensionar la imagen
            val resizedBitmap = resizeBitmap(bitmap, maxWidth, maxHeight)

            // Rotar la imagen según su orientación
            val rotatedBitmap = rotateBitmap(resizedBitmap, orientation)

            // Comprimir la imagen
            val byteArrayOutputStream = ByteArrayOutputStream()
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
            val compressedData = byteArrayOutputStream.toByteArray()

            // Guardar en caché
            val cacheFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            FileOutputStream(cacheFile).use { fos ->
                fos.write(compressedData)
            }

            // Limpiar
            bitmap.recycle()
            if (resizedBitmap != bitmap) resizedBitmap.recycle()
            if (rotatedBitmap != resizedBitmap) rotatedBitmap.recycle()

            return Uri.fromFile(cacheFile)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Redimensiona un bitmap manteniendo la proporción
     */
    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }

        val ratio = minOf(
            maxWidth.toFloat() / width,
            maxHeight.toFloat() / height
        )

        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Obtiene la orientación de una imagen
     */
    private fun getImageOrientation(context: Context, imageUri: Uri): Int {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val exif = inputStream?.let { ExifInterface(it) }
            inputStream?.close()

            exif?.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            ) ?: ExifInterface.ORIENTATION_NORMAL
        } catch (e: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }
    }

    /**
     * Rota un bitmap según la orientación EXIF
     */
    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
            else -> return bitmap
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}

