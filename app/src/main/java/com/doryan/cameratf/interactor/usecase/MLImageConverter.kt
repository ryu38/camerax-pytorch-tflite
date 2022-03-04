package com.doryan.cameratf.interactor.usecase

import android.graphics.Bitmap

interface MLImageConverter {

    val savedTime: Long?

    fun process(bitmap: Bitmap): Bitmap
}