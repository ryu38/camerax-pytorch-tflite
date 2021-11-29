package com.doryan.cameratf.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.doryan.cameratf.R
import com.doryan.cameratf.databinding.FragmentCameraBinding
import timber.log.Timber
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment: Fragment() {

    private val viewModel: CameraViewModel by viewModels()
    private lateinit var binding: FragmentCameraBinding

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // check whether all permissions granted or not
        if (allPermissionsGranted()) {
            startCamera()
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
        binding.run {
            cameraViewModel = viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        binding.shutter.setOnClickListener { takePhoto() }

        return binding.root
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    private val requestsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        // Each key in permissions is like "android.permission.CAMERA"
        if (permissions.values.all { it }) {
            startCamera()
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        viewModel.getCameraProvider()
        viewModel.cameraProvider.observe(viewLifecycleOwner, Observer {
            bindPreview(it)
        })
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

        imageCapture = ImageCapture.Builder()
            .build()

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

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

                }

                override fun onError(exception: ImageCaptureException) {

                }
            }
        )
    }
}