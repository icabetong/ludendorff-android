package io.capstone.ludendorff.features.shared

import android.os.Bundle
import android.view.View
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.google.android.material.appbar.MaterialToolbar
import io.capstone.ludendorff.R

abstract class BasePickerFragment(private val manager: FragmentManager): DialogFragment() {

    protected val connectionHandler = ConnectionHandler()
    private var toolbar: MaterialToolbar? = null

    protected fun setupToolbar( toolbar: MaterialToolbar,
                                @StringRes titleRes: Int,
                                @MenuRes menuRes: Int,
                                onNavigation: () -> Unit,
                                onMenuItemClicked: (id: Int) -> Unit) {
        this.toolbar = toolbar
        with(toolbar) {
            setTitle(titleRes)
            setNavigationIcon(R.drawable.ic_round_close_24)
            setNavigationOnClickListener { onNavigation() }
            setOnMenuItemClickListener { onMenuItemClicked(it.itemId); true }
            inflateMenu(menuRes)
        }
    }

    protected fun dismissSearch() {
        toolbar?.menu?.findItem(R.id.action_search)?.collapseActionView()
    }

    val searchView: SearchView?
        get() = toolbar?.menu?.findItem(R.id.action_search)?.actionView as? SearchView

    fun show() {
        if (!this.isAdded || !this.isVisible) {
            show(manager, this::class.simpleName)
        }
    }

    override fun getTheme(): Int {
        return R.style.Component_Keeper_Picker_Core
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireDialog().window?.setWindowAnimations(R.style.Animation)
    }

    override fun onDestroy() {
        super.onDestroy()
        connectionHandler.clear()
    }
}