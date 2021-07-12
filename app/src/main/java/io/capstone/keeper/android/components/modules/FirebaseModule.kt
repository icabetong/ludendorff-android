package io.capstone.keeper.android.components.modules

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.capstone.keeper.android.features.category.Category
import io.capstone.keeper.android.features.category.CategoryRepository
import io.capstone.keeper.android.features.core.data.FirestoreLiveData
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class FirebaseModule {

    @Singleton
    @Provides
    fun provideAuthentication(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Singleton
    @Provides
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Singleton
    @Provides
    fun provideCategoryRepository(firestore: FirebaseFirestore): FirestoreLiveData.FirestoreRepository<Category> {
        return CategoryRepository(firestore)
    }

}