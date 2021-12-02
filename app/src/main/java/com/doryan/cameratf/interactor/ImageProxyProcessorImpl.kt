package com.doryan.cameratf.interactor

import android.graphics.Bitmap
import android.graphics.ImageFormat
import androidx.camera.core.ImageProxy
import com.doryan.cameratf.interactor.usecase.ImageProxyProcessor
import timber.log.Timber

class ImageProxyProcessorImpl: ImageProxyProcessor {

//    override fun ProxyToBitmap(input: ImageProxy): Bitmap {
//
//    }

    override fun test(inputImg: ImageProxy) {
        if (inputImg.format == ImageFormat.JPEG) {
            Timber.i("JPEG")
        } else {
            Timber.i("else")
        }
    }
}