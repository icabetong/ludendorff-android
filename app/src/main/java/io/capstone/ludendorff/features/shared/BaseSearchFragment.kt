package io.capstone.ludendorff.features.shared

import android.os.Bundle
import com.algolia.instantsearch.core.connection.ConnectionHandler

abstract class BaseSearchFragment: BaseFragment() {

    protected val connection = ConnectionHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = buildContainerTransform()
        sharedElementReturnTransition = buildContainerTransform()
    }

    override fun onDestroy() {
        super.onDestroy()
        connection.clear()
    }

    companion object {
        const val TRANSITION_SEARCH = "transition:search"
    }
}