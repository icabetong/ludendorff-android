package io.capstone.keeper.components.views

import android.content.Context
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.content.ContextCompat
import io.capstone.keeper.R

class TwoLineCheckBox @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = R.attr.checkboxStyle
): AppCompatCheckBox(context, attributeSet, defStyleAttr) {

    private val titleSpan: TextAppearanceSpan
    private val subtitleSpan: TextAppearanceSpan
    private var titleTextColorSpan: ForegroundColorSpan
    private var subtitleTextColorSpan: ForegroundColorSpan

    var title: String = ""
        set(value) {
            field = value
            renderText()
        }

    var subtitle: String? = null
        set(value) {
            field = value
            renderText()
        }

    var titleTextColor: Int = ContextCompat.getColor(context, R.color.keeper_text_primary)
        set(value) {
            field = value
            renderText()
        }

    var subtitleTextColor: Int = ContextCompat.getColor(context, R.color.keeper_text_secondary)
        set(value) {
            field = value
            renderText()
        }

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.TwoLineCheckBox,
            defStyleAttr, 0)

        try {
            title = typedArray.getString(R.styleable.TwoLineCheckBox_titleText) ?: ""
            subtitle = typedArray.getString(R.styleable.TwoLineCheckBox_subtitleText)

            titleTextColor = typedArray.getColor(R.styleable.TwoLineCheckBox_titleTextColor,
                titleTextColor)
            subtitleTextColor = typedArray.getColor(R.styleable.TwoLineCheckBox_subtitleTextColor,
                subtitleTextColor)

            val titleTextAppearance = typedArray.getResourceId(
                R.styleable.TwoLineCheckBox_titleTextAppearance,
                R.style.TextAppearance_Keeper_Switch
            )
            titleSpan = TextAppearanceSpan(context, titleTextAppearance)
            titleTextColorSpan = ForegroundColorSpan(titleTextColor)

            val subtitleTextAppearance = typedArray.getResourceId(
                R.styleable.TwoLineCheckBox_subtitleTextAppearance,
                R.style.TextAppearance_Keeper_Caption
            )
            subtitleSpan = TextAppearanceSpan(context, subtitleTextAppearance)
            subtitleTextColorSpan = ForegroundColorSpan(subtitleTextColor)

        } finally {
            typedArray.recycle()
        }

        renderText()
    }

    private fun renderText() {
        titleTextColorSpan = ForegroundColorSpan(titleTextColor)
        subtitleTextColorSpan = ForegroundColorSpan(subtitleTextColor)

        val textToRender = if (subtitle.isNullOrEmpty()) title
        else "$title\n$subtitle"

        text = SpannableStringBuilder(textToRender).apply {
            setSpan(titleSpan, 0, title.length,
                SpannableString.SPAN_INCLUSIVE_EXCLUSIVE)
            setSpan(titleTextColorSpan, 0, title.length,
                SpannableString.SPAN_INCLUSIVE_EXCLUSIVE)

            subtitle?.also {
                if (it.isNotEmpty()) {
                    setSpan(subtitleSpan, title.length,
                        title.length + it.length + 1, SpannableString.SPAN_EXCLUSIVE_INCLUSIVE)
                    setSpan(subtitleTextColorSpan, title.length,
                        title.length + it.length + 1, SpannableString.SPAN_EXCLUSIVE_INCLUSIVE)
                }
            }
        }
    }

}