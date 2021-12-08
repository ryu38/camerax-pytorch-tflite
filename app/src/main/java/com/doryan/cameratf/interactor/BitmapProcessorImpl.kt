package com.doryan.cameratf.interactor

import android.graphics.Bitmap
import android.graphics.Matrix
import com.doryan.cameratf.interactor.usecase.BitmapProcessor

class BitmapProcessorImpl: BitmapProcessor {

    override fun changeResolution(src: Bitmap, width: Int, height: Int): Bitmap {
        val matrix = Matrix()
        val widthScale = width.toFloat() / src.width
        val heightScale = height.toFloat() / src.height
        matrix.postScale(widthScale, heightScale)
        return Bitmap.createBitmap(
            src, 0, 0, src.width, src.height, matrix, true)
    }
}