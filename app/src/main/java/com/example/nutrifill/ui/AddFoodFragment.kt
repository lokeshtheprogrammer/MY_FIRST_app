package com.example.nutrifill.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.nutrifill.databinding.FragmentAddFoodBinding
import com.example.nutrifill.viewmodel.FoodViewModel

class AddFoodFragment : Fragment() {
    private var _binding: FragmentAddFoodBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FoodViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddFoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSaveButton()
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            val name = binding.foodNameInput.text.toString()
            val calories = binding.caloriesInput.text.toString().toDoubleOrNull()
            val protein = binding.proteinInput.text.toString().toDoubleOrNull()
            val carbs = binding.carbsInput.text.toString().toDoubleOrNull()
            val fats = binding.fatsInput.text.toString().toDoubleOrNull()

            if (name.isBlank() || calories == null || protein == null || carbs == null || fats == null) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.insertFood(name, calories, protein, carbs, fats)
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}