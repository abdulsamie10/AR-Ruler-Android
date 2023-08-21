package com.example.ar_ruler_abdulsamie.fragment

import android.util.DisplayMetrics
import android.util.TypedValue

enum class RulerUnit(val converter: Float, val unit: String) {
    MM(25.4f, "mm"),
    CM(2.54f, "cm"),
    IN(1f   , "inches");

    companion object {

        fun mmToPx(mm: Float, coefficient: Float, displayMetrics: DisplayMetrics): Float {
            return mm * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, coefficient, displayMetrics)
        }

        fun pxToIn(px: Float, coefficient: Float, displayMetrics: DisplayMetrics): Float {
            return px / TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_IN, coefficient, displayMetrics)
        }

    }

    /**
     * @param value in IN
     */
    fun getUnitString(value: Float) = "${(value * converter).format(1)} $unit"

    /**
     * @param value in IN
     *
     * @return value in #unit
     */
    fun convert(value: Float) = value * converter
}

private fun Float.format(value: Int) = java.lang.String.format("%.${value}f", this)
