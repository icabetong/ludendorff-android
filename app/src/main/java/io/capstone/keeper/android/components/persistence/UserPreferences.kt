package io.capstone.keeper.android.components.persistence

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import java.util.*

class UserPreferences(private val context: Context?) {
    private val sharedPreference by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    enum class Theme {
        SYSTEM, DARK, LIGHT;

        companion object {
            fun parse(s: String?): Theme {
                return when(s) {
                    DARK.toString().lowercase(Locale.getDefault()) -> DARK
                    LIGHT.toString().lowercase(Locale.getDefault()) -> LIGHT
                    SYSTEM.toString().lowercase(Locale.getDefault()) -> SYSTEM
                    else -> SYSTEM
                }
            }
        }
    }

    var theme: Theme
        get() = Theme.parse(sharedPreference.getString(PREFERENCE_THEME, Theme.SYSTEM.toString()))
        set(value) {
            sharedPreference.edit {
                putString(PREFERENCE_THEME, value.toString())
            }
        }

    companion object {
        const val PREFERENCE_THEME = "preference:theme"

        fun notifyThemeChanged(theme: Theme) {
            when(theme) {
                Theme.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                Theme.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                Theme.SYSTEM -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                }
            }
        }
    }
}