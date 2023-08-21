package com.example.ar_ruler_abdulsamie.fragment

import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Shader
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.*
import com.example.ar_ruler_abdulsamie.R

class PickerAdapter(
    private val context: Context,
    private var dataList: List<String>,
    private val recyclerView: RecyclerView?,
    private var selectedItemPosition: Int = 0 // Set to 0 initially
) : Adapter<PickerAdapter.TextVH>() {

    private lateinit var gradientColors : IntArray
    private var selectePos : Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextVH {
        val view: View
        val inflater = LayoutInflater.from(context)
        view = inflater.inflate(R.layout.picker_item_layout, parent, false)
        return TextVH(view)
    }

    override fun onBindViewHolder(holder: TextVH, position: Int) {
        holder.bind(dataList[position], position)
        holder.pickerTxt.setOnClickListener { recyclerView?.smoothScrollToPosition(position) }
    }

    fun setSelectedItemPosition(position: Int) {
        selectedItemPosition = position
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun swapData(newData: List<String>) {
        dataList = newData
        notifyDataSetChanged()
    }

    inner class TextVH(itemView: View) : ViewHolder(itemView) {
        var pickerTxt: TextView = itemView.findViewById(R.id.picker_item) as TextView

        fun bind(item: String, position: Int) {
            pickerTxt.text = item

            val selectedColor = ContextCompat.getColor(context, R.color.selected_color)
            val defaultColor = ContextCompat.getColor(context, R.color.default_color)
            val fadingColor = ContextCompat.getColor(context, R.color.fading_color)

            if (position == selectedItemPosition) {
                // Set color for the selected item
                pickerTxt.setTextColor(selectedColor)
                pickerTxt.paint.shader = null // Remove the gradient shader
                // Reset scaling and alpha properties
                itemView.scaleX = 1.0f
                itemView.scaleY = 1.0f
                itemView.alpha = 1.0f
                selectePos = position
            } else {
                if(position + 2 == selectePos || position - 2 == selectePos){
                    itemView.scaleX = 0.0f
                    itemView.scaleY = 0.0f
                    itemView.alpha = 0.0f
                }else{
                    // Set color for unselected items
                    pickerTxt.setTextColor(defaultColor)

                    // Reset scaling and alpha properties
                    itemView.scaleX = 0.7f
                    itemView.scaleY = 0.7f
                    //itemView.alpha = 0.7f

                    gradientColors = if(position + 1 == selectePos){
                        intArrayOf(Color.TRANSPARENT, defaultColor)
                    }else{
                        intArrayOf(defaultColor, Color.TRANSPARENT)
                    }
                    val gradient = LinearGradient(
                        0.5f, 0.5f, pickerTxt.width.toFloat(), 0.2f,
                        gradientColors, null, Shader.TileMode.CLAMP
                    )
                    pickerTxt.paint.shader = gradient

                    itemView.alpha = 0.7f // Reset the alpha to 1 for non-selected items
                }
            }
        }
    }
}