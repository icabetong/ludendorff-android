package io.capstone.ludendorff.features.asset.usages

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.extensions.setup
import io.capstone.ludendorff.databinding.FragmentAssetUsagesBinding
import io.capstone.ludendorff.features.asset.usages.inventory.InventoryTabFragment
import io.capstone.ludendorff.features.asset.usages.issued.IssuedTabFragment
import io.capstone.ludendorff.features.shared.BaseFragment

class FindUsagesFragment: BaseFragment() {
    private var _binding: FragmentAssetUsagesBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssetUsagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInsets(view, binding.appBar.toolbar)

        binding.appBar.toolbar.setup(
            titleRes = R.string.button_find_usages,
            onNavigationClicked = { controller?.navigateUp() }
        )
        binding.viewPager.adapter = ViewPagerAdapter(view.context, childFragmentManager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }

    override fun onStart() {
        super.onStart()
        controller = findNavController()
    }

    class ViewPagerAdapter(private val context: Context, manager: FragmentManager):
        FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val tabs = arrayOf(InventoryTabFragment(), IssuedTabFragment())

        override fun getCount(): Int = tabs.size
        override fun getItem(position: Int): Fragment = tabs[position]
        override fun getPageTitle(position: Int): CharSequence? {
            return when(position) {
                0 -> context.getString(R.string.navigation_inventory)
                1 -> context.getString(R.string.navigation_issued)
                else -> null
            }
        }

    }
}