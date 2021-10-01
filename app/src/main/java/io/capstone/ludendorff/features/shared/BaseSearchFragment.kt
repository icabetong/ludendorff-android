package io.capstone.ludendorff.features.shared

import android.os.Bundle
import android.view.View
import com.algolia.instantsearch.core.connection.ConnectionHandler

abstract class BaseSearchFragment: BaseFragment() {

    protected val connection = ConnectionHandler()

    override fun onDestroy() {
        super.onDestroy()
        connection.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.transitionName = TRANSITION_SEARCH
    }

    companion object {
        private const val TRANSITION_SEARCH = "transition:search"
    }
}