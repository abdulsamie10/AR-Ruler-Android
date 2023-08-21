package com.example.ar_ruler_abdulsamie.fragment

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.ar_ruler_abdulsamie.R
import com.example.ar_ruler_abdulsamie.databinding.FragmentHomeBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlin.properties.Delegates

class HomeFragment : Fragment() {
    lateinit var binding: FragmentHomeBinding
    lateinit var cardView: CardView
    private var startColor by Delegates.notNull<Int>()
    private var endColor by Delegates.notNull<Int>()
    lateinit var gradientDrawable: GradientDrawable
    var camerapermission: Boolean = false
    var locationpermission: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        val cornerRadius = resources.getDimension(R.dimen.margin_15dp) // Replace with your desired corner radius value
        //Everything here
        //Gradient for AR Measure Card
        cardView = binding.arMeasureCard
        startColor = Color.parseColor("#087418")
        endColor = Color.parseColor("#2BCD62")
        gradientDrawable = createGradientDrawable(startColor, endColor, cornerRadius)
        cardView.background = gradientDrawable

        //Gradient for Screen Ruler Card
        cardView = binding.screenRulerCard
        startColor = Color.parseColor("#9220EC")
        endColor = Color.parseColor("#3E7CF3")
        gradientDrawable = createGradientDrawable(startColor, endColor, cornerRadius)
        cardView.background = gradientDrawable

        //Gradient for Bubble Leveler Card
        cardView = binding.bubbleLevelCard
        startColor = Color.parseColor("#FF6B00")
        endColor = Color.parseColor("#DB9F04")
        gradientDrawable = createGradientDrawable(startColor, endColor, cornerRadius)
        cardView.background = gradientDrawable

        //Gradient for Compass Card
        cardView = binding.compassCard
        startColor = Color.parseColor("#1956F3")
        endColor = Color.parseColor("#20D2C8")
        gradientDrawable = createGradientDrawable(startColor, endColor, cornerRadius)
        cardView.background = gradientDrawable

        //Gradient for Compass Card
        cardView = binding.arRulerProCard
        startColor = Color.parseColor("#17BBD5")
        endColor = Color.parseColor("#00DCFF")
        gradientDrawable = createGradientDrawable(startColor, endColor, cornerRadius)
        cardView.background = gradientDrawable

        // Handle fragment onBackPressed
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val bottomSheet: BottomSheetDialog = BottomSheetDialog(requireContext())
                bottomSheet.setContentView(R.layout.exit_dialogue)

                val textViewYes = bottomSheet.findViewById<TextView>(R.id.dialog_yes)
                val textViewNo = bottomSheet.findViewById<TextView>(R.id.dialog_no)

                textViewYes?.setOnClickListener {
                    bottomSheet.dismiss()
                    isEnabled = false
                    requireActivity().finishAffinity() // Close the application
                }

                textViewNo?.setOnClickListener {
                    bottomSheet.dismiss()
                }
                bottomSheet.show()
            }
        })

        binding.arMeasureCard.setOnClickListener {
            // Check if the dialog should be shown
            if (shouldShowARMeasureDialog()) {
                val bottomSheet: BottomSheetDialog = BottomSheetDialog(requireContext())
                bottomSheet.setContentView(R.layout.camera_permission_dialogue)
                val textViewYes = bottomSheet.findViewById<TextView>(R.id.dialog_yes)
                val textViewNo = bottomSheet.findViewById<TextView>(R.id.dialog_no)

                textViewYes?.setOnClickListener {
                    camerapermission = true
                    // Save the checkbox state in SharedPreferences
                    val preferences = requireActivity().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
                    val editor = preferences.edit()
                    val checkBox = bottomSheet.findViewById<CheckBox>(R.id.checkBox)
                    if (checkBox?.isChecked == true) {
                        editor.putBoolean("ar_measure_dialog_shown", true)
                    }
                    editor.apply()
                    bottomSheet.dismiss()
                    Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_ARRulerFragment)
                }

                textViewNo?.setOnClickListener {
                    camerapermission = false
                    bottomSheet.dismiss()
                }
                bottomSheet.show()
            } else {
                // If the checkbox is checked, directly navigate to the AR Measure screen
                Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_ARRulerFragment)
            }
        }

        binding.compassCard.setOnClickListener {
            // Check if the dialog should be shown
            if (shouldShowCompassDialog()) {
                val bottomSheet: BottomSheetDialog = BottomSheetDialog(requireContext())
                bottomSheet.setContentView(R.layout.compass_dialogue)
                val textViewYes = bottomSheet.findViewById<TextView>(R.id.dialog_yes)
                val textViewNo = bottomSheet.findViewById<TextView>(R.id.dialog_no)

                textViewYes?.setOnClickListener {
                    locationpermission = true

                    // Save the checkbox state in SharedPreferences
                    val preferences = requireActivity().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
                    val editor = preferences.edit()
                    val checkBox = bottomSheet.findViewById<CheckBox>(R.id.checkBox)
                    if (checkBox?.isChecked == true) {
                        editor.putBoolean("compass_dialog_shown", true)
                    }
                    editor.apply()

                    bottomSheet.dismiss()
                    Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_compassFragment)
                }

                textViewNo?.setOnClickListener {
                    locationpermission = false
                    bottomSheet.dismiss()
                }
                bottomSheet.show()
            } else {
                // If the checkbox is checked, directly navigate to the compass screen
                Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_compassFragment)
            }
        }

        binding.screenRulerCard.setOnClickListener {
            Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_screenrulerFragment)
        }

        binding.bubbleLevelCard.setOnClickListener {
            Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_bubbleLevelerFragment)
        }

        return binding.root
    }



    private fun createGradientDrawable(startColor: Int, endColor: Int, cornerRadius: Float): GradientDrawable {
        val gradientDrawable = GradientDrawable()
        gradientDrawable.orientation = GradientDrawable.Orientation.LEFT_RIGHT
        gradientDrawable.colors = intArrayOf(startColor, endColor)
        gradientDrawable.cornerRadius = cornerRadius
        return gradientDrawable
    }

    private fun shouldShowARMeasureDialog(): Boolean {
        val preferences = requireActivity().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        return !preferences.getBoolean("ar_measure_dialog_shown", false)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun shouldShowCompassDialog(): Boolean {
        val preferences = requireActivity().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        return !preferences.getBoolean("compass_dialog_shown", false)
    }
}