package io.capstone.ludendorff.features.scan.image

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.ludendorff.components.extensions.hide
import io.capstone.ludendorff.components.extensions.show
import io.capstone.ludendorff.components.interfaces.OnItemActionListener
import io.capstone.ludendorff.databinding.FragmentPickerImageBinding
import io.capstone.ludendorff.features.core.backend.Response
import io.capstone.ludendorff.features.shared.components.BaseBottomSheet

@AndroidEntryPoint
class ImagePickerBottomSheet(manager: FragmentManager): BaseBottomSheet(manager),
    OnItemActionListener<Uri> {
    private var _binding: FragmentPickerImageBinding? = null

    private val binding get() = _binding!!
    private val viewModel: ImageViewModel by viewModels()
    private val imageAdapter = ImageAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPickerImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.recyclerView) {
            layoutManager = GridLayoutManager(context, 3)
            adapter = imageAdapter
        }
    }

    override fun onStart() {
        super.onStart()

        viewModel.images.observe(viewLifecycleOwner) {
            when(it) {
                is Response.Error -> {
                    binding.errorView.root.show()
                }
                is Response.Success -> {
                    binding.errorView.root.hide()
                    imageAdapter.submitList(it.data)
                }
            }
        }
    }

    override fun onActionPerformed(
        data: Uri?,
        action: OnItemActionListener.Action,
        container: View?
    ) {
        if (action == OnItemActionListener.Action.SELECT) {
            setFragmentResult(REQUEST_KEY_PICK, bundleOf(EXTRA_IMAGE_URI to data))
            this.dismiss()
        }
    }

    companion object {
        const val REQUEST_KEY_PICK = "request:pick:image"
        const val EXTRA_IMAGE_URI = "extra:image:uri"
    }
}