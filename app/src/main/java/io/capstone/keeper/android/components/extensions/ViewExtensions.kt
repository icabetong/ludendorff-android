package io.capstone.keeper.android.components.extensions

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import kotlin.math.ceil

fun View.getCountThatFitsOnScreen(context: Context): Int {
    val deviceHeight = context.resources.displayMetrics.heightPixels
    val skeletonRowHeight = height
    return ceil((deviceHeight / skeletonRowHeight).toDouble()).toInt()
}

fun TextView.setTextColorRes(@ColorRes color: Int) {
    setTextColor(ContextCompat.getColor(context, color))
}