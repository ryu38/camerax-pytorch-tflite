package com.doryan.cameratf.interactor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.doryan.cameratf.interactor.usecase.MLImageConverter
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.torchvision.TensorImageUtils
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class MLImageConverterPytorch(context: Context): MLImageConverter {

    private val module = Module.load(
        getAssetFilePath(context, MODEL_PATH)
    )

    private var startTime: Long = 0
    var savedTime: Long? = null

    override fun process(bitmap: Bitmap): Bitmap {
        startTime = System.currentTimeMillis()
        val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
            bitmap, FloatArray(3) { RGB_MEAN * 3 }, FloatArray(3) { RGB_STD * 3 }
        )
        logTime("input")
        val outputTensor = module.forward(IValue.from(inputTensor))
            .toTensor().dataAsFloatArray
        logTime("ML process", true)
        val result = outputTensor.bitmap
        logTime("conv bitmap")
        return result
    }

    private val FloatArray.bitmap: Bitmap
        get() {
            val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
            val totalPixels = WIDTH * HEIGHT
            val pixelArray = IntArray(totalPixels)

            val denorm = { n: Float -> (n * RGB_STD) + RGB_MEAN }

            for (i in 0 until totalPixels) {
                pixelArray[i] = Color.rgb(
                    denorm(this[i]), denorm(this[i + totalPixels]), denorm(this[i + 2 * totalPixels])
                )
            }
            bitmap.setPixels(pixelArray, 0, WIDTH, 0, 0, WIDTH, HEIGHT)
            return bitmap
        }

    private fun logTime(tag: String, saveTime: Boolean = false) {
        val now = System.currentTimeMillis()
        val time = now - startTime
        startTime = now
        Timber.i("ML_TIME_LOG: $tag $time mills")
        if (saveTime) savedTime = time
    }

    companion object {
        private const val MODEL_PATH = "GANModelInt8.ptl"

        private const val WIDTH = 256
        private const val HEIGHT = 256
        private const val RGB_MEAN = 0.5f
        private const val RGB_STD = 0.5f

        private fun getAssetFilePath(context: Context, assetName: String): String? {
            try {
                val file = File(context.filesDir, assetName)
                if (file.exists() && file.length() > 0) {
                    return file.absolutePath
                }
                context.assets.open(assetName).use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        val buffer = ByteArray(4 * 1024)
                        var read: Int
                        while (inputStream.read(buffer).also { read = it } != -1) {
                            outputStream.write(buffer, 0, read)
                        }
                        outputStream.flush()
                    }
                    return file.absolutePath
                }
            } catch (e: Exception) {
                Timber.e(e)
                return null
            }
        }
    }
}