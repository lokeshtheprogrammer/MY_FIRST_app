package com.example.nutrifill.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrifill.data.entity.FoodEntity
import com.example.nutrifill.databinding.ItemFoodBinding

class FoodAdapter(private val onFoodClick: (FoodEntity) -> Unit) : 
    ListAdapter<FoodEntity, FoodAdapter.FoodViewHolder>(FoodDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val binding = ItemFoodBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FoodViewHolder(private val binding: ItemFoodBinding) : 
        RecyclerView.ViewHolder(binding.root) {

        fun bind(food: FoodEntity) {
            binding.apply {
                foodName.text = food.name
                calories.text = "${food.calories} kcal"
                protein.text = "${food.protein}g"
                carbs.text = "${food.carbs}g"
                fats.text = "${food.fats}g"
                root.setOnClickListener { onFoodClick(food) }
            }
        }
    }

    private class FoodDiffCallback : DiffUtil.ItemCallback<FoodEntity>() {
        override fun areItemsTheSame(oldItem: FoodEntity, newItem: FoodEntity) = 
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: FoodEntity, newItem: FoodEntity) = 
            oldItem == newItem
    }
}