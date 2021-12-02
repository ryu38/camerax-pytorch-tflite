package com.doryan.cameratf.ui.camera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.doryan.cameratf.R
import com.doryan.cameratf.databinding.FragmentCameraBinding
import com.doryan.cameratf.ui.SharedViewModel
import timber.log.Timber
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment: Fragment() {

    private lateinit var binding: FragmentCameraBinding

    private val cameraViewModel: CameraViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private var permissionAccepted = false
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

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
        startCamera()

        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_camera, container, false)

        // assign functions to each view here
        binding.shutter.setOnClickListener { takePhoto() }
        binding.chooseImg.setOnClickListener { pickImageFromGallery() }

        return binding.root
    }

    companion object {
        // permissions required when app is started
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
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
//                    sendImageToPreview(image)
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

    private val startForPickImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.also { uri ->
                    val inputStream = requireActivity().contentResolver.openInputStream(uri)
                    val image = BitmapFactory.decodeStream(inputStream)
                    sendImageToPreview(image)
                }
            }
        }

    private fun sendImageToPreview(image: Bitmap) {
        sharedViewModel.setSharedImage(image)
        findNavController().navigate(
            CameraFragmentDirections.actionCameraFragmentToPreviewFragment()
        )
    }
}