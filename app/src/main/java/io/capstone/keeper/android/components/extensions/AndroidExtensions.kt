package io.capstone.keeper.android.components.extensions

import android.widget.AbsListView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.onLastItemReached(onLastItemReachedFunction: () -> Unit) {
    var isScrolling = false

    val onScrollListener = object: RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                isScrolling = true
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = layoutManager as? LinearLayoutManager
            if (layoutManager != null) {
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount

                if (isScrolling && (firstVisibleItemPosition + visibleItemCount == totalItemCount)) {
                    isScrolling = false
                    onLastItemReachedFunction()
                }
            }
        }
    }
    this.addOnScrollListener(onScrollListener)
}