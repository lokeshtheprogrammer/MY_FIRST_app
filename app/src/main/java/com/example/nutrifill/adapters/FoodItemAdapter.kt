package com.example.nutrifill.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrifill.R
import com.example.nutrifill.model.FoodItem
import com.google.android.material.card.MaterialCardView

class FoodItemAdapter(
    private val onItemClick: (FoodItem) -> Unit
) : ListAdapter<FoodItem, FoodItemAdapter.FoodItemViewHolder>(FoodItemDiffCallback()) {

    class FoodItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: MaterialCardView = view.findViewById(R.id.card_view)
        val foodName: TextView = view.findViewById(R.id.tv_food_name)
        val calories: TextView = view.findViewById(R.id.tv_calories)
        val nutrients: TextView = view.findViewById(R.id.tv_nutrients)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food, parent, false)
        return FoodItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.foodName.text = item.name
        holder.calories.text = "${item.calories.toInt()} kcal"
        holder.nutrients.text = "P: ${item.protein}g | C: ${item.carbohydrates}g | F: ${item.fats}g"
        
        holder.cardView.setOnClickListener {
            onItemClick(item)
        }
    }

    private class FoodItemDiffCallback : DiffUtil.ItemCallback<FoodItem>() {
        override fun areItemsTheSame(oldItem: FoodItem, newItem: FoodItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FoodItem, newItem: FoodItem): Boolean {
            return oldItem == newItem
        }
    }

    fun updateItems(newItems: List<FoodItem>) {
        submitList(newItems)
    }
}