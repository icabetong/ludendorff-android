package io.capstone.ludendorff.components.custom

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
            top = OFFSET_SIZE_VERTICAL
            bottom = OFFSET_SIZE_VERTICAL
            left = OFFSET_SIZE_HORIZONTAL
            right = OFFSET_SIZE_HORIZONTAL
        }
    }

    companion object {
        private const val OFFSET_SIZE_VERTICAL = 4
        private const val OFFSET_SIZE_HORIZONTAL = 8
    }
}