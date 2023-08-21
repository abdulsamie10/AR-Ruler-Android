package com.example.ar_ruler_abdulsamie.fragment

import android.graphics.Bitmap

class Circle(
    private val width: Int,
    private val height: Int,
    val bitmap: Bitmap
) {
    var x: Double = 0.0
    var y: Double = 0.0

    fun setPoint(x: Double, y: Double) {
        this.x = width / 2 + (x * width / 9.8 / 2) - bitmap.width / 2
        this.y = height / 2 - (y * height / 9.8 / 2) - bitmap.height / 2
    }
}