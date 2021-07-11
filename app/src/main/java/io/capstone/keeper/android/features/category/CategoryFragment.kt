package io.capstone.keeper.android.features.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import io.capstone.keeper.android.R
import io.capstone.keeper.android.databinding.FragmentCategoryBinding
import io.capstone.keeper.android.features.category.editor.CategoryBottomSheet
import io.capstone.keeper.android.features.shared.components.BaseFragment

class CategoryFragment: BaseFragment() {
    private var _binding: FragmentCategoryBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        controller = Navigation.findNavController(requireActivity(), R.id.navHostFragment)
        setupToolbar(binding.appBar.toolbar, {
            controller?.navigateUp()
        }, R.string.activity_categories, R.drawable.ic_hero_x)

        binding.actionButton.setOnClickListener {
            CategoryBottomSheet(childFragmentManager).show()
        }
    }
}