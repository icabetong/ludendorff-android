package io.capstone.ludendorff.components.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.capstone.ludendorff.api.Backend
import io.capstone.ludendorff.components.persistence.DevicePermissions
import io.capstone.ludendorff.components.persistence.UserPreferences
import io.capstone.ludendorff.components.persistence.UserProperties
import io.capstone.ludendorff.features.scan.image.ImageRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class InternalModules {

    @Singleton
    @Provides
    fun provideBackend(): Backend {
        return Backend()
    }

    @Provides
    fun provideUserProperties(@ApplicationContext context: Context): UserProperties {
        return UserProperties(context)
    }

    @Provides
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences {
        return UserPreferences(context)
    }

    @Provides
    fun provideDevicePermissions(@ApplicationContext context: Context): DevicePermissions {
        return DevicePermissions(context)
    }

    @Provides
    fun provideImageRepository(@ApplicationContext context: Context): ImageRepository {
        return ImageRepository(context)
    }
}