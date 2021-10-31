package io.capstone.ludendorff.features.settings.notices

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.extensions.setup
import io.capstone.ludendorff.databinding.FragmentLibrariesBinding
import io.capstone.ludendorff.databinding.LayoutItemLibraryBinding
import io.capstone.ludendorff.features.shared.BaseFragment
import io.capstone.ludendorff.features.shared.BaseViewHolder

class LibrariesFragment: BaseFragment() {
    private var _binding: FragmentLibrariesBinding? = null
    private var controller: NavController? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(object: OnBackPressedCallback(true) {
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
        _binding = FragmentLibrariesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setInsets(view, binding.appBar.appBar, arrayOf(binding.recyclerView))

        binding.appBar.toolbar.setup(
            titleRes = R.string.credit_open_source_libraries,
            onNavigationClicked = { controller?.navigateUp() }
        )

        with(binding.recyclerView) {
            adapter = LibrariesAdapter(Libs(context).libraries)
        }
    }

    override fun onStart() {
        super.onStart()
        controller = findNavController()
    }

    class LibrariesAdapter(private val items: List<Library>)
        : RecyclerView.Adapter<LibrariesAdapter.LibrariesViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibrariesViewHolder {
            val binding = LayoutItemLibraryBinding.inflate(LayoutInflater.from(parent.context),
                parent, false)
            return LibrariesViewHolder(binding.root)
        }

        override fun onBindViewHolder(holder: LibrariesViewHolder, position: Int) {
            holder.onBind(items[position])
        }

        override fun getItemCount(): Int {
            return items.size
        }

        inner class LibrariesViewHolder(itemView: View): BaseViewHolder<Library>(itemView) {
            private val binding = LayoutItemLibraryBinding.bind(itemView)

            @Suppress("DEPRECATION")
            override fun onBind(data: Library?) {
                data?.let { library ->
                    binding.nameTextView.text = library.libraryName
                    binding.versionTextView.text = library.libraryVersion

                    if (library.author.isNotEmpty())
                        binding.authorTextView.text = library.author
                    else binding.authorTextView.visibility = View.GONE

                    val license = library.licenses?.firstOrNull()
                    if (license != null) {
                        binding.licenseNameTextView.text = license.licenseName
                        binding.licenseDescriptionTextView.text =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                Html.fromHtml(license.licenseShortDescription, Html.FROM_HTML_MODE_COMPACT)
                            else Html.fromHtml(license.licenseShortDescription)
                    } else {
                        binding.licenseNameTextView.visibility = View.GONE
                        binding.licenseDescriptionTextView.visibility = View.GONE
                    }
                }
            }
        }
    }
}