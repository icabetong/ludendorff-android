package io.capstone.ludendorff.features.category.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.extensions.setup
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.FragmentEditorCategoryBinding
import io.capstone.ludendorff.features.category.Category
import io.capstone.ludendorff.features.category.CategoryViewModel
import io.capstone.ludendorff.features.category.subcategory.SubcategoryAdapter
import io.capstone.ludendorff.features.category.subcategory.SubcategoryEditorBottomSheet
import io.capstone.ludendorff.features.shared.BaseEditorFragment

@AndroidEntryPoint
class CategoryEditorFragment: BaseEditorFragment(), OnItemActionListener<String>,
    FragmentResultListener {
    private var _binding: FragmentEditorCategoryBinding? = null
    private var controller: NavController? = null
    private var requestKey = REQUEST_KEY_CREATE

    private val binding get() = _binding!!
    private val subcategoryAdapter = SubcategoryAdapter(this)
    private val editorViewModel: CategoryEditorViewModel by viewModels()
    private val viewModel: CategoryViewModel by activityViewModels()

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
        _binding = FragmentEditorCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInsets(binding.root, binding.appBar.toolbar, arrayOf(binding.addActionButton.root))

        binding.root.transitionName = TRANSITION_NAME_ROOT
        binding.appBar.toolbar.setup(
            titleRes = R.string.title_category_create,
            iconRes = R.drawable.ic_round_close_24,
            menuRes = R.menu.menu_editor,
            onNavigationClicked = { controller?.navigateUp() },
            customTitleView = binding.appBar.toolbarTitleTextView
        )

        with(binding.recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = subcategoryAdapter
        }

        arguments?.getParcelable<Category>(EXTRA_CATEGORY)?.let {
            requestKey = REQUEST_KEY_CREATE
            editorViewModel.category = it

            binding.root.transitionName = TRANSITION_NAME_ROOT + it.categoryId
            binding.appBar.toolbarTitleTextView.setText(R.string.title_category_update)
            binding.appBar.toolbar.menu.findItem(R.id.action_remove).isVisible = true

            binding.nameTextInput.setText(it.categoryName)
        }

        registerForFragmentResult(
            arrayOf(
                SubcategoryEditorBottomSheet.REQUEST_KEY_CREATE,
                SubcategoryEditorBottomSheet.REQUEST_KEY_UPDATE
            ), this
        )
    }

    override fun onStart() {
        super.onStart()
        controller = Navigation.findNavController(requireActivity(), R.id.navHostFragment)

        editorViewModel.subcategories.observe(viewLifecycleOwner) {
            subcategoryAdapter.submitList(it)
        }
    }

    override fun onStop() {
        super.onStop()
        hideKeyboardFromCurrentFocus(binding.root)
    }

    override fun onResume() {
        super.onResume()

        binding.nameTextInput.doAfterTextChanged {
            editorViewModel.triggerCategoryName(it.toString())
        }
        binding.addActionButton.root.setOnClickListener {
            SubcategoryEditorBottomSheet(childFragmentManager)
                .show()
        }
        binding.appBar.toolbarActionButton.setOnClickListener {
            with(editorViewModel.category) {
                subcategories = editorViewModel.items
            }

            if (editorViewModel.category.subcategories.isEmpty()) {
                createSnackbar(R.string.feedback_empty_subcategories,
                    view = binding.snackbarAnchor)
                return@setOnClickListener
            }
            onSaveCategory()
        }
    }

    private fun onSaveCategory() {
        if (requestKey == REQUEST_KEY_CREATE)
            viewModel.create(editorViewModel.category)
        else viewModel.update(editorViewModel.category)
        controller?.navigateUp()
    }

    override fun onActionPerformed(
        data: String?,
        action: OnItemActionListener.Action,
        container: View?
    ) {
        when(action) {
            OnItemActionListener.Action.SELECT -> {
                SubcategoryEditorBottomSheet(childFragmentManager).show {
                    arguments = bundleOf(
                        SubcategoryEditorBottomSheet.EXTRA_SUBCATEGORY to data)
                }
            }
            OnItemActionListener.Action.DELETE -> {
                data?.let { editorViewModel.remove(it) }
            }
        }
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when(requestKey) {
            SubcategoryEditorBottomSheet.REQUEST_KEY_CREATE -> {
                result.getString(SubcategoryEditorBottomSheet.EXTRA_SUBCATEGORY)?.let {
                    editorViewModel.insert(it)
                }
            }
            SubcategoryEditorBottomSheet.REQUEST_KEY_UPDATE -> {
                result.getString(SubcategoryEditorBottomSheet.EXTRA_SUBCATEGORY)?.let {
                    editorViewModel.update(it)
                }
            }
        }
    }

    companion object {
        const val REQUEST_KEY_CREATE = "request:create"
        const val REQUEST_KEY_UPDATE = "request:update"
        const val EXTRA_CATEGORY = "extra:category"
    }

}