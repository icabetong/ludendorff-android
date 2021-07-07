package io.capstone.keeper.android.features.category

import java.util.*

data class Category @JvmOverloads constructor(
    var categoryId: String = UUID.randomUUID().toString(),
    var categoryName: String? = null
)
