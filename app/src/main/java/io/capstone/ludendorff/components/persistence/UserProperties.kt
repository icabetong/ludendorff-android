package io.capstone.ludendorff.components.persistence

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import io.capstone.ludendorff.features.user.User

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
            putString(USER_IMAGE_URL, user.imageUrl)
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

    companion object {
        const val USER_ID = User.FIELD_ID
        const val USER_FIRST_NAME = User.FIELD_FIRST_NAME
        const val USER_LAST_NAME = User.FIELD_LAST_NAME
        const val USER_EMAIL = User.FIELD_EMAIL
        const val USER_IMAGE_URL = User.FIELD_IMAGE_URL
        const val USER_POSITION = User.FIELD_POSITION
    }
}