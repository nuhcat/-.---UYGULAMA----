package com.example.kantahliliuygulamasi

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class GradientTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatTextView(context, attrs, defStyle) {

    override fun onDraw(canvas: Canvas) {
        val paint = paint
        val width = measuredWidth.toFloat()

        val shader = LinearGradient(
            0f, 0f, width, textSize,
            intArrayOf(Color.parseColor("#FFFFFF"), Color.parseColor("#FFC1CC")),
            null,
            Shader.TileMode.CLAMP
        )
        paint.shader = shader
        super.onDraw(canvas)
    }
}
