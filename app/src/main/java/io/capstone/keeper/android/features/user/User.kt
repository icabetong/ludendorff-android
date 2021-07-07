package io.capstone.keeper.android.features.user

import java.util.*

data class User @JvmOverloads constructor(
    var userId: String = UUID.randomUUID().toString(),
    var firstName: String? = null,
    var lastName: String? = null,
    var email: String? = null,
    var permissions: Int = 0,
    var position: String? = null,
    var department: String? = null
)