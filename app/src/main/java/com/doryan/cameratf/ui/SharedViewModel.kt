package com.doryan.cameratf.ui

import androidx.camera.core.ImageProxy
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import timber.log.Timber

class SharedViewModel: ViewModel() {

    var previewImage: ImageProxy? = null
        private set

    fun setPreviewImage(image: ImageProxy) {
        previewImage = image
    }
}