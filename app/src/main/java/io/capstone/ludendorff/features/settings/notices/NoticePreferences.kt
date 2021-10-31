package io.capstone.ludendorff.features.settings.notices

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import io.capstone.ludendorff.R
import io.capstone.ludendorff.features.shared.BasePreference

class NoticePreferences: BasePreference() {
    private var controller: NavController? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.xml_notices, rootKey)
    }

    override fun onStart() {
        super.onStart()
        controller = findNavController()

        findPreference<Preference>(PREFERENCE_LIBRARIES)
            ?.setOnPreferenceClickListener {
                controller?.navigate(R.id.navigation_libraries)
                true
            }
    }

    companion object {
        const val URL_AVATAR_AUTHOR = "https://www.flaticon.com/free-icon/user_149071"
        const val PREFERENCE_LIBRARIES = "preference:libraries"
        const val PREFERENCE_USER_ICON = "preference:user"
        const val PREFERENCE_ICONS = "preference:icons"
    }
}