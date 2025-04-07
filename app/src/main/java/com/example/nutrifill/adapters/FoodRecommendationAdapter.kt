package com.example.nutrifill.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrifill.R
import com.example.nutrifill.models.FoodRecommendation

class FoodRecommendationAdapter(private val onItemClick: (FoodRecommendation) -> Unit) :
    ListAdapter<FoodRecommendation, FoodRecommendationAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foodName: TextView = view.findViewById(R.id.tv_food_name)
        val foodDescription: TextView = view.findViewById(R.id.tv_nutrient_content)
        val price: TextView = view.findViewById(R.id.tv_price)
        val foodImage: ImageView = view.findViewById(R.id.iv_food_image)
        val servingSuggestion: TextView = view.findViewById(R.id.tv_serving_suggestion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food_recommendation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recommendation = getItem(position)
        holder.foodName.text = recommendation.name
        holder.foodDescription.text = recommendation.description
        holder.price.text = recommendation.price
        holder.foodImage.setImageResource(recommendation.imageResource)
        holder.servingSuggestion.text = recommendation.portion
        holder.itemView.setOnClickListener { onItemClick(recommendation) }
    }

    class DiffCallback : DiffUtil.ItemCallback<FoodRecommendation>() {
        override fun areItemsTheSame(oldItem: FoodRecommendation, newItem: FoodRecommendation): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: FoodRecommendation, newItem: FoodRecommendation): Boolean {
            return oldItem == newItem
        }
    }
}