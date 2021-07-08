package io.capstone.keeper.android.components.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.capstone.keeper.android.components.persistence.UserPreferences
import io.capstone.keeper.android.components.persistence.UserProperties

@Module
@InstallIn(ActivityComponent::class)
class InternalModules {

    @Provides
    fun provideUserProperties(@ApplicationContext context: Context): UserProperties {
        return UserProperties(context)
    }

    @Provides
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences {
        return UserPreferences(context)
    }
}