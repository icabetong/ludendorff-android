package io.capstone.keeper.components.extensions

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import io.capstone.keeper.R
import me.saket.cascade.CascadePopupMenu
import me.saket.cascade.overrideOverflowMenu
import kotlin.math.ceil

fun View.getCountThatFitsOnScreen(context: Context): Int {
    val deviceHeight = context.resources.displayMetrics.heightPixels
    val skeletonRowHeight = height
    return ceil((deviceHeight / skeletonRowHeight).toDouble()).toInt()
}

fun Toolbar.setup(
    @StringRes titleRes: Int = 0,
    @DrawableRes iconRes: Int = R.drawable.ic_hero_arrow_left,
    onNavigationClicked: (() -> Unit)? = null,
    @MenuRes menuRes: Int = 0,
    onMenuOptionClicked: ((itemId: Int) -> Unit)? = null
) {
    if (titleRes != 0)
        setTitle(titleRes)
    if (iconRes != 0)
        setNavigationIcon(iconRes)
    if (menuRes != 0)
        inflateMenu(menuRes)

    onNavigationClicked?.let { setNavigationOnClickListener { it() } }
    onMenuOptionClicked?.let { setOnMenuItemClickListener { item -> it(item.itemId); true } }

    overrideOverflowMenu{ context, anchor ->
        CascadePopupMenu(context, anchor,
            styler = CascadePopupMenu.Styler(
                background = {
                    ContextCompat.getDrawable(context, R.drawable.shape_cascade_background)
                }
            ))
    }
}

fun TextView.setTextColorRes(@ColorRes color: Int) {
    setTextColor(ContextCompat.getColor(context, color))
}