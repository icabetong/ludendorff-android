package io.capstone.keeper.android.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView

class NavigationItemDecoration(context: Context)
    : DividerItemDecoration(context, VERTICAL) {

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {}

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        with(outRect) {
            top = OFFSET_SIZE
            bottom = OFFSET_SIZE
            left = OFFSET_SIZE
            right = OFFSET_SIZE
        }
    }

    companion object {
        private const val OFFSET_SIZE = 4
    }
}