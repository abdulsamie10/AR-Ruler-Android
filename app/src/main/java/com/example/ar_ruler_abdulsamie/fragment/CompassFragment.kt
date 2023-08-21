package com.example.ar_ruler_abdulsamie.fragment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.ar_ruler_abdulsamie.R
import com.example.ar_ruler_abdulsamie.databinding.FragmentCompassBinding

class CompassFragment : Fragment(R.layout.fragment_compass), SensorEventListener, LocationListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var magnetometer: Sensor

    private lateinit var directionTextView: TextView

    private lateinit var locationManager: LocationManager
    private var currentLocation: Location? = null

    // Initialize accelerometer and magnetometer readings
    private var accelerometerReading = FloatArray(3)
    private var magnetometerReading = FloatArray(3)

    private val LOCATION_PERMISSION_REQUEST_CODE = 123

    lateinit var binding: FragmentCompassBinding
    private lateinit var compassNeedle: ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentCompassBinding.bind(view)
        setHasOptionsMenu(true)

        directionTextView = binding.directionTextView
        compassNeedle = binding.compassNeedle

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL
        )
        sensorManager.registerListener(
            this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL
        )

        if (!hasLocationPermissions()) {
            requestLocationPermission()
        } else {
            requestLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        stopLocationUpdates()
    }

    private fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            // You can show a rationale for requesting the location permission
            // Explain to the user why this permission is needed
            // For example, using a dialog explaining the purpose of the location access.
            // Once the user acknowledges, call `requestPermission()`
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates()
            } else {
                // Location permission denied. You may handle this case accordingly.
                // For example, show a dialog explaining why the permission is required
                // and ask the user to grant it again or open settings for the user to enable it manually.
            }
        }
    }

    private fun requestLocationUpdates() {
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L, // Minimum time interval in milliseconds for updates
                1.0f, // Minimum distance interval in meters for updates
                this
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun stopLocationUpdates() {
        locationManager.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location) {
        currentLocation = location
        updateCompassDirection()
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Not used in this example
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            accelerometerReading = event.values
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            magnetometerReading = event.values
        }
        updateCompassDirection()
    }

    private fun updateCompassDirection() {
        val rotationMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)

        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()

        val direction = getDirection(azimuth)
        compassNeedle.rotation = -azimuth

        val locationText = currentLocation?.let {
            "Lat: ${it.latitude}\nLong: ${it.longitude}\n"
        } ?: "Location: Unknown\n"

        directionTextView.text = "$locationText Direction: $direction\nAzimuth: $azimuth°"
    }

    private fun getDirection(azimuth: Float): String {
        if (currentLocation == null) return "Unknown"

        val geoField = GeomagneticField(
            currentLocation!!.latitude.toFloat(),
            currentLocation!!.longitude.toFloat(),
            currentLocation!!.altitude.toFloat(),
            System.currentTimeMillis()
        )
        val correctedAzimuth = azimuth + geoField.declination
        return when {
            correctedAzimuth >= -22.5 && correctedAzimuth < 22.5 -> "North"
            correctedAzimuth >= 22.5 && correctedAzimuth < 67.5 -> "Northeast"
            correctedAzimuth >= 67.5 && correctedAzimuth < 112.5 -> "East"
            correctedAzimuth >= 112.5 && correctedAzimuth < 157.5 -> "Southeast"
            correctedAzimuth >= 157.5 || correctedAzimuth < -157.5 -> "South"
            correctedAzimuth >= -157.5 && correctedAzimuth < -112.5 -> "Southwest"
            correctedAzimuth >= -112.5 && correctedAzimuth < -67.5 -> "West"
            correctedAzimuth >= -67.5 && correctedAzimuth < -22.5 -> "Northwest"
            else -> "Unknown"
        }
    }
}
