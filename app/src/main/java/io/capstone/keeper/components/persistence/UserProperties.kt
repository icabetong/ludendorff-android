package io.capstone.keeper.components.persistence

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import io.capstone.keeper.features.user.User
import kotlinx.coroutines.flow.flow

class UserProperties(private val context: Context) {
    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun set(user: User) {
        sharedPreferences.edit {
            putString(USER_ID, user.userId)
            putString(USER_FIRST_NAME, user.firstName)
            putString(USER_LAST_NAME, user.lastName)
            putString(USER_EMAIL, user.email)
            putString(USER_POSITION, user.position)
            putString(USER_DEPARTMENT, user.department?.departmentId)

            putBoolean(USER_PERMISSION_READ, user.hasPermission(User.PERMISSION_READ))
            putBoolean(USER_PERMISSION_WRITE, user.hasPermission(User.PERMISSION_WRITE))
            putBoolean(USER_PERMISSION_DELETE, user.hasPermission(User.PERMISSION_DELETE))
            putBoolean(USER_PERMISSION_AUDIT, user.hasPermission(User.PERMISSION_AUDIT))
            putBoolean(USER_PERMISSION_MANAGE_USER, user.hasPermission(User.PERMISSION_MANAGE_USERS))
            putBoolean(USER_PERMISSION_ADMINISTRATIVE, user.hasPermission(User.PERMISSION_ADMINISTRATIVE))
        }
    }

    fun set(field: String, value: String) {
        sharedPreferences.edit {
            putString(field, value)
        }
    }

    fun set(field: String, value: Int) {
        sharedPreferences.edit {
            putInt(field, value)
        }
    }

    fun clear() {
        sharedPreferences.edit {
            remove(USER_ID)
            remove(USER_FIRST_NAME)
            remove(USER_LAST_NAME)
            remove(USER_EMAIL)
            remove(USER_POSITION)
            remove(USER_DEPARTMENT)
            remove(USER_PERMISSION_READ)
            remove(USER_PERMISSION_WRITE)
            remove(USER_PERMISSION_DELETE)
            remove(USER_PERMISSION_AUDIT)
            remove(USER_PERMISSION_MANAGE_USER)
            remove(USER_PERMISSION_ADMINISTRATIVE)
        }
    }

    fun getDisplayName(): String? {
        return if (firstName != null && lastName != null)
            "$firstName $lastName"
        else email
    }

    var userId: String?
        get() = sharedPreferences.getString(USER_ID, null)
        set(value) {
            sharedPreferences.edit {
                putString(USER_ID, value)
            }
        }

    var firstName: String?
        get() = sharedPreferences.getString(USER_FIRST_NAME, null)
        set(value) {
            sharedPreferences.edit {
                putString(USER_FIRST_NAME, value)
            }
        }

    var lastName: String?
        get() = sharedPreferences.getString(USER_LAST_NAME, null)
        set(value) {
            sharedPreferences.edit {
                putString(USER_LAST_NAME, value)
            }
        }

    var email: String?
        get() = sharedPreferences.getString(USER_EMAIL, null)
        set(value) {
            sharedPreferences.edit {
                putString(USER_EMAIL, value)
            }
        }

    var imageUrl: String?
        get() = sharedPreferences.getString(USER_IMAGE_URL, null)
        set(value) {
            sharedPreferences.edit {
                putString(USER_IMAGE_URL, value)
            }
        }

    var position: String?
        get() = sharedPreferences.getString(USER_POSITION, null)
        set(value) {
            sharedPreferences.edit {
                putString(USER_POSITION, value)
            }
        }

    var department: String?
        get() = sharedPreferences.getString(USER_DEPARTMENT, null)
        set(value) {
            sharedPreferences.edit {
                putString(USER_DEPARTMENT, value)
            }
        }


    var hasReadPermissions: Boolean
        get() = sharedPreferences.getBoolean(USER_PERMISSION_READ, false)
        set(value) {
            sharedPreferences.edit {
                putBoolean(USER_PERMISSION_READ, value)
            }
        }

    var hasWritePermission: Boolean
        get() = sharedPreferences.getBoolean(USER_PERMISSION_WRITE, false)
        set(value) {
            sharedPreferences.edit {
                putBoolean(USER_PERMISSION_WRITE, value)
            }
        }

    var hasDeletePermission: Boolean
        get() = sharedPreferences.getBoolean(USER_PERMISSION_DELETE, false)
        set(value) {
            sharedPreferences.edit {
                putBoolean(USER_PERMISSION_DELETE, value)
            }
        }

    var hasAuditPermission: Boolean
        get() = sharedPreferences.getBoolean(USER_PERMISSION_AUDIT, false)
        set(value) {
            sharedPreferences.edit {
                putBoolean(USER_PERMISSION_AUDIT, false)
            }
        }

    var hasManageUserPermissions: Boolean
        get() = sharedPreferences.getBoolean(USER_PERMISSION_MANAGE_USER, false)
        set(value) {
            sharedPreferences.edit {
                putBoolean(USER_PERMISSION_MANAGE_USER, false)
            }
        }

    var hasAdministrativePermissions: Boolean
        get() = sharedPreferences.getBoolean(USER_PERMISSION_ADMINISTRATIVE, false)
        set(value) {
            sharedPreferences.edit {
                putBoolean(USER_PERMISSION_ADMINISTRATIVE, false)
            }
        }

    companion object {
        const val USER_ID = User.FIELD_ID
        const val USER_FIRST_NAME = User.FIELD_FIRST_NAME
        const val USER_LAST_NAME = User.FIELD_LAST_NAME
        const val USER_EMAIL = User.FIELD_EMAIL
        const val USER_IMAGE_URL = User.FIELD_IMAGE_URL
        const val USER_POSITION = User.FIELD_POSITION
        const val USER_DEPARTMENT_ID = User.FIELD_DEPARTMENT_ID
        const val USER_DEPARTMENT = User.FIELD_DEPARTMENT_NAME

        const val USER_PERMISSION_READ = "canRead"
        const val USER_PERMISSION_WRITE = "canWrite"
        const val USER_PERMISSION_DELETE = "canDelete"
        const val USER_PERMISSION_AUDIT = "canAudit"
        const val USER_PERMISSION_MANAGE_USER = "canManageUsers"
        const val USER_PERMISSION_ADMINISTRATIVE = "isAdmin"
    }
}