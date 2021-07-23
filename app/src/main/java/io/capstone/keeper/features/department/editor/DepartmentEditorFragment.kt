package io.capstone.keeper.features.department.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.keeper.R
import io.capstone.keeper.components.extensions.setup
import io.capstone.keeper.databinding.FragmentEditorDepartmentBinding
import io.capstone.keeper.features.department.Department
import io.capstone.keeper.features.shared.components.BaseEditorFragment
import io.capstone.keeper.features.user.picker.UserPickerBottomSheet

@AndroidEntryPoint
class DepartmentEditorFragment: BaseEditorFragment() {
    private var _binding: FragmentEditorDepartmentBinding? = null
    private var controller: NavController? = null
    private var requestKey: String = REQUEST_KEY_CREATE

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
        _binding = FragmentEditorDepartmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.transitionName = TRANSITION_NAME_ROOT

        controller = Navigation.findNavController(requireActivity(), R.id.navHostFragment)
        binding.appBar.toolbar.setup(
            titleRes = R.string.title_department_create,
            iconRes = R.drawable.ic_hero_x,
            onNavigationClicked = { controller?.navigateUp() }
        )

        binding.managerTextView.setOnClickListener {
            UserPickerBottomSheet(childFragmentManager).show()
        }

        arguments?.getParcelable<Department>(EXTRA_DEPARTMENT)?.let {
            requestKey = REQUEST_KEY_UPDATE

            binding.root.transitionName = TRANSITION_NAME_ROOT + it.departmentId
            binding.appBar.toolbar.setTitle(R.string.title_department_update)

            binding.nameTextInput.setText(it.name)
        }
    }

    companion object {
        const val REQUEST_KEY_CREATE = "request:create"
        const val REQUEST_KEY_UPDATE = "request:update"
        const val EXTRA_DEPARTMENT = "extra:department"
    }
}