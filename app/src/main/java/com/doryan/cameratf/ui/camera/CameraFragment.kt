package com.doryan.cameratf.ui.camera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.doryan.cameratf.R
import com.doryan.cameratf.databinding.FragmentCameraBinding

class CameraFragment: Fragment() {

    private val viewModel: CameraViewModel by viewModels()

    private lateinit var binding: FragmentCameraBinding

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

        return binding.root
    }
}