package com.doryan.cameratf.ui.preview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.doryan.cameratf.R
import com.doryan.cameratf.databinding.FragmentPreviewBinding
import com.doryan.cameratf.interactor.ImageProxyProcessorImpl
import com.doryan.cameratf.interactor.usecase.ImageProxyProcessor
import com.doryan.cameratf.ui.SharedViewModel

class PreviewFragment: Fragment() {

    private lateinit var binding: FragmentPreviewBinding

    private val previewViewModel: PreviewViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private val imageProcessor: ImageProxyProcessor = ImageProxyProcessorImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getImageForPreview()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_preview, container, false)

        binding.run {
            viewModel = previewViewModel
            lifecycleOwner = viewLifecycleOwner
        }

        return binding.root
    }

    private fun getImageForPreview() {
        val image = sharedViewModel.sharedImage ?: return
        previewViewModel.setPreviewImage(image)
    }
}