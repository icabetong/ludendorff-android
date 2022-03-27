package io.capstone.ludendorff.features.issued.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.datePicker
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.extensions.setup
import io.capstone.ludendorff.components.extensions.toLocalDate
import io.capstone.ludendorff.components.extensions.toTimestamp
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.components.utils.DateTimeFormatter
import io.capstone.ludendorff.components.utils.DateTimeFormatter.Companion.getDateFormatter
import io.capstone.ludendorff.databinding.FragmentEditorIssuedReportBinding
import io.capstone.ludendorff.features.asset.Asset
import io.capstone.ludendorff.features.asset.picker.AssetPickerBottomSheet
import io.capstone.ludendorff.features.issued.item.IssuedItem
import io.capstone.ludendorff.features.issued.item.IssuedItemAdapter
import io.capstone.ludendorff.features.issued.item.IssuedItemEditorBottomSheet
import io.capstone.ludendorff.features.shared.BaseEditorFragment
import okhttp3.internal.format

@AndroidEntryPoint
class IssuedReportEditorFragment: BaseEditorFragment(), FragmentResultListener,
    OnItemActionListener<IssuedItem> {
    private var _binding: FragmentEditorIssuedReportBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!
    private val editorViewModel: IssuedReportEditorViewModel by viewModels()
    private val issuedItemAdapter = IssuedItemAdapter(this)

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
        _binding = FragmentEditorIssuedReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInsets(view, binding.appBar.toolbar, arrayOf(binding.addActionButton.root))

        binding.root.transitionName = TRANSITION_NAME_ROOT
        binding.appBar.toolbar.setup(
            titleRes = R.string.title_issued_create,
            iconRes = R.drawable.ic_round_close_24,
            onNavigationClicked = { controller?.navigateUp() },
            customTitleView = binding.appBar.toolbarTitleTextView
        )

        with(binding.recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = issuedItemAdapter
        }

        registerForFragmentResult(arrayOf(
            AssetPickerBottomSheet.REQUEST_KEY_PICK,
            IssuedItemEditorBottomSheet.REQUEST_KEY_CREATE,
            IssuedItemEditorBottomSheet.REQUEST_KEY_UPDATE), this)
    }

    override fun onStart() {
        super.onStart()
        controller = Navigation.findNavController(requireActivity(), R.id.navHostFragment)

        editorViewModel.issuedItems.observe(viewLifecycleOwner) {
            issuedItemAdapter.submit(it)
        }
    }

    override fun onStop() {
        super.onStop()
        hideKeyboardFromCurrentFocus(binding.root)
    }

    override fun onResume() {
        super.onResume()

        binding.addActionButton.addActionButton.setOnClickListener {
            AssetPickerBottomSheet(childFragmentManager)
                .show()
        }
        binding.dateTextInput.setOnClickListener(::invokeDatePicker)
        binding.dateTextInputLayout.setEndIconOnClickListener(::invokeDatePicker)
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when(requestKey) {
            AssetPickerBottomSheet.REQUEST_KEY_PICK -> {
                result.getParcelable<Asset>(AssetPickerBottomSheet.EXTRA_ASSET)?.let {
                    IssuedItemEditorBottomSheet(childFragmentManager).show {
                        arguments = bundleOf(AssetPickerBottomSheet.EXTRA_ASSET to it)
                    }
                }
            }
            IssuedItemEditorBottomSheet.REQUEST_KEY_CREATE -> {
                result.getParcelable<IssuedItem>(IssuedItemEditorBottomSheet.EXTRA_ISSUED_ITEM)?.let {
                    editorViewModel.insert(it)
                }
            }
            IssuedItemEditorBottomSheet.REQUEST_KEY_UPDATE -> {
                result.getParcelable<IssuedItem>(IssuedItemEditorBottomSheet.EXTRA_ISSUED_ITEM)?.let {
                    editorViewModel.update(it)
                }
            }
        }
    }

    private fun invokeDatePicker(view: View) {
        MaterialDialog(requireContext()).show {
            lifecycleOwner(viewLifecycleOwner)
            datePicker { _, calendar ->
                editorViewModel.issuedReport.date = calendar.toTimestamp()

                val formatter = getDateFormatter(withYear = true, isShort = true)
                binding.dateTextInput.setText(formatter.format(calendar.toLocalDate()))
            }
        }
    }

    override fun onActionPerformed(
        data: IssuedItem?,
        action: OnItemActionListener.Action,
        container: View?
    ) {
        when(action) {
            OnItemActionListener.Action.SELECT -> {
                IssuedItemEditorBottomSheet(childFragmentManager).show {
                    arguments = bundleOf(IssuedItemEditorBottomSheet.EXTRA_ISSUED_ITEM to data)
                }
            }
            OnItemActionListener.Action.DELETE -> {}
        }
    }

}