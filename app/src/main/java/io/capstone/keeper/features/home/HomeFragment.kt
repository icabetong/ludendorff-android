package io.capstone.keeper.features.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import io.capstone.keeper.R
import io.capstone.keeper.components.extensions.setup
import io.capstone.keeper.databinding.FragmentHomeBinding
import io.capstone.keeper.features.shared.components.BaseFragment

class HomeFragment: BaseFragment() {
    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!
    private lateinit var launcher: ActivityResultLauncher<Intent>

    override fun onAttach(context: Context) {
        super.onAttach(context)

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            throw Exception()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appBar.toolbar.setup(
            titleRes = R.string.activity_home,
            iconRes = R.drawable.ic_hero_menu,
            onNavigationClicked = { getOverlappingPanelLayout().openStartPanel() },
            menuRes = R.menu.menu_main,
            onMenuOptionClicked = { getOverlappingPanelLayout().openEndPanel() }
        )

        binding.actionButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"

            launcher.launch(intent)
        }
    }

}