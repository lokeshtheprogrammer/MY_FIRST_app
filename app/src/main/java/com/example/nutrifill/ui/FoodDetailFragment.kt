package com.example.nutrifill.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.nutrifill.databinding.FragmentFoodDetailBinding
import com.example.nutrifill.viewmodel.FoodViewModel
import kotlinx.coroutines.launch
import com.example.nutrifill.models.FoodDetails

class FoodDetailFragment : Fragment() {
    private var _binding: FragmentFoodDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FoodViewModel by viewModels()
    private val args: FoodDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoodDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadFoodDetails()
    }

    private fun loadFoodDetails() {
        viewLifecycleOwner.lifecycleScope.launch {
            val food = viewModel.getFoodById(args.foodId)
            food?.let {
                binding.foodNameText.text = it.name
                binding.caloriesText.text = "${it.calories} kcal"
                binding.nutrientsText.text = """
                    Protein: ${it.protein}g
                    Carbs: ${it.carbs}g
                    Fats: ${it.fats}g
                """.trimIndent()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}