package com.doryan.cameratf.interactor.usecase

import android.graphics.Bitmap

interface BitmapProcessor {

    fun changeResolution(src: Bitmap, width: Int, height: Int): Bitmap
}