package io.capstone.ludendorff.features.settings.core

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.preference.ListPreference
import androidx.preference.Preference
import io.capstone.ludendorff.BuildConfig
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.persistence.UserPreferences
import io.capstone.ludendorff.features.core.viewmodel.CoreViewModel
import io.capstone.ludendorff.features.shared.components.BasePreference

class CorePreferences: BasePreference() {
    private lateinit var controller: NavController

    private val coreViewModel: CoreViewModel by activityViewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.xml_settings, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findPreference<Preference>(PREFERENCE_KEY_USER)
            ?.setOnPreferenceClickListener {
                controller.navigate(R.id.navigation_profile)
                true
            }

        findPreference<ListPreference>(PREFERENCE_KEY_THEME)
            ?.setOnPreferenceChangeListener { _, newTheme ->
                if (newTheme is String)
                    UserPreferences.notifyThemeChanged(UserPreferences.Theme.parse(newTheme.toString()))
                true
            }

        findPreference<Preference>(PREFERENCE_KEY_BUILD)
            ?.summary = BuildConfig.VERSION_NAME

        findPreference<Preference>(PREFERENCE_KEY_NOTICE)
            ?.setOnPreferenceClickListener {
                controller.navigate(R.id.navigation_notices_settings)
                true
            }
    }

    override fun onStart() {
        super.onStart()

        controller = Navigation.findNavController(requireActivity(), R.id.navHostFragment)

        coreViewModel.userData.observe(viewLifecycleOwner) {
            findPreference<Preference>(PREFERENCE_KEY_USER)?.run {
                title = it.getDisplayName()
                summary = it.email
            }
        }
    }

    companion object {
        const val PREFERENCE_KEY_USER = "preference:user"
        const val PREFERENCE_KEY_THEME = "preference:theme"
        const val PREFERENCE_KEY_BUILD = "preference:build"
        const val PREFERENCE_KEY_NOTICE = "preference:notices"
    }

}