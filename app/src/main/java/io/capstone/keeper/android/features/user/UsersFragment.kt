package io.capstone.keeper.android.features.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.discord.panels.OverlappingPanelsLayout
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.keeper.android.R
import io.capstone.keeper.android.databinding.FragmentUsersBinding
import io.capstone.keeper.android.features.shared.components.BaseFragment

@AndroidEntryPoint
class UsersFragment: BaseFragment() {
    private var _binding: FragmentUsersBinding? = null

    private lateinit var adapter: UserAdapter

    private val binding get() = _binding!!
    private val viewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar(binding.appBar.toolbar, {
            val rootView: View? = getParentView()?.findViewById(R.id.overlappingPanels)

            if (rootView is OverlappingPanelsLayout)
                rootView.openStartPanel()
        }, R.string.activity_users, R.drawable.ic_hero_menu)


        adapter = UserAdapter()
        binding.recyclerView.adapter = adapter
    }

}