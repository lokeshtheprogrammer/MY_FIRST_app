package com.example.nutrifill

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class NutritionHistoryAdapter(
    private val context: Context,
    private val historyList: List<NutritionHistoryItem>
) : RecyclerView.Adapter<NutritionHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foodName: TextView = view.findViewById(R.id.food_name)
        val calories: TextView = view.findViewById(R.id.food_calories)
        val macros: TextView = view.findViewById(R.id.food_macros)
        val timestamp: TextView = view.findViewById(R.id.food_timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_nutrition_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = historyList[position]

        holder.foodName.text = item.foodName
        holder.calories.text = "Calories: ${item.calories} kcal"
        holder.macros.text = "Carbs: ${item.carbs}g | Protein: ${item.protein}g | Fats: ${item.fats}g"
        holder.timestamp.text = item.timestamp

        // Alternate row colors for better UI
        val backgroundColor = if (position % 2 == 0) {
            ContextCompat.getColor(context, R.color.light_gray)
        } else {
            ContextCompat.getColor(context, R.color.white)
        }
        holder.itemView.setBackgroundColor(backgroundColor)
    }

    override fun getItemCount(): Int {
        return historyList.size
    }
}
