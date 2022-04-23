package io.capstone.ludendorff.features.settings.core

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.BuildConfig
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.persistence.UserPreferences
import io.capstone.ludendorff.features.core.entity.EntityEditorBottomSheet
import io.capstone.ludendorff.features.core.viewmodel.CoreViewModel
import io.capstone.ludendorff.features.core.worker.TokenUpdateWorker
import io.capstone.ludendorff.features.shared.BasePreference
import javax.inject.Inject

@AndroidEntryPoint
class CorePreferences: BasePreference() {
    private var controller: NavController? = null

    @Inject lateinit var userPreferences: UserPreferences
    @Inject lateinit var firebaseMessaging: FirebaseMessaging
    @Inject lateinit var firebaseAuth: FirebaseAuth

    private val authViewModel: CoreViewModel by activityViewModels()
    private val workManager by lazy {
        WorkManager.getInstance(requireContext())
    }

    private var isTriggered: Boolean = false

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.xml_settings, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findPreference<Preference>(PREFERENCE_KEY_USER)?.apply {
            isVisible = firebaseAuth.currentUser?.isAnonymous != true
            setOnPreferenceClickListener {
                controller?.navigate(R.id.navigation_profile)
                true
            }
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
                controller?.navigate(R.id.navigation_notices_settings)
                true
            }

        findPreference<Preference>(PREFERENCE_KEY_DISPLAY)
            ?.setOnPreferenceClickListener {
                controller?.navigate(R.id.navigation_data_display_settings)
                true
            }

        findPreference<Preference>(PREFERENCE_KEY_ENTITY)
            ?.setOnPreferenceClickListener {
                EntityEditorBottomSheet(childFragmentManager).show()
                true
            }
    }

    override fun onStart() {
        super.onStart()
        controller = findNavController()

        authViewModel.userData.observe(viewLifecycleOwner) {
            findPreference<Preference>(PREFERENCE_KEY_USER)?.run {
                title = it.getDisplayName()
                summary = it.email
            }
        }
    }

    companion object {
        const val PREFERENCE_KEY_USER = "preference:user"
        const val PREFERENCE_KEY_THEME = "preference:theme"
        const val PREFERENCE_KEY_SORT = "preference:sort"
        const val PREFERENCE_KEY_ENTITY = "preference:entity"
        const val PREFERENCE_KEY_BUILD = "preference:build"
        const val PREFERENCE_KEY_NOTICE = "preference:notices"

        const val PREFERENCE_KEY_DISPLAY = "preference:display"
    }

}