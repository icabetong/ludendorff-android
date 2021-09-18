package io.capstone.ludendorff.features.settings.core

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.work.*
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.BuildConfig
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.persistence.UserPreferences
import io.capstone.ludendorff.features.auth.AuthViewModel
import io.capstone.ludendorff.features.core.worker.TokenUpdateWorker
import io.capstone.ludendorff.features.shared.components.BasePreference
import javax.inject.Inject

@AndroidEntryPoint
class CorePreferences: BasePreference() {
    private var controller: NavController? = null

    @Inject lateinit var userPreferences: UserPreferences
    @Inject lateinit var firebaseMessaging: FirebaseMessaging

    private val authViewModel: AuthViewModel by activityViewModels()
    private val workManager by lazy {
        WorkManager.getInstance(requireContext())
    }

    private var isTriggered: Boolean = false

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.xml_settings, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findPreference<Preference>(PREFERENCE_KEY_USER)
            ?.setOnPreferenceClickListener {
                controller?.navigate(R.id.navigation_profile)
                true
            }

        findPreference<ListPreference>(PREFERENCE_KEY_THEME)
            ?.setOnPreferenceChangeListener { _, newTheme ->
                if (newTheme is String)
                    UserPreferences.notifyThemeChanged(UserPreferences.Theme.parse(newTheme.toString()))
                true
            }

        findPreference<Preference>(PREFERENCE_KEY_DEVICE_DEFAULT)
            ?.setOnPreferenceClickListener {
                isTriggered = true
                firebaseMessaging.token.addOnCompleteListener {
                    if (!it.isSuccessful) {
                        createSnackbar(R.string.feedback_token_update_error)
                        return@addOnCompleteListener
                    }

                    userPreferences.deviceToken = it.result
                    val deviceTokenRequest = OneTimeWorkRequestBuilder<TokenUpdateWorker>()
                        .addTag(TokenUpdateWorker.WORKER_TAG)
                        .setInputData(
                            workDataOf(
                                TokenUpdateWorker.EXTRA_TOKEN_ID to userPreferences.deviceToken
                            )
                        )
                        .build()
                    workManager.enqueueUniqueWork(
                        TokenUpdateWorker.WORKER_TAG,
                        ExistingWorkPolicy.REPLACE, deviceTokenRequest)
                    createSnackbar(R.string.feedback_token_update_success)
                }
                true
            }

        findPreference<Preference>(PREFERENCE_KEY_BUILD)
            ?.summary = BuildConfig.VERSION_NAME

        findPreference<Preference>(PREFERENCE_KEY_NOTICE)
            ?.setOnPreferenceClickListener {
                controller?.navigate(R.id.navigation_notices_settings)
                true
            }
    }

    override fun onStart() {
        super.onStart()
        controller = Navigation.findNavController(requireActivity(), R.id.navHostFragment)

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
        const val PREFERENCE_KEY_DEVICE_DEFAULT = "preference:device_default"
        const val PREFERENCE_KEY_BUILD = "preference:build"
        const val PREFERENCE_KEY_NOTICE = "preference:notices"
    }

}