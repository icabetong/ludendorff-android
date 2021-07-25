package io.capstone.keeper.features.user.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import io.capstone.keeper.R
import io.capstone.keeper.components.extensions.hide
import io.capstone.keeper.components.extensions.setup
import io.capstone.keeper.components.utils.PasswordManager
import io.capstone.keeper.databinding.FragmentEditorUserBinding
import io.capstone.keeper.features.shared.components.BaseEditorFragment
import io.capstone.keeper.features.user.User

@AndroidEntryPoint
class UserEditorFragment: BaseEditorFragment() {
    private var _binding: FragmentEditorUserBinding? = null
    private var controller: NavController? = null
    private var requestKey = REQUEST_KEY_CREATE

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
        _binding = FragmentEditorUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.transitionName = TRANSITION_NAME_ROOT

        controller = Navigation.findNavController(view)
        binding.appBar.toolbar.setup(
            titleRes = R.string.title_user_create,
            iconRes = R.drawable.ic_hero_x,
            onNavigationClicked = { controller?.navigateUp() }
        )

        binding.passwordTextInput.setText(PasswordManager.generateRandom(
            isWithLetters = true,
            isWithUppercase = true,
            isWithNumbers = true,
            isWithSpecial = true,
            length = 10
        ))

        arguments?.getParcelable<User>(EXTRA_USER)?.let {
            requestKey = REQUEST_KEY_UPDATE

            binding.appBar.toolbar.setTitle(R.string.title_user_update)
            binding.root.transitionName = TRANSITION_NAME_ROOT + it.userId
            binding.passwordTextInputLayout.hide()
        }
    }

    companion object {
        const val REQUEST_KEY_CREATE = "request:create"
        const val REQUEST_KEY_UPDATE = "request:update"
        const val EXTRA_USER = "extra:user"
    }
}