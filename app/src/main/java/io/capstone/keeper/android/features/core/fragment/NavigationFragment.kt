package io.capstone.keeper.android.features.core.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.discord.panels.OverlappingPanelsLayout
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.keeper.android.R
import io.capstone.keeper.android.components.NavigationItemDecoration
import io.capstone.keeper.android.databinding.FragmentNavigationBinding
import io.capstone.keeper.android.features.core.viewmodel.CoreViewModel
import io.capstone.keeper.android.features.shared.adapter.MenuAdapter
import io.capstone.keeper.android.features.shared.components.BaseFragment

@AndroidEntryPoint
class NavigationFragment: BaseFragment(), MenuAdapter.MenuItemListener {
    private var _binding: FragmentNavigationBinding? = null

    private val binding get() = _binding!!
    private val viewModel: CoreViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNavigationBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.recyclerView) {
            addItemDecoration(NavigationItemDecoration(context))
            adapter = MenuAdapter(activity, R.menu.menu_navigation, this@NavigationFragment)
        }
    }

    override fun onItemSelected(id: Int) {
        viewModel.setDestination(id)

        val view: View = requireActivity().findViewById(R.id.overlappingPanels)
        if (view is OverlappingPanelsLayout)
            view.closePanels()
    }

}