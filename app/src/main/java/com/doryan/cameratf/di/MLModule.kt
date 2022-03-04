package com.doryan.cameratf.di

import android.content.Context
import com.doryan.cameratf.interactor.MLImageConverterPytorch
import com.doryan.cameratf.interactor.MLImageConverterTF
import com.doryan.cameratf.interactor.usecase.MLImageConverter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Initialize MLImageConverter only once
@Module
@InstallIn(SingletonComponent::class)
object MLModule {

    @Provides
    @Singleton
    fun provideMLImageConverter(
        @ApplicationContext context: Context
    ): MLImageConverter =
        // comment out either one you want to use
        MLImageConverterTF(context)
//        MLImageConverterPytorch(context)
}