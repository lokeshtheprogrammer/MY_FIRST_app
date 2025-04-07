package com.example.nutrifill

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrifill.models.ScanHistoryItem
import java.text.SimpleDateFormat
import java.util.*

class NutritionHistoryAdapter : RecyclerView.Adapter<NutritionHistoryAdapter.ViewHolder>() {
    private var items: List<ScanHistoryItem> = emptyList()

    fun updateItems(newItems: List<ScanHistoryItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_nutrition_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val foodName: TextView = itemView.findViewById(R.id.food_name)
        private val foodCalories: TextView = itemView.findViewById(R.id.food_calories)
        private val foodMacros: TextView = itemView.findViewById(R.id.food_macros)
        private val foodTimestamp: TextView = itemView.findViewById(R.id.food_timestamp)

        fun bind(item: ScanHistoryItem) {
            foodName.text = item.foodName
            foodCalories.text = "${item.nutrients.calories} kcal"
            foodMacros.text = "Carbs: ${item.nutrients.carbs}g | Protein: ${item.nutrients.protein}g | Fats: ${item.nutrients.fat}g"
            
            // Format timestamp
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val date = Date(item.timestamp)
            foodTimestamp.text = dateFormat.format(date)
        }
    }
}
