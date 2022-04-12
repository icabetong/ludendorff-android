package io.capstone.ludendorff.features.settings.data.child

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.doOnPreDraw
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.extensions.setup
import io.capstone.ludendorff.databinding.FragmentSettingsInventoryDataBinding
import io.capstone.ludendorff.features.shared.BaseFragment
import io.capstone.ludendorff.features.shared.BasePreference

class InventoryDataDisplayFragment: BaseFragment() {
    private var _binding: FragmentSettingsInventoryDataBinding? = null
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
        _binding = FragmentSettingsInventoryDataBinding.inflate(inflater, container, false)
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
            titleRes = R.string.settings_data_list_inventory,
            iconRes = R.drawable.ic_round_arrow_back_24,
            onNavigationClicked = { controller?.navigateUp() }
        )

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    companion object {
        const val PREFERENCE_KEY_INVENTORY_OVERLINE = "preference:data:inventory:overline"
        const val PREFERENCE_KEY_INVENTORY_HEADER = "preference:data:inventory:header"
        const val PREFERENCE_KEY_INVENTORY_SUMMARY = "preference:data:inventory:summary"
    }

    class InventoryDataDisplayPreference: BasePreference() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.xml_settings_data_inventory, rootKey)
        }
    }
}