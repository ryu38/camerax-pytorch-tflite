package com.doryan.cameratf.interactor

import android.content.Context
import android.graphics.Bitmap
import com.doryan.cameratf.interactor.usecase.MLImageConverter
import com.doryan.cameratf.ml.GANModelFloat32
import com.doryan.cameratf.ml.GANModelDqInt8
import com.doryan.cameratf.ml.LiteModelEsrganTf21
import org.pytorch.torchvision.TensorImageUtils
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.model.Model
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import timber.log.Timber
import java.nio.FloatBuffer

class MLImageConverterTF(context: Context): MLImageConverter {

    private val option = Model.Options.Builder()
        .setDevice(Model.Device.NNAPI)
        .build()
    private val model = GANModelFloat32.newInstance(context, option)

    private var startTime: Long = 0

    companion object {
        val DATATYPE = DataType.FLOAT32
        val INPUT_SHAPE = intArrayOf(1, 3, 256, 256)
    }

    override fun process(bitmap: Bitmap): Bitmap {
        startTime = System.currentTimeMillis()
        val input = convertBitmapToModelInput(bitmap)
        logTime("convert input")
        val result = model.process(input)
        logTime("ML process")
        val output = result.outputFeature1AsTensorBuffer
        logTime("output tfbuff")
//        val tImage = convertToTImage(output)
//        logTime("tImage")
//        return tImage.bitmap
        Timber.i("DEBUG: buffer length: ${output.buffer.capacity()}")
        return bitmap

    }

    private fun convertBitmapToModelInput(bitmap: Bitmap): TensorBuffer {
        val floatBuffer = FloatBuffer.allocate(3 * 256 * 256).also {
            TensorImageUtils.bitmapToFloatBuffer(
                bitmap, 0, 0, 256, 256,
                floatArrayOf(0.5f, 0.5f, 0.5f),
                floatArrayOf(0.5f, 0.5f, 0.5f),
                it, 0
            )
        }
        return TensorBuffer.createFixedSize(INPUT_SHAPE, DATATYPE).apply {
            loadArray(floatBuffer.array())
        }
    }

//    private fun convertBitmapToModelInput(bitmap: Bitmap): TensorBuffer {
//        val tImage = convertToTImage(bitmap)
//        logTime("tImage")
//        return TensorBuffer.createFixedSize(INPUT_SHAPE, DATATYPE).apply {
//            loadBuffer(tImage.buffer)
//        }
//    }

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