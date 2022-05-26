package io.capstone.ludendorff.components.custom.preference

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.core.content.res.TypedArrayUtils
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import io.capstone.ludendorff.R

@SuppressLint("RestrictedApi")
class ImagePreference(context: Context, private val attr: AttributeSet?, defStyleAttr: Int, defStyleRes: Int):
    Preference(context, attr, defStyleAttr, defStyleRes) {

    private var image: Drawable? = null

    constructor(context: Context, attr: AttributeSet?, defStyleAttr: Int):
            this(context, attr, defStyleAttr, 0)
    constructor(context: Context, attr: AttributeSet?):
            this(context, attr, TypedArrayUtils.getAttr(context,
                androidx.preference.R.attr.preferenceStyle, android.R.attr.preferenceStyle))
    constructor(context: Context): this(context, null)

    init {
        layoutResource = R.layout.layout_preference_image
        val typedArray = context.obtainStyledAttributes(attr,
            R.styleable.io_capstone_ludendorff_components_preference_ImagePreference)
        image = typedArray.getDrawable(R.styleable.io_capstone_ludendorff_components_preference_ImagePreference_srcVector)
        typedArray.recycle()
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        holder.itemView.isClickable = false
        holder.itemView.isFocusable = false
        val imageView = holder.findViewById(R.id.imageView) as ImageView
        if (image != null) {
            imageView.setImageDrawable(image)
        }
    }

    override fun onClick() {}
    override fun performClick() {}
    override fun performClick(view: View) {}
}