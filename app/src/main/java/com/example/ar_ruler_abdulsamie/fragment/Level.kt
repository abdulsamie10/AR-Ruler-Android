package com.example.ar_ruler_abdulsamie.fragment

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.example.ar_ruler_abdulsamie.R

abstract class Level @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint()
    private val circleBitmap = resizeBitmap(getBitmap(R.drawable.circle), 0.2f)
    protected val circle by lazy { Circle(width, height, circleBitmap) }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.drawBitmap(
            circle.bitmap,
            circle.x.toFloat(),
            circle.y.toFloat(),
            paint
        )
    }

    private fun getBitmap(resource: Int) = BitmapFactory.decodeResource(context.resources, resource)

    private fun resizeBitmap(bitmap: Bitmap, ratio: Float) = Bitmap.createScaledBitmap(
        bitmap,
        (bitmap.width * ratio).toInt(),
        (bitmap.height * ratio).toInt(),
        false
    )

    abstract fun setCirclePoint(x: Double, y: Double)
}