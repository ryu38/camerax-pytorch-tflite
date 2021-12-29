package com.doryan.cameratf.interactor.usecase

import android.graphics.Bitmap
import android.graphics.Matrix

interface BitmapProcessor {

    fun centerCropScaleRotate(src: Bitmap, width: Int, height: Int, rotate: Float? = null): Bitmap

    @Deprecated("not recommended", ReplaceWith("centerCropAndScale") )
    fun changeResolution(src: Bitmap, width: Int, height: Int): Bitmap
}