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

class FoodListFragment : Fragment() {
    private var _binding: FragmentFoodListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FoodViewModel by viewModels()
    private lateinit var foodAdapter: FoodAdapter

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
        setupRecyclerView()
        observeFoods()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        foodAdapter = FoodAdapter { food ->
            // Navigate to food detail screen
            findNavController().navigate(
                FoodListFragmentDirections.actionFoodListFragmentToFoodDetailFragment(food.id)
            )
        }
        binding.foodRecyclerView.apply {
            adapter = foodAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }
    }

    private fun setupClickListeners() {
        binding.addFoodButton.setOnClickListener {
            findNavController().navigate(
                FoodListFragmentDirections.actionFoodListFragmentToAddFoodFragment()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}