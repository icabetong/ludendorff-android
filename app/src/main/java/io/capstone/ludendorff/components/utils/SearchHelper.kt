package io.capstone.ludendorff.components.utils

import androidx.paging.PagedList
import com.algolia.instantsearch.helper.android.list.SearcherSingleIndexDataSource
import com.algolia.instantsearch.helper.searcher.SearcherSingleIndex
import com.algolia.search.client.ClientSearch
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.IndexName
import com.algolia.search.model.response.ResponseSearch
import io.ktor.client.features.logging.*

class SearchHelper(indexName: String) {

    private val client = ClientSearch(
        ApplicationID(ALGOLIA_APP_ID),
        APIKey(ALGOLIA_API_KEY),
        LogLevel.ALL
    )
    val index = client.initIndex(IndexName(indexName))
    val searcher = SearcherSingleIndex(index)

    fun <T> getDataSource(callback: (json: ResponseSearch.Hit) -> T): SearcherSingleIndexDataSource.Factory<T> {
        return SearcherSingleIndexDataSource.Factory(searcher) {
            callback(it)
        }
    }

    val config: PagedList.Config
        get() = PagedList.Config.Builder()
            .setPageSize(25)
            .setEnablePlaceholders(false)
            .build()

    companion object {
        private const val ALGOLIA_APP_ID = "H1BMXJXRBE"
        private const val ALGOLIA_API_KEY = "ecfcef9a59b7ec023817ef3041de6416"
    }
}