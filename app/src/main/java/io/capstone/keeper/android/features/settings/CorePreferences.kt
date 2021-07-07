package io.capstone.keeper.android.features.settings

import android.os.Bundle
import android.view.View
import androidx.preference.ListPreference
import io.capstone.keeper.android.R
import io.capstone.keeper.android.components.persistence.UserPreferences
import io.capstone.keeper.android.features.shared.components.BasePreference

class CorePreferences: BasePreference() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.xml_settings, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findPreference<ListPreference>(UserPreferences.PREFERENCE_THEME)
            ?.setOnPreferenceChangeListener { _, newTheme ->
                if (newTheme is String)
                    UserPreferences.notifyThemeChanged(UserPreferences.Theme.parse(newTheme.toString()))
                true
            }
    }


}