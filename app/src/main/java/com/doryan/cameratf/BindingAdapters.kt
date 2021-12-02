package com.doryan.cameratf

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

object BindingAdapters {

    @JvmStatic
    @BindingAdapter("setBitmap")
    fun loadBitmap(imageView: ImageView, bitmap: Bitmap?) {
        bitmap?.let {
            Glide.with(imageView.context)
                .asBitmap()
                .load(it)
                .into(imageView)
        }
    }
}