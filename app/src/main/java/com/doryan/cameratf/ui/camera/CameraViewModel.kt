package com.doryan.cameratf.ui.camera

import android.app.Application
import android.content.Context
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel

class CameraViewModel(app: Application) : AndroidViewModel(app) {

    private val _cameraProvider = MutableLiveData<ProcessCameraProvider>()
    val cameraProvider: LiveData<ProcessCameraProvider>
        get() = _cameraProvider

    fun getCameraProvider() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(getApplication())
        cameraProviderFuture.addListener({
            _cameraProvider.value = cameraProviderFuture.get()
        }, ContextCompat.getMainExecutor(getApplication()))
    }
}