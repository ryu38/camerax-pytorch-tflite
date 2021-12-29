package com.doryan.cameratf.interactor

import android.graphics.Bitmap
import android.graphics.Matrix
import com.doryan.cameratf.interactor.usecase.BitmapProcessor

class BitmapProcessorImpl: BitmapProcessor {

    override fun centerCropScaleRotate(src: Bitmap, width: Int, height: Int, rotate: Float?): Bitmap {
        val srcWidth = src.width
        val srcHeight = src.height

        val srcAspect = srcHeight.toFloat() / srcWidth
        val cropAspect = height.toFloat() / width

        val matrix = { srcW: Int, srcH: Int ->
            Matrix().apply {
                postScale(width.toFloat() / srcW, height.toFloat() / srcH)
                rotate?.let { postRotate(it) }
            }
        }
        if (srcAspect > cropAspect) {
            // width ratio larger than original so cut height side
            val cropHeight = (srcWidth * cropAspect).toInt()
            return Bitmap.createBitmap(
                src, 0, (srcHeight - cropHeight) / 2, srcWidth, cropHeight,
                matrix(srcWidth, cropHeight), true)
        } else {
            val cropWidth = (srcHeight / cropAspect).toInt()
            return Bitmap.createBitmap(
                src, (srcWidth - cropWidth) / 2, 0, cropWidth, srcHeight, matrix(cropWidth, srcHeight), true)
        }
    }

    override fun changeResolution(src: Bitmap, width: Int, height: Int): Bitmap {
        val matrix = Matrix()
        val widthScale = width.toFloat() / src.width
        val heightScale = height.toFloat() / src.height
        matrix.postScale(widthScale, heightScale)
        return Bitmap.createBitmap(
            src, 0, 0, src.width, src.height, matrix, true)
    }
}