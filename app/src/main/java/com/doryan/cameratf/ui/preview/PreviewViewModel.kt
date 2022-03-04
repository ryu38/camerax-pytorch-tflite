package com.doryan.cameratf.ui.preview

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.*
import com.doryan.cameratf.interactor.MLImageConverterPytorch
import com.doryan.cameratf.interactor.MLImageConverterTF
import com.doryan.cameratf.interactor.usecase.MLImageConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class PreviewViewModel @Inject constructor(
    app: Application,
    private val imageConverter: MLImageConverter,
): AndroidViewModel(app) {

    private val _previewImage = MutableLiveData<Bitmap?>()
    val previewImage: LiveData<Bitmap?>
        get() = _previewImage

    private val _message = MutableLiveData("no actions")
    val message: LiveData<String>
        get() = _message

    fun setPreviewImage(image: Bitmap) {
        _previewImage.value = image
    }

    fun mlProcessPreviewImage() {
        _previewImage.value?.let {
            viewModelScope.launch {
                writeMessage("converting ...")
                val result = withContext(Dispatchers.Default) {
                    imageConverter.process(it)
                }
                _previewImage.value = result
                val time = imageConverter.savedTime?.let { " $it ms" } ?: ""
                writeMessage("complete!$time")
            }
        }
    }

    fun writeMessage(text: String = "hello") {
        _message.value = text
    }
}