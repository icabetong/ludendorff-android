package io.capstone.ludendorff.features.user.search

import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.helper.android.searchbox.SearchBoxConnectorPagedList
import io.capstone.ludendorff.components.utils.SearchHelper
import io.capstone.ludendorff.features.shared.BaseViewModel
import io.capstone.ludendorff.features.user.User

class UserSearchViewModel: BaseViewModel() {

    private val connection = ConnectionHandler()
    private val searchHelper = SearchHelper(User.COLLECTION)
    private val searcher = searchHelper.searcher

    val users: LiveData<PagedList<User>> =
        LivePagedListBuilder(searchHelper.getDataSource {
            it.deserialize(User.serializer())
        }, searchHelper.config).build()
    val searchBox = SearchBoxConnectorPagedList(searcher, listOf(users))

    init {
        connection += searchBox
    }

    override fun onCleared() {
        super.onCleared()
        connection.clear()
        searcher.cancel()
    }
}