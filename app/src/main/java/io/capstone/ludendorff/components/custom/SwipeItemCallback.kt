package io.capstone.ludendorff.components.custom

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import io.capstone.ludendorff.R
import io.capstone.ludendorff.components.interfaces.SwipeableAdapter

class SwipeItemCallback<T: SwipeableAdapter>(context: Context, private var adapter: T)
    : ItemTouchHelper.Callback() {

    private val isThemeDark: Boolean = (context.resources.configuration.uiMode
            and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    private val iconTrash = ContextCompat.getDrawable(context, R.drawable.ic_round_delete_outline_24)
    private val backgroundTrash: ColorDrawable

    init {
        val backgroundColor = if (isThemeDark)
            Color.parseColor(COLOR_BACKGROUND_DARK_DELETE)
        else Color.parseColor(COLOR_BACKGROUND_LIGHT_DELETE)

        backgroundTrash = ColorDrawable(backgroundColor)
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return defaultValue * 10
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        adapter.onSwipe(viewHolder.bindingAdapterPosition, direction)
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int = makeMovementFlags(0, ItemTouchHelper.START)

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        val itemView: View = viewHolder.itemView
        val backgroundOffset = 40

        if (dX > 0) {
            with (backgroundTrash) {
                setBounds(itemView.left, itemView.top,
                    itemView.left + dX.toInt() + backgroundOffset, itemView.bottom)
                draw(c)
            }

            iconTrash?.let {
                val tintColor = if (isThemeDark)
                    Color.parseColor(COLOR_ICON_DARK_DELETE)
                else Color.parseColor(COLOR_ICON_LIGHT_DELETE)

                it.mutate()
                it.setTint(tintColor)

                val iconMargin: Int = (itemView.height - it.intrinsicHeight) / 2

                val iconTop: Int = itemView.top + (itemView.height - it.intrinsicHeight) / 2
                val iconBottom: Int = iconTop + it.intrinsicHeight
                val iconLeft: Int = itemView.left + iconMargin
                val iconRight: Int = iconLeft + it.intrinsicWidth

                it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                it.draw(c)
            }
        } else if (dX < 0) {
            with (backgroundTrash) {
                setBounds(itemView.right + dX.toInt() - backgroundOffset, itemView.top,
                    itemView.right, itemView.bottom)
                draw(c)

                iconTrash?.let {
                    val tintColor = if (isThemeDark)
                        Color.parseColor(COLOR_ICON_DARK_DELETE)
                    else Color.parseColor(COLOR_ICON_LIGHT_DELETE)

                    it.mutate()
                    it.setTint(tintColor)

                    val iconMargin: Int = (itemView.height - it.intrinsicHeight) / 2

                    val iconTop: Int = itemView.top + (itemView.height - it.intrinsicHeight) / 2
                    val iconBottom: Int = iconTop + it.intrinsicHeight
                    val iconLeft: Int = itemView.right - iconMargin - it.intrinsicWidth
                    val iconRight: Int = itemView.right - iconMargin

                    it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    it.draw(c)
                }
            }
        }
    }

    companion object {
        const val COLOR_ICON_LIGHT_DELETE = "#ea4335"
        const val COLOR_ICON_DARK_DELETE = "#000000"
        const val COLOR_BACKGROUND_LIGHT_DELETE = "#66ea4335"
        const val COLOR_BACKGROUND_DARK_DELETE = "#ea4335"
    }
}