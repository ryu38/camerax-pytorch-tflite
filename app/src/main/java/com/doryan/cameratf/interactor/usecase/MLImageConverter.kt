package com.doryan.cameratf.interactor.usecase

import android.graphics.Bitmap

interface MLImageConverter {

    fun process(bitmap: Bitmap): Bitmap
}