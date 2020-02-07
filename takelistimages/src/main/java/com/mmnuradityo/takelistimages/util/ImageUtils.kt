package com.mmnuradityo.takelistimages.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*


open class ImageUtils(private val activity: Activity) {

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                return drawable.bitmap
            }
        }
        val bitmap: Bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(
                1,
                1,
                Bitmap.Config.ARGB_8888
            )
        } else {
            Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        }
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun bitmapResize(image: Bitmap, maxSize: Int): Bitmap? {
        var width = image.width
        var height = image.height

        if (width < maxSize) {
            return null
        }

        if (height < maxSize) {
            return null
        }

        var imageRatio = width.toDouble() / height.toDouble()
        when {
            imageRatio > 1 -> {
                width = maxSize
                height = (maxSize / imageRatio).toInt()
            }
            imageRatio == 1.0 -> {
                width = maxSize
                height = maxSize
            }
            else -> {
                imageRatio = height.toDouble() / width.toDouble()
                width = (maxSize / imageRatio).toInt()
                height = maxSize
            }
        }

        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    fun getNameFromUri(uri: Uri?): String {
        var fileName = uri?.path
        val startIndex = fileName?.lastIndexOf('/')
        val endIndex = fileName?.lastIndexOf('.')
        fileName = when {
            startIndex != -1 && endIndex!! > -1 ->
                fileName?.substring(startIndex!! + 1, endIndex)
            startIndex != -1 ->
                fileName?.substring(startIndex!! + 1)
            else ->
                "not found"
        }
        return fileName!!
    }

    fun bitmapToFile(bitmap: Bitmap, name: String): File {
        val file = ContextWrapper(activity.applicationContext).filesDir
        val fileImg = File(file, "$name.jpg")
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val bitmapData = baos.toByteArray()

        try {
            val os = FileOutputStream(fileImg)
            os.write(bitmapData)
            os.flush()
            os.close()
        } catch (ex: Exception) {

        }
        return fileImg
    }

    fun uriToBitmap(uri: Uri): Bitmap {
        val option = BitmapFactory.Options()
        option.inPreferredConfig = Bitmap.Config.ALPHA_8
        val inputStream = activity.contentResolver.openInputStream(uri)
        val image = BitmapFactory.decodeStream(inputStream, null, option)
        inputStream?.close()
        return image!!
    }

    fun bitmapToUri(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(activity.applicationContext)
        var imageFile = wrapper.getDir("Images", Context.MODE_PRIVATE)
        imageFile = File(imageFile, "${UUID.randomUUID()}.jpg")

        try {
            val outStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
            outStream.flush()
            outStream.close()
        } catch (ex: Exception) {

        }

        return Uri.parse(imageFile.absolutePath)
    }

    fun getImageSize(imageFile: File): Float {
        val sizeInBytes = imageFile.length()
        val sizeInKB = sizeInBytes / 1024.0F
        return sizeInKB / 1024.0F
    }

    fun checkOrientationImage(bitmap: Bitmap, imagePath: String): Bitmap? {
        val imageFile = File(imagePath)

        val exif =
            ExifInterface(imageFile.absolutePath)
        val orientation: Int = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90F)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180F)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270F)
            ExifInterface.ORIENTATION_NORMAL -> bitmap
            else -> bitmap
        }
    }

    fun rotateImage(img: Bitmap, degree: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(degree)
        val rotatedImg =
            Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }
}