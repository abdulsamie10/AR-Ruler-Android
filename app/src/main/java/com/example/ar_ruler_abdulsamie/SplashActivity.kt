package com.example.ar_ruler_abdulsamie

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.example.ar_ruler_abdulsamie.adapter.CustomPagerAdapter
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator

class SplashActivity : AppCompatActivity() {
    private val imageResources = intArrayOf(
        R.drawable.pic1,
        R.drawable.pic2,
        R.drawable.pic3,
        R.drawable.pic4
    )

    private lateinit var viewPager: ViewPager
    private lateinit var dotsIndicator: DotsIndicator
    private lateinit var continueButton: TextView
    private lateinit var progressBar: ProgressBar
    private var currentImageIndex = 0
    private var isAutoSlideEnabled = true
    private val autoSlideInterval = 2000L // 2 seconds
    private val sharedPreferencesKey = "APP_FIRST_TIME"
    private val splashScreenDuration = 500L // 15 seconds
    private lateinit var textShader: Shader

    private val handler = Handler(Looper.getMainLooper())
    private val autoSlideRunnable = object : Runnable {
        override fun run() {
            if (isAutoSlideEnabled) {
                currentImageIndex = (currentImageIndex + 1) % imageResources.size
                isManualSlide = false // Reset the flag since this is an auto-slide
                viewPager.currentItem = currentImageIndex // Update ViewPager immediately
                handler.postDelayed(this, autoSlideInterval)
            }
        }
    }

    private var isManualSlide = false // Track whether the slide is manual

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        viewPager = findViewById(R.id.viewPager)
        continueButton = findViewById(R.id.continueButton)
        progressBar = findViewById(R.id.progressBar)
        dotsIndicator = findViewById(R.id.dots_indicator)

        currentImageIndex = 0 // Initialize currentImageIndex to 0

        val isFirstTime = getSharedPreferences(sharedPreferencesKey, Context.MODE_PRIVATE)
            .getBoolean(sharedPreferencesKey, true)

        val pageViews: ArrayList<View> = ArrayList()
        val inflater = LayoutInflater.from(this)

        for (i in imageResources.indices) {
            val view = inflater.inflate(R.layout.page_layout, null)
            val imageView: ImageView = view.findViewById(R.id.imageView)
            val eclipseView: ImageView = view.findViewById(R.id.eclipseImage)
            val textView: TextView = view.findViewById(R.id.titleFeature)
            val firstText: TextView = view.findViewById(R.id.StartingText)
            val secondText: TextView = view.findViewById(R.id.FirstTextDescription)
            val featureDescription: TextView = view.findViewById(R.id.descriptionFeature)

            val paint = firstText.paint
            val width = paint.measureText(firstText.text.toString())
            val themeGradientColors = intArrayOf(
                ContextCompat.getColor(this, R.color.ThemeGradientStart),
                ContextCompat.getColor(this, R.color.ThemeGradientEnd)
            )
            textShader = LinearGradient(0f, 0f, width, textView.textSize, themeGradientColors, null, Shader.TileMode.REPEAT)

            firstText.paint.setShader(textShader)
            eclipseView.visibility = if (i == 0) View.VISIBLE else View.GONE
            firstText.visibility = if (i == 0) View.VISIBLE else View.GONE
            secondText.visibility = if (i == 0) View.VISIBLE else View.GONE

            // Reduce the size of the first image
            if (i == 0) {
                val imageSizeInDp = 265 // Set the desired image size in dp
                val imageSizeInPixels = (imageSizeInDp * resources.displayMetrics.density).toInt()
                imageView.layoutParams.width = imageSizeInPixels
                imageView.layoutParams.height = imageSizeInPixels
            }

            imageView.setImageResource(imageResources[i])
            when (i) {
                0 -> {
                    textView.text = getString(R.string.ar_ruler_title)
                    featureDescription.text = getString(R.string.ar_ruler_description)
                }
                1 -> {
                    textView.text = getString(R.string.camera_measure_title)
                    featureDescription.text = getString(R.string.camera_measure_description)
                }
                2 -> {
                    textView.text = getString(R.string.screen_ruler_title)
                    featureDescription.text = getString(R.string.screen_ruler_description)
                }
                3 -> {
                    textView.text = getString(R.string.bubble_level_title)
                    featureDescription.text = getString(R.string.bubble_level_description)
                }
                else -> {
                    textView.text = getString(R.string.ar_ruler_title)
                    featureDescription.text = getString(R.string.ar_ruler_description)
                }
            }

            if (i == 0) {
                val params = imageView.layoutParams as ViewGroup.MarginLayoutParams
                val topMarginInDp = 165 // Set the desired top margin in dp
                val topMarginInPixels = (topMarginInDp * resources.displayMetrics.density).toInt()
                params.setMargins(0, topMarginInPixels, 50, 0)
                imageView.layoutParams = params
            }

            pageViews.add(view)
        }

        viewPager.adapter = CustomPagerAdapter(pageViews)
        dotsIndicator.attachTo(viewPager)

        if (isFirstTime) {
            continueButton.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
            continueButton.setOnClickListener {
                getSharedPreferences(sharedPreferencesKey, Context.MODE_PRIVATE)
                    .edit().putBoolean(sharedPreferencesKey, false).apply()
                moveToMainActivity()
            }
        } else {
            continueButton.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            handler.postDelayed({ moveToMainActivity() }, splashScreenDuration)
        }

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                currentImageIndex = position
                isManualSlide = true
                handler.removeCallbacks(autoSlideRunnable)
            }

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager.SCROLL_STATE_DRAGGING || state == ViewPager.SCROLL_STATE_SETTLING) {
                    isManualSlide = true
                    handler.removeCallbacks(autoSlideRunnable)
                    isAutoSlideEnabled = false
                } else if (state == ViewPager.SCROLL_STATE_IDLE && isManualSlide) {
                    isAutoSlideEnabled = true
                    handler.postDelayed(autoSlideRunnable, autoSlideInterval)
                    isManualSlide = false
                }
            }
        })

        handler.postDelayed(autoSlideRunnable, autoSlideInterval)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(autoSlideRunnable)
        handler.removeCallbacksAndMessages(null)
    }

    private fun moveToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}