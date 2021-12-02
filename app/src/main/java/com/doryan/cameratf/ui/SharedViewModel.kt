package com.doryan.cameratf.ui

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel

class SharedViewModel: ViewModel() {

    var sharedImage: Bitmap? = null
        private set

    fun setSharedImage(image: Bitmap) {
        sharedImage = image
    }
}