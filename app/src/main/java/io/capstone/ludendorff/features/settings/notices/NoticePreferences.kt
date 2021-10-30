package io.capstone.ludendorff.features.settings.notices

import android.os.Bundle
import io.capstone.ludendorff.R
import io.capstone.ludendorff.features.shared.BasePreference

class NoticePreferences: BasePreference() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.xml_notices, rootKey)
    }

    companion object {
        const val URL_AVATAR_AUTHOR = "https://www.flaticon.com/free-icon/user_149071"
    }
}