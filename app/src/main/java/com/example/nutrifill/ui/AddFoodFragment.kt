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
import com.example.nutrifill.model.FoodDetails

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupListeners()
        observeViewModel()
    }
    
    private fun setupListeners() {
        binding.saveButton.setOnClickListener {
            val foodName = binding.foodNameInput.text.toString()
            if (foodName.isNotEmpty()) {
                // Call ViewModel to save food
                viewModel.addFood(foodName)
            } else {
                // Show error
                binding.foodNameLayout.error = "Food name cannot be empty"
            }
        }
    }
    
    private fun observeViewModel() {
        viewModel.foodSaved.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Food saved successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), "Failed to save food", Toast.LENGTH_SHORT).show()
            }
        }
    }
}