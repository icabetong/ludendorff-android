package io.capstone.ludendorff.features.assignment.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.datePicker
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.firebase.Timestamp
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.extensions.setup
import io.capstone.ludendorff.components.extensions.toTimestamp
import io.capstone.ludendorff.databinding.FragmentEditorAssignmentBinding
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.asset.picker.AssetPickerBottomSheet
import io.capstone.ludendorff.features.assignment.Assignment
import io.capstone.ludendorff.features.assignment.AssignmentViewModel
import io.capstone.ludendorff.features.shared.components.BaseEditorFragment
import io.capstone.ludendorff.features.user.User
import io.capstone.ludendorff.features.user.picker.UserPickerBottomSheet

@AndroidEntryPoint
class AssignmentEditorFragment: BaseEditorFragment(), FragmentResultListener {
    private var _binding: FragmentEditorAssignmentBinding? = null
    private var controller: NavController? = null
    private var requestKey = REQUEST_KEY_CREATE

    private val binding get() = _binding!!
    private val viewModel: AssignmentViewModel by activityViewModels()
    private val editorViewModel: AssignmentEditorViewModel by viewModels()

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
        _binding = FragmentEditorAssignmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.transitionName = TRANSITION_NAME_ROOT

        binding.appBar.toolbar.setup(
            titleRes = R.string.title_assignment_create,
            iconRes = R.drawable.ic_hero_x,
            onNavigationClicked = { controller?.navigateUp() },
            customTitleView = binding.appBar.toolbarTitleTextView
        )

        arguments?.getParcelable<Assignment>(EXTRA_ASSIGNMENT)?.let {
            requestKey = REQUEST_KEY_UPDATE

            binding.appBar.toolbarTitleTextView.setText(R.string.title_assignment_update)
            binding.root.transitionName = TRANSITION_NAME_ROOT + it.assignmentId

            binding.assetTextInput.setText(it.asset?.assetName)
            binding.userTextInput.setText(it.user?.name)
            binding.dateAssignedTextInput.setText(it.formatDateAssigned(requireContext()))
            binding.dateReturnedTextInput.setText(it.formatDateReturned(requireContext()))
            binding.locationTextInput.setText(it.location)
            binding.remarksTextInput.setText(it.remarks)
        }

        registerForFragmentResult(
            arrayOf(AssetPickerBottomSheet.REQUEST_KEY_PICK,
                UserPickerBottomSheet.REQUEST_KEY_PICK),
            this
        )
    }

    override fun onStart() {
        super.onStart()

        controller = Navigation.findNavController(requireActivity(), R.id.navHostFragment)
    }

    override fun onResume() {
        super.onResume()

        binding.locationTextInput.doAfterTextChanged {
            editorViewModel.assignment.location = it.toString()
        }
        binding.remarksTextInput.doAfterTextChanged {
            editorViewModel.assignment.remarks = it.toString()
        }
        binding.assetTextInput.setOnClickListener {
            AssetPickerBottomSheet(childFragmentManager)
                .show()
        }
        binding.userTextInput.setOnClickListener {
            UserPickerBottomSheet(childFragmentManager)
                .show()
        }
        binding.dateAssignedTextInput.setOnClickListener {
            MaterialDialog(requireContext()).show {
                lifecycleOwner(viewLifecycleOwner)
                datePicker { _, date ->
                    val timestamp = date.toTimestamp()

                    val formattedDate = Assignment.formatTimestamp(timestamp, it.context)
                    binding.dateAssignedTextInput.setText(formattedDate)
                    editorViewModel.assignment.dateAssigned = timestamp
                }
            }
        }
        binding.dateReturnedTextInput.setOnClickListener {
            MaterialDialog(requireContext()).show {
                lifecycleOwner(viewLifecycleOwner)
                datePicker { _, date ->
                    val timestamp = date.toTimestamp()

                    val formattedDate = Assignment.formatTimestamp(timestamp, it.context)
                    binding.dateReturnedTextInput.setText(formattedDate)
                    editorViewModel.assignment.dateReturned = timestamp
                }
                positiveButton(R.string.button_continue)
                negativeButton(R.string.button_cancel)
            }
        }

        binding.appBar.toolbarActionButton.setOnClickListener {
            if (editorViewModel.assignment.asset == null) {
                createSnackbar(R.string.feedback_empty_asset)
                return@setOnClickListener
            }
            if (editorViewModel.assignment.user == null) {
                createSnackbar(R.string.feedback_empty_user)
                return@setOnClickListener
            }
            if (requestKey != REQUEST_KEY_UPDATE &&
                    editorViewModel.assignment.dateAssigned == null) {
                MaterialDialog(requireContext()).show {
                    lifecycleOwner(viewLifecycleOwner)
                    title(R.string.dialog_no_date_assigned_title)
                    message(R.string.dialog_no_date_assignment_message)
                    positiveButton(R.string.button_use_today) {
                        val timestamp = Timestamp.now()
                        editorViewModel.assignment.dateAssigned = timestamp
                    }
                    negativeButton(R.string.button_pick_date) {
                        binding.dateAssignedTextInput.performClick()
                        return@negativeButton
                    }
                }
            }

            if (editorViewModel.assignment.location.isNullOrBlank()) {
                MaterialDialog(requireContext()).show {
                    lifecycleOwner(viewLifecycleOwner)
                    title(R.string.dialog_confirm_location_empty_title)
                    message(R.string.dialog_confirm_location_empty_message)
                    positiveButton(R.string.button_continue)
                    negativeButton(R.string.button_cancel) {
                        binding.locationTextInput.requestFocus()
                        return@negativeButton
                    }
                }
            }

            if (requestKey == REQUEST_KEY_UPDATE)
                viewModel.update(editorViewModel.assignment)
            else viewModel.create(editorViewModel.assignment)
            controller?.navigateUp()
        }
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when(requestKey) {
            AssetPickerBottomSheet.REQUEST_KEY_PICK -> {
                result.getParcelable<Asset>(AssetPickerBottomSheet.EXTRA_ASSET)?.let {
                    editorViewModel.assignment.asset = it.minimize()
                    binding.assetTextInput.setText(it.assetName)
                }
            }
            UserPickerBottomSheet.REQUEST_KEY_PICK -> {
                result.getParcelable<User>(UserPickerBottomSheet.EXTRA_USER)?.let {
                    editorViewModel.assignment.user = it.minimize()
                    binding.userTextInput.setText(it.getDisplayName())
                }
            }
        }
    }

    companion object {
        const val REQUEST_KEY_CREATE = "request:create"
        const val REQUEST_KEY_UPDATE = "request:update"
        const val EXTRA_ASSIGNMENT = "extra:assignment"
    }
}