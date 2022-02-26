package io.capstone.ludendorff.features.department.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.extensions.setup
import io.capstone.ludendorff.databinding.FragmentEditorDepartmentBinding
import io.capstone.ludendorff.features.department.Department
import io.capstone.ludendorff.features.department.DepartmentViewModel
import io.capstone.ludendorff.features.shared.BaseEditorFragment
import io.capstone.ludendorff.features.user.User
import io.capstone.ludendorff.features.user.picker.UserPickerBottomSheet

@AndroidEntryPoint
class DepartmentEditorFragment: BaseEditorFragment(), FragmentResultListener {
    private var _binding: FragmentEditorDepartmentBinding? = null
    private var controller: NavController? = null
    private var requestKey: String = REQUEST_KEY_CREATE

    private val binding get() = _binding!!
    private val editorViewModel: DepartmentEditorViewModel by viewModels()
    private val viewModel: DepartmentViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = buildContainerTransform()
        sharedElementReturnTransition = buildContainerTransform()

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
        _binding = FragmentEditorDepartmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInsets(view, binding.appBar.toolbar)

        binding.root.transitionName = TRANSITION_NAME_ROOT
        binding.appBar.toolbar.setup(
            titleRes = R.string.title_department_create,
            iconRes = R.drawable.ic_round_close_24,
            onNavigationClicked = { controller?.navigateUp() },
            customTitleView = binding.appBar.toolbarTitleTextView
        )

        arguments?.getParcelable<Department>(EXTRA_DEPARTMENT)?.let {
            requestKey = REQUEST_KEY_UPDATE
            editorViewModel.department = it

            binding.root.transitionName = TRANSITION_NAME_ROOT + it.departmentId
            binding.appBar.toolbarTitleTextView.setText(R.string.title_department_update)

            binding.nameTextInput.setText(it.name)
            binding.managerTextInput.setText(it.manager?.name)

            if (it.manager != null)
                binding.managerTextInputLayout.setEndIconDrawable(R.drawable.ic_round_close_24)
        }

        registerForFragmentResult(arrayOf(UserPickerBottomSheet.REQUEST_KEY_PICK), this)
    }

    override fun onStart() {
        super.onStart()
        controller = findNavController()
    }

    override fun onResume() {
        super.onResume()

        binding.appBar.toolbarActionButton.setOnClickListener {
            editorViewModel.department.name = binding.nameTextInput.text.toString()

            if (editorViewModel.department.name.isNullOrBlank()) {
                createSnackbar(R.string.feedback_empty_department_name, view = binding.snackbarAnchor)
                return@setOnClickListener
            }

            if (requestKey == REQUEST_KEY_CREATE)
                viewModel.create(editorViewModel.department)
            else viewModel.update(editorViewModel.department)
            controller?.navigateUp()
        }

        binding.managerTextInputLayout.setEndIconOnClickListener {
            if (editorViewModel.department.manager != null) {
                editorViewModel.department.manager = null
                binding.managerTextInput.setText(R.string.hint_vacant)
                binding.managerTextInputLayout.setEndIconDrawable(R.drawable.ic_round_keyboard_arrow_down_24)
            } else
                UserPickerBottomSheet(childFragmentManager)
                    .show()
        }
    }

    override fun onStop() {
        super.onStop()
        hideKeyboardFromCurrentFocus(binding.root)
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when(requestKey) {
            UserPickerBottomSheet.REQUEST_KEY_PICK -> {
                result.getParcelable<User>(UserPickerBottomSheet.EXTRA_USER)?.let {
                    binding.managerTextInput.setText(it.getDisplayName())
                    binding.managerTextInputLayout.setEndIconDrawable(R.drawable.ic_round_close_24)

                    editorViewModel.triggerManagerChanged(it)
                }
            }
        }
    }

    companion object {
        const val REQUEST_KEY_CREATE = "request:create"
        const val REQUEST_KEY_UPDATE = "request:update"
        const val EXTRA_DEPARTMENT = "extra:department"
    }
}