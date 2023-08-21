package com.example.ar_ruler_abdulsamie.fragment

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Parcelable
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.example.ar_ruler_abdulsamie.R

class OneDimensionRulerView : View {
    private var customValue: Int = 0
    var heightText : String = ""

    fun setCustomValue(value: Int) {
        customValue = value
        unit = if (customValue == 1) RulerUnit.IN else RulerUnit.CM
        invalidate() // Trigger a redraw if necessary
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val availableWidth = MeasureSpec.getSize(widthMeasureSpec)
        val newWidth = (availableWidth * 0.85).toInt() // Adjust the scaling factor as needed

        val availableHeight = MeasureSpec.getSize(heightMeasureSpec)
        val newHeight = (availableHeight * 0.7).toInt() // Adjust the scaling factor as needed

        setMeasuredDimension(MeasureSpec.makeMeasureSpec(newWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.EXACTLY))
    }


    companion object {
        const val UpperLeftSection = 1
        const val UpperRightSection = 2
        const val LowerLeftSection = 3
        const val LowerRightSection = 4
    }

    private var drawCallback: (() -> Unit)? = null

    fun setDrawCallback(callback: () -> Unit) {
        drawCallback = callback
    }

    private val colorPaintMask = Paint(Paint.ANTI_ALIAS_FLAG)
    private val grayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val grayPaintReplace: Paint
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val textPaintReplace: TextPaint

    private var upperY : Float = 500f
    private var lowerY : Float = 1000f
    private var leftX : Float = 300f
    private var rightX : Float = 800f

    private val minDistance = dpTOpx(1f)

    var markCmWidth = dpTOpx(20f)
        set(value) {
            field = value
            invalidate()
        }
    var markHalfCmWidth = dpTOpx(15f)
        set(value) {
            field = value
            invalidate()
        }
    var markMmWidth = dpTOpx(10f)
        set(value) {
            field = value
            invalidate()
        }

    private var currentSection = 0
    private var pointerY = 0f
    private var pointerX = 0f

    var unit: RulerUnit = RulerUnit.IN
        set(value) {
            field = when (customValue) {
                1 -> RulerUnit.IN
                2 -> RulerUnit.CM
                else -> value
            }
            invalidate()
        }

    var coefficient = 1f
        set(value) {
            field = value
            invalidate()
        }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    init {
        colorPaintMask.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

        grayPaint.color = Color.DKGRAY
        grayPaintReplace = Paint(grayPaint)
        grayPaintReplace.color = Color.WHITE
        grayPaintReplace.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OVER)

        textPaint.textSize = dpTOpx(18f)
        textPaint.color = context.resources.getColor(R.color.black)
        textPaint.textAlign = Paint.Align.CENTER
        textPaintReplace = TextPaint(textPaint)
        textPaintReplace.color = Color.WHITE
        textPaintReplace.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OVER)
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        //drawMarks(canvas, grayPaint)

        canvas?.drawText(unit.getUnitString(RulerUnit.pxToIn(Math.abs(upperY - lowerY), coefficient, resources.displayMetrics))
                , width*.7f, height/2f, textPaint)

        //canvas?.drawText(unit.getUnitString(RulerUnit.pxToIn(Math.abs(rightX - leftX), coefficient, resources.displayMetrics)), width*.5f, 130f, textPaint)

        canvas?.drawText(
            unit.getUnitString(RulerUnit.pxToIn(Math.abs(rightX - leftX), coefficient, resources.displayMetrics)),
            width * 0.5f,
            130f,
            textPaint
        )
        heightText = unit.getUnitString(RulerUnit.pxToIn(Math.abs(rightX - leftX), coefficient, resources.displayMetrics))


        canvas?.drawText(heightText, width * 0.5f, 130f, textPaint)

        canvas?.drawRect(0f, 0f, width.toFloat(), upperY, colorPaintMask)
        canvas?.drawRect(0f, lowerY, width.toFloat(), height.toFloat(), colorPaintMask)



        canvas?.drawRect(0f, upperY, leftX, lowerY, colorPaintMask)
        canvas?.drawRect(rightX, upperY, width.toFloat(),lowerY, colorPaintMask)

        //drawMarks(canvas, grayPaintReplace)

        canvas?.drawText(unit.getUnitString(RulerUnit.pxToIn(Math.abs(upperY - lowerY), coefficient, resources.displayMetrics)) , width*.7f, height/2f , textPaintReplace)
        canvas?.drawText(unit.getUnitString(RulerUnit.pxToIn(Math.abs(rightX - leftX), coefficient, resources.displayMetrics)), width*.5f, 130f , textPaintReplace)
        // Call the draw callback after drawing
        drawCallback?.invoke()
    }

    private fun drawMarks(canvas: Canvas?, paint: Paint) {
        val oneMmInPx = RulerUnit.mmToPx(1f, coefficient, resources.displayMetrics)
        for (i in 1..1000) {
            val y = oneMmInPx * i
            val x = oneMmInPx * i
            val markWidth = when {
                i % 10 == 0 -> markCmWidth
                i % 5 == 0 -> markHalfCmWidth
                else -> markMmWidth
            }

            canvas?.drawLine(width.toFloat(), y, width - markWidth, y, paint)

            canvas?.drawLine(x, 0f, x, markWidth, paint)

            if (y >= height || x >= width)
                break
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val centerPointX = (rightX + leftX) / 2
                val centerPointY = (lowerY + upperY) / 2

                currentSection = when {
                    event.x < centerPointX && event.y < centerPointY -> UpperLeftSection
                    event.x > centerPointX && event.y < centerPointY -> UpperRightSection
                    event.x < centerPointX && event.y > centerPointY -> LowerLeftSection
                    event.x > centerPointX && event.y > centerPointY -> LowerRightSection
                    else -> 0
                }

                pointerX = event.x
                pointerY = event.y

                return currentSection != 0
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - pointerX
                val dy = event.y - pointerY

                val leftBound = width * 0.005f
                val rightBound = width - width * 0.005f
                val upperBound = height * 0.001f
                val lowerBound = height - height * 0.001f

                when (currentSection) {
                    UpperLeftSection -> {
                        // Apply left bound restriction
                        leftX += dx
                        leftX = Math.max(leftBound, Math.min(rightX - minDistance, leftX))

                        // Apply upper bound restriction
                        upperY += dy
                        upperY = Math.max(upperBound, Math.min(lowerY - minDistance, upperY))
                    }
                    UpperRightSection -> {
                        // Apply right bound restriction
                        rightX += dx
                        rightX = Math.max(leftX + minDistance, Math.min(rightBound, rightX))

                        // Apply upper bound restriction
                        upperY += dy
                        upperY = Math.max(upperBound, Math.min(lowerY - minDistance, upperY))
                    }
                    LowerLeftSection -> {
                        // Apply left bound restriction
                        leftX += dx
                        leftX = Math.max(leftBound, Math.min(rightX - minDistance, leftX))

                        // Apply lower bound restriction
                        lowerY += dy
                        lowerY = Math.max(upperY + minDistance, Math.min(lowerBound, lowerY))
                    }
                    LowerRightSection -> {
                        // Apply right bound restriction
                        rightX += dx
                        rightX = Math.max(leftX + minDistance, Math.min(rightBound, rightX))

                        // Apply lower bound restriction
                        lowerY += dy
                        lowerY = Math.max(upperY + minDistance, Math.min(lowerBound, lowerY))
                    }
                }
                pointerX = event.x
                pointerY = event.y

                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                return false
            }
        }
        return false
    }


    fun getDistanceY () = unit.convert(Math.abs(upperY - lowerY))

    override fun onSaveInstanceState(): Parcelable? {
        super.onSaveInstanceState()
        val bundle = Bundle()
        bundle.putParcelable("superState", super.onSaveInstanceState())
        bundle.putFloat("coefficient", coefficient)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var _state = state
        val bundle = _state as Bundle
        coefficient = bundle.getFloat("coefficient")
        _state = bundle.getParcelable("superState")
        super.onRestoreInstanceState(_state)
    }

    private fun dpTOpx(dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }
}