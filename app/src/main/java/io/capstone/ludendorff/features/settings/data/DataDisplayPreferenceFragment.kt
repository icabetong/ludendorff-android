package io.capstone.ludendorff.features.settings.data

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.doOnPreDraw
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.extensions.setup
import io.capstone.ludendorff.databinding.FragmentSettingsDataDisplayBinding
import io.capstone.ludendorff.features.shared.BaseFragment
import io.capstone.ludendorff.features.shared.BasePreference

class DataDisplayPreferenceFragment: BaseFragment() {
    private var _binding: FragmentSettingsDataDisplayBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this,
            object: OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    controller?.navigateUp()
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsDataDisplayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInsets(binding.root, binding.appBar.toolbar,
            arrayOf(view.findViewById(R.id.preferencesFragment)))

        controller = findNavController()
        binding.appBar.toolbar.setup(
            titleRes = R.string.settings_data_display,
            iconRes = R.drawable.ic_round_arrow_back_24,
            onNavigationClicked = { controller?.navigateUp() }
        )

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    class DataDisplayPreferences: BasePreference() {
        private var controller: NavController? = null

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.xml_settings_data, rootKey)
        }

        override fun onStart() {
            super.onStart()
            controller = findNavController()
            findPreference<Preference>(PREFERENCE_KEY_ASSET)
                ?.setOnPreferenceClickListener {
                    controller?.navigate(R.id.navigation_asset_data_display_settings)
                    true
                }
            findPreference<Preference>(PREFERENCE_KEY_INVENTORY)
                ?.setOnPreferenceClickListener {
                    controller?.navigate(R.id.navigation_inventory_data_display_settings)
                    true
                }
            findPreference<Preference>(PREFERENCE_KEY_ISSUED)
                ?.setOnPreferenceClickListener {
                    controller?.navigate(R.id.navigation_issued_data_display_settings)
                    true
                }
            findPreference<Preference>(PREFERENCE_KEY_STOCK_CARD)
                ?.setOnPreferenceClickListener {
                    controller?.navigate(R.id.navigation_stock_card_data_display_settings)
                    true
                }
            findPreference<Preference>(PREFERENCE_KEY_USER)
                ?.setOnPreferenceClickListener {
                    controller?.navigate(R.id.navigation_user_display_settings)
                    true
                }
        }
    }

    companion object {
        const val PREFERENCE_KEY_ASSET = "preference:data:asset"
        const val PREFERENCE_KEY_INVENTORY = "preference:data:inventory"
        const val PREFERENCE_KEY_ISSUED = "preference:data:issued"
        const val PREFERENCE_KEY_STOCK_CARD = "preference:data:stock"
        const val PREFERENCE_KEY_USER = "preference:data:user"
    }
}