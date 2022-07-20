package com.skydoves.colorpickerview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class RingGlowView : View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    private val paintGlow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val paintOutline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
    }
    private var centerX = -1f
    private var centerY = -1f

    var glowColor: Int = Color.WHITE
        set(value) {
            if (field != value) {
                field = value
                paintGlow.colorFilter = PorterDuffColorFilter(value, PorterDuff.Mode.SRC_IN)
                paintOutline.colorFilter = PorterDuffColorFilter(value, PorterDuff.Mode.SRC_IN)
                invalidate()
            }
        }

    private val glowExtentDp = 12
    private val strokeWidthDp = 1

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f

        val glowExtent = SizeUtils.dp2Px(context, glowExtentDp)

        if (this.centerX != centerX || this.centerY != centerY) {
            this.centerX = centerX
            this.centerY = centerY

            val radius = centerX
            val colors = intArrayOf(Color.TRANSPARENT, Color.TRANSPARENT, 0x80ffffff.toInt(), Color.TRANSPARENT)

            val stopStart = 1f - (4f * glowExtent) / width
            val stopCenter = 1f - (2f * glowExtent) / width

            val stops = floatArrayOf(0f, stopStart, stopCenter, 1f)
            paintGlow.shader = RadialGradient(centerX, centerY, radius, colors, stops, Shader.TileMode.CLAMP)
        }

        canvas.drawCircle(centerX, centerY, centerX, paintGlow)

        paintOutline.strokeWidth = SizeUtils.dp2Px(context, strokeWidthDp).toFloat()
        canvas.drawCircle(centerX, centerX, (width - 2 * glowExtent) / 2f, paintOutline)
    }

}
