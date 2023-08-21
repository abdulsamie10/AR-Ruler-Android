package com.example.ar_ruler_abdulsamie.fragment

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.ar_ruler_abdulsamie.R
import com.example.ar_ruler_abdulsamie.databinding.FragmentBubbleLevelerBinding
import kotlin.math.atan2

class BubbleLevelerFragment : Fragment(R.layout.fragment_bubble_leveler), SensorEventListener {
    private lateinit var binding: FragmentBubbleLevelerBinding
    private val sensorManager by lazy { requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    private val gravitySensor by lazy { sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBubbleLevelerBinding.bind(view)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor != gravitySensor) return

        event?.let {
            val x = it.values[0].toDouble()
            val y = it.values[1].toDouble()
            val z = event.values[2].toDouble()
            val angleXZ = atan2(x,  z) * 180/Math.PI
            val angleYZ = atan2(y,  z) * 180/Math.PI

            binding.horizontalLevel.setCirclePoint(x, y)
            binding.verticalLevel.setCirclePoint(x, y)
            binding.centerLevel.setCirclePoint(x, y)

            binding.textView.text = String.format(
                getString(R.string.sensor_value),
                String.format("%.2f", angleXZ),
                String.format("%.2f", angleYZ)
            )
        }
    }
}
