package io.capstone.keeper.android.features.asset.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import io.capstone.keeper.android.R
import io.capstone.keeper.android.databinding.FragmentEditorAssetBinding
import io.capstone.keeper.android.features.shared.components.BaseEditorFragment

class AssetEditorFragment: BaseEditorFragment() {
    private var _binding: FragmentEditorAssetBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = buildContainerTransform()
        sharedElementReturnTransition = buildContainerTransform()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditorAssetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.transitionName = TRANSITION_NAME_ROOT

        controller = Navigation.findNavController(view)
        setupToolbar(binding.appBar.toolbar, {
            controller?.navigateUp()
        }, icon = R.drawable.ic_hero_x)
    }

    override fun onStart() {
        super.onStart()
        setSystemBarColor(R.color.keeper_background_content)
    }

    override fun onStop() {
        super.onStop()
        setSystemBarColor(R.color.keeper_background_main)
    }
}