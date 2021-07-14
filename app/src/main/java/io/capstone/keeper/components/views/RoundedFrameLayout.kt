package io.capstone.keeper.components.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.FrameLayout
import io.capstone.keeper.R

class RoundedFrameLayout(context: Context, attrs: AttributeSet): FrameLayout(context, attrs) {

    private var cornerRadius = 16f
    private lateinit var rectF: RectF

    private val path = Path()

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.RoundedFrameLayout)
        cornerRadius = attributes.getFloat(R.styleable.RoundedFrameLayout_layoutCornerRadius, 16f)
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