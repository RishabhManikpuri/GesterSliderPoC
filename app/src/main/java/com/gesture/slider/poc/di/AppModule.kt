package com.gesture.slider.poc.di

import android.content.Context
import com.gesture.slider.core.domain.entity.GestureNavConfig
import com.gesture.slider.core.domain.repository.GestureCoreFactory
import com.gesture.slider.core.domain.repository.GestureRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGestureNavConfig(): GestureNavConfig = GestureNavConfig()

    @Provides
    @Singleton
    fun provideGestureRepository(
        @ApplicationContext context: Context,
        config: GestureNavConfig
    ): GestureRepository {
        return GestureCoreFactory.createRepository(context, config)
    }
}
