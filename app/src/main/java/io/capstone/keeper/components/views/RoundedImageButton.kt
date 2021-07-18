package io.capstone.keeper.components.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import io.capstone.keeper.R

class RoundedImageButton(
    context: Context,
    attributeSet: AttributeSet
): AppCompatImageButton(context, attributeSet) {

    private var cornerRadius = 16f
    private lateinit var rectF: RectF

    private val path = Path()

    init {
        val attributes = context.obtainStyledAttributes(attributeSet, R.styleable.RoundedImageButton)
        cornerRadius = attributes.getFloat(R.styleable.RoundedImageButton_containerRadius, 16f)
        attributes.recycle()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rectF = RectF(0f, 0f, w.toFloat(), h.toFloat())
        resetPath()
    }

    override fun draw(canvas: Canvas) {
        val save = canvas.save()
        canvas.clipPath(path)
        super.draw(canvas)
        canvas.restoreToCount(save)
    }

    override fun dispatchDraw(canvas: Canvas) {
        val save = canvas.save()
        canvas.clipPath(path)
        super.dispatchDraw(canvas)
        canvas.restoreToCount(save)
    }

    private fun resetPath() {
        path.reset()
        path.addRoundRect(rectF, cornerRadius, cornerRadius, Path.Direction.CW)
        path.close()
    }

}