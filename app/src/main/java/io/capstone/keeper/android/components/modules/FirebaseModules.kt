package io.capstone.keeper.android.components.modules

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.capstone.keeper.android.features.category.Category
import io.capstone.keeper.android.features.core.backend.FirestoreRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class FirebaseModules {

    @Singleton
    @Provides
    fun provideAuthentication(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

}