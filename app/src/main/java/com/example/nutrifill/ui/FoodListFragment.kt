package com.example.nutrifill.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.nutrifill.R
import com.example.nutrifill.databinding.FragmentFoodListBinding
import com.example.nutrifill.viewmodel.FoodViewModel
import com.example.nutrifill.ui.adapter.FoodAdapter
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

class FoodListFragment : Fragment() {
    private var _binding: FragmentFoodListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FoodViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoodListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.foods.observe(viewLifecycleOwner) { foods ->
            // Update your RecyclerView adapter here
        }
    }

    private fun navigateToFoodDetail(foodId: Long) {
        val action = FoodListFragmentDirections.actionFoodListToDetail(foodId)
        findNavController().navigate(action)
    }

    private fun navigateToAddFood() {
        val action = FoodListFragmentDirections.actionFoodListToAdd()
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}