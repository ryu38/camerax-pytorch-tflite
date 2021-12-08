package com.doryan.cameratf.interactor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.camera.core.ImageProxy
import com.doryan.cameratf.interactor.usecase.ImageProxyProcessor
import timber.log.Timber

class ImageProxyProcessorImpl(private val context: Context): ImageProxyProcessor {

    private val yuvToRgbConverter = YuvToRgbConverter(context)

//    override fun ProxyToBitmap(input: ImageProxy): Bitmap {
//
//    }

    @androidx.camera.core.ExperimentalGetImage
    override fun test(inputImg: ImageProxy): Bitmap? {
        val image = inputImg.image ?: return null
        val buffer = image.planes[0].buffer
        val data = ByteArray(buffer.capacity())
        buffer.get(data)
        val src = BitmapFactory.decodeByteArray(data, 0, data.size)
        val matrix = Matrix()
        matrix.postRotate(inputImg.imageInfo.rotationDegrees.toFloat())
        val targetBitmap = Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
        return targetBitmap
    }
}