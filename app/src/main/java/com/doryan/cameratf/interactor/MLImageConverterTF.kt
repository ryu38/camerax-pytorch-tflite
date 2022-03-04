package com.doryan.cameratf.interactor

import android.content.Context
import android.graphics.Bitmap
import com.doryan.cameratf.GlobalConfig
import com.doryan.cameratf.interactor.usecase.MLImageConverter
import com.doryan.cameratf.ml.LiteModelEsrganTf21
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.model.Model
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import timber.log.Timber
import javax.inject.Inject

class MLImageConverterTF @Inject constructor(context: Context): MLImageConverter {

    private val option = Model.Options.Builder()
        .setDevice(Model.Device.NNAPI)
        .build()
    private val model = LiteModelEsrganTf21.newInstance(context, option)

    private var startTime: Long = 0
    override var savedTime: Long? = null

    companion object {
        val INPUT_DATATYPE = DataType.FLOAT32
        val OUTPUT_DATATYPE = DataType.FLOAT32

        private const val WIDTH = GlobalConfig.imageWidth
        private const val HEIGHT = GlobalConfig.imageHeight
        val INPUT_SHAPE = intArrayOf(1, HEIGHT, WIDTH, 3)
    }

    override fun process(bitmap: Bitmap): Bitmap {
        startTime = System.currentTimeMillis()
        val input = convertBitmapToModelInput(bitmap)
        logTime("convert input")
        val result = model.process(input)
        logTime("ML process", true)
        val output = result.outputFeature0AsTensorBuffer
        logTime("output tfbuff")
        return convertModelOutputToBitmap(output)
    }

    private fun convertBitmapToModelInput(bitmap: Bitmap): TensorBuffer {
        val tImage = convertToTImage(bitmap, INPUT_DATATYPE)
        logTime("tImage")
        return TensorBuffer.createFixedSize(INPUT_SHAPE, INPUT_DATATYPE).apply {
            loadBuffer(tImage.buffer)
        }
    }

    private fun convertModelOutputToBitmap(output: TensorBuffer): Bitmap {
        val tImage = convertToTImage(output, OUTPUT_DATATYPE)
        logTime("tImage")
        return tImage.bitmap
    }

    private fun convertToTImage(src: Bitmap, dataType: DataType): TensorImage {
        return TensorImage(dataType).apply {
            load(src)
        }
    }

    private fun convertToTImage(src: TensorBuffer, dataType: DataType): TensorImage {
        return TensorImage(dataType).apply {
            load(src)
        }
    }

    private fun logTime(tag: String, saveTime: Boolean = false) {
        val now = System.currentTimeMillis()
        val time = now - startTime
        startTime = now
        Timber.i("ML_TIME_LOG: $tag $time mills")
        if (saveTime) savedTime = time
    }
}