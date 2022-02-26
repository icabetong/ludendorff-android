package io.capstone.ludendorff.components.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.widget.TextView
import androidx.annotation.*
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.textfield.TextInputLayout
import io.capstone.ludendorff.R
import me.saket.cascade.CascadePopupMenu
import me.saket.cascade.overrideOverflowMenu

fun Context.getDimension(@DimenRes res: Int): Int {
    return (resources.getDimension(res) / resources.displayMetrics.density).toInt()
}

fun View.hide() {
    visibility = View.GONE
}

fun View.show() {
    visibility = View.VISIBLE
}

fun Toolbar.setup(
    @StringRes titleRes: Int = 0,
    @DrawableRes iconRes: Int = R.drawable.ic_round_arrow_back_24,
    onNavigationClicked: (() -> Unit)? = null,
    @MenuRes menuRes: Int = 0,
    onMenuOptionClicked: ((itemId: Int) -> Unit)? = null,
    customTitleView: TextView? = null
) {
    if (titleRes != 0 && customTitleView == null)
        setTitle(titleRes)
    else if (titleRes != 0 && customTitleView != null)
        customTitleView.setText(titleRes)

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

@SuppressLint("DEPRECATION")
fun Drawable.setColorFilterCompat(@ColorInt color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        colorFilter = BlendModeColorFilter(color, BlendMode.SRC_ATOP)
    else setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
}

fun TextInputLayout.setCustomEndIconDrawable(@DrawableRes id: Int) {
    endIconMode = TextInputLayout.END_ICON_CUSTOM
    setEndIconDrawable(id)
}

fun TextInputLayout.removeCustomEndIconDrawable() {
    endIconDrawable = null
    endIconMode = TextInputLayout.END_ICON_NONE
}

fun SwipeRefreshLayout.setColorRes(@ColorRes foreground: Int, @ColorRes background: Int) {
    setColorSchemeResources(foreground)
    setProgressBackgroundColorSchemeResource(background)
}