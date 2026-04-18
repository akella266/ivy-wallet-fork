package com.ivy.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ContentResolverModule {

    @Provides
    fun provideContentResolver(
        @ApplicationContext context: Context
    ) = context.contentResolver
}