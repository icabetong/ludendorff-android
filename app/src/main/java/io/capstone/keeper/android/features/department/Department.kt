package io.capstone.keeper.android.features.department

import java.util.*

data class Department @JvmOverloads constructor(
    var departmentId: String = UUID.randomUUID().toString(),
    var name: String? = null,
    var managerSSN: String? = null,
    var type: String? = null
)
