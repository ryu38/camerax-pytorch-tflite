package com.doryan.cameratf.ui.camera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Rational
import android.util.Size
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.doryan.cameratf.GlobalConfig
import com.doryan.cameratf.R
import com.doryan.cameratf.databinding.FragmentCameraBinding
import com.doryan.cameratf.interactor.BitmapProcessorImpl
import com.doryan.cameratf.interactor.ImageProxyProcessorImpl
import com.doryan.cameratf.interactor.usecase.BitmapProcessor
import com.doryan.cameratf.interactor.usecase.ImageProxyProcessor
import com.doryan.cameratf.ui.SharedViewModel
import timber.log.Timber
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment: Fragment() {

    private lateinit var binding: FragmentCameraBinding

    private val cameraViewModel: CameraViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private val imageProxyProcessor: ImageProxyProcessor by lazy { ImageProxyProcessorImpl() }
    private val bitmapProcessor: BitmapProcessor by lazy { BitmapProcessorImpl() }

    private var permissionAccepted = false
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // check whether all permissions granted or not
        if (allPermissionsGranted()) {
            permissionAccepted = true
        } else {
            // if not, request required permissions here
            requestsLauncher.launch(REQUIRED_PERMISSIONS)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_camera, container, false)

        startCamera()

        // assign functions to each view here
        binding.shutter.setOnClickListener { takePhoto() }
        binding.chooseImg.setOnClickListener { pickImageFromGallery() }
        binding.changeCamera.setOnClickListener { switchCamera() }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        // permissions required when app is started
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

        private const val IMG_WIDTH = GlobalConfig.imageWidth
        private const val IMG_HEIGHT = GlobalConfig.imageHeight
    }

    private val requestsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        // Each key in permissions is like "android.permission.CAMERA"
        if (permissions.values.all { it }) {
            permissionAccepted = true
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        cameraViewModel.getCameraProvider()
        cameraViewModel.cameraProvider.observe(viewLifecycleOwner, Observer {
            bindPreview(it)
        })
    }

    // this is executed after getting an instance of CameraProvider
    private fun bindPreview(cameraProvider: ProcessCameraProvider) {

        val viewFinder = binding.viewFinder

        val resolution = Size(IMG_WIDTH, IMG_HEIGHT)
        val rational = Rational(IMG_WIDTH, IMG_HEIGHT)
//        val screenSize = Size(viewFinder.width, viewFinder.height)
//        val rotation = viewFinder.display.rotation

        val preview = Preview.Builder()
            .setTargetResolution(resolution)
            .build().apply {
                setSurfaceProvider(viewFinder.surfaceProvider)
            }

        imageCapture = ImageCapture.Builder()
            .setTargetResolution(resolution)
            .build().apply {
                setCropAspectRatio(rational)
            }

//        val imageAnalyzer = ImageAnalysis.Builder()
//            .build()

        val orientationEventListener = object : OrientationEventListener(requireContext()) {
            override fun onOrientationChanged(orientation : Int) {
                // Monitors orientation values to determine the target rotation value
                val rotation : Int = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
                imageCapture?.targetRotation = rotation
            }
        }
        orientationEventListener.enable()

        try {
            // Unbind use cases before rebinding
            cameraProvider.unbindAll()

            // Bind use cases to camera
            cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture)

        } catch(exc: Exception) {
            Timber.e("Use case binding failed: $exc")
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object: ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val srcBitmap = imageProxyProcessor.convertProxytoBitmap(image)
                    val rotate = image.imageInfo.rotationDegrees.toFloat()
                    image.close()
                    srcBitmap?.let {
                        val bitmap = bitmapProcessor.centerCropScaleRotate(it, IMG_WIDTH, IMG_HEIGHT, rotate)
                        sendImageToPreview(bitmap)
                    }
                }

                override fun onError(exception: ImageCaptureException) {

                }
            }
        )
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }

        startForPickImageResult.launch(intent)
    }

    // create this initially in order not to occur errors
    private val startForPickImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.also { uri ->
                    val inputStream = requireActivity().contentResolver.openInputStream(uri)
                    val src = BitmapFactory.decodeStream(inputStream)
                    val bitmap = bitmapProcessor.centerCropScaleRotate(src, IMG_WIDTH, IMG_HEIGHT)
                    sendImageToPreview(bitmap)
                }
            }
        }

    private fun sendImageToPreview(image: Bitmap) {
        sharedViewModel.setSharedImage(image)
        findNavController().navigate(
            CameraFragmentDirections.actionCameraFragmentToPreviewFragment()
        )
    }

    private fun switchCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
            CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
        startCamera()
    }
}