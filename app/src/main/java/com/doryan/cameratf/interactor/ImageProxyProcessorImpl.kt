package com.doryan.cameratf.interactor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import com.doryan.cameratf.interactor.usecase.ImageProxyProcessor
import timber.log.Timber

class ImageProxyProcessorImpl: ImageProxyProcessor {

    @androidx.camera.core.ExperimentalGetImage
    override fun convertProxytoBitmap(imageProxy: ImageProxy): Bitmap? {
        val image = imageProxy.image ?: return null
        val buffer = image.planes[0].buffer
        val data = ByteArray(buffer.capacity())
        buffer.get(data)
        return BitmapFactory.decodeByteArray(data, 0, data.size)
    }
}