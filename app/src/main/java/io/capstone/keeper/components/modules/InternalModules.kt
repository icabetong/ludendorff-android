package io.capstone.keeper.components.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.capstone.keeper.components.persistence.DevicePermissions
import io.capstone.keeper.components.persistence.UserPreferences
import io.capstone.keeper.components.persistence.UserProperties

@Module
@InstallIn(SingletonComponent::class)
class InternalModules {

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
}