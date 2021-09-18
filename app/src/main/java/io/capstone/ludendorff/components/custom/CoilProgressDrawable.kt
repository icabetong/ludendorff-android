package io.capstone.ludendorff.components.custom

import android.content.Context
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import io.capstone.ludendorff.components.extensions.setColorFilterCompat

class CoilProgressDrawable(context: Context, @ColorRes id: Int): CircularProgressDrawable(context) {

    init {
        strokeWidth = 4f
        centerRadius = 24f
        setColorFilterCompat(ContextCompat.getColor(context, id))
        start()
    }

}