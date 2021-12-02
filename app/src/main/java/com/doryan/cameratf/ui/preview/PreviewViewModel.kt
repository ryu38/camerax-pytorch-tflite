package com.doryan.cameratf.ui.preview

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PreviewViewModel: ViewModel() {

    private val _previewImage = MutableLiveData<Bitmap?>()
    val previewImage: LiveData<Bitmap?>
        get() = _previewImage

    fun setPreviewImage(image: Bitmap) {
        _previewImage.value = image
    }
}