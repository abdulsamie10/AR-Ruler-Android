package com.example.ar_ruler_abdulsamie.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.ar_ruler_abdulsamie.R
import com.example.ar_ruler_abdulsamie.databinding.FragmentScreenrulerBinding

class screenrulerFragment : Fragment(R.layout.fragment_screenruler) {
    private lateinit var binding: FragmentScreenrulerBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentScreenrulerBinding.bind(view)

        val settings = MySettings(requireContext())
        binding.oneDimensionRulerView.coefficient = settings.rulerCoefficient

        binding.inchButtonText.setOnClickListener {
            binding.oneDimensionRulerView.setCustomValue(1)
            binding.inchButtonText.setBackgroundResource(R.drawable.selected_screenmeasure_button)
            binding.cmButtonText.setBackgroundResource(R.drawable.unselected_screenmeasure_button)
        }

        binding.cmButtonText.setOnClickListener {
            binding.oneDimensionRulerView.setCustomValue(2)
            binding.cmButtonText.setBackgroundResource(R.drawable.selected_screenmeasure_button)
            binding.inchButtonText.setBackgroundResource(R.drawable.unselected_screenmeasure_button)
        }

        binding.homeScreenButton.setOnClickListener {
            Navigation.findNavController(requireView()).navigate(R.id.action_screenrulerFragment_to_homeFragment)
        }
    }
}