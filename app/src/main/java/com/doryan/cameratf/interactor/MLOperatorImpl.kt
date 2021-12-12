package com.doryan.cameratf.interactor

import android.content.Context
import android.graphics.Bitmap
import com.doryan.cameratf.interactor.usecase.MLImageConverter
import com.doryan.cameratf.ml.HumanToChimpDrq
import com.doryan.cameratf.ml.LiteModelEsrganTf21
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.model.Model
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import timber.log.Timber

class MLImageConverterImpl(context: Context): MLImageConverter {

    private val option = Model.Options.Builder()
        .setDevice(Model.Device.NNAPI)
        .build()
    private val model = LiteModelEsrganTf21.newInstance(context, option)

    private var startTime: Long = 0

    companion object {
        val DATATYPE = DataType.FLOAT32
        val INPUT_SHAPE = intArrayOf(1, 50, 50, 3)
    }

    override fun process(bitmap: Bitmap): Bitmap {
        startTime = System.currentTimeMillis()
        val input = convertBitmapToModelInput(bitmap)
        logTime("convert input")
        val result = model.process(input)
        logTime("ML process")
        val output = result.outputFeature0AsTensorBuffer
        logTime("output tfbuff")
        val tImage = convertToTImage(output)
        logTime("tImage")
        return tImage.bitmap
    }

    private fun convertBitmapToModelInput(bitmap: Bitmap): TensorBuffer {
        val tImage = convertToTImage(bitmap)
        logTime("tImage")
        return TensorBuffer.createFixedSize(INPUT_SHAPE, DATATYPE).apply {
            loadBuffer(tImage.buffer)
        }
    }

    private fun convertToTImage(src: Bitmap): TensorImage {
        return TensorImage(DATATYPE).apply {
            load(src)
        }
    }

    private fun convertToTImage(src: TensorBuffer): TensorImage {
        return TensorImage(DATATYPE).apply {
            load(src)
        }
    }

    private fun logTime(tag: String) {
        val now = System.currentTimeMillis()
        val time = now - startTime
        startTime = now
        Timber.i("ML_TIME_LOG: $tag $time mills")
    }
}