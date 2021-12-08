package com.doryan.cameratf.interactor.usecase

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy

interface ImageProxyProcessor {

    fun convertProxytoBitmap(imageProxy: ImageProxy): Bitmap?
}