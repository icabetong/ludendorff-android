package io.capstone.keeper.android.features.user.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.keeper.android.R
import io.capstone.keeper.android.databinding.FragmentEditorUserBinding
import io.capstone.keeper.android.features.shared.components.BaseEditorFragment

@AndroidEntryPoint
class UserEditorFragment: BaseEditorFragment() {
    private var _binding: FragmentEditorUserBinding? = null
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
        _binding = FragmentEditorUserBinding.inflate(inflater, container, false)
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
}