package com.example.nutrifill.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrifill.R
import com.example.nutrifill.models.ScanHistoryItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil

class ScanHistoryAdapter : ListAdapter<ScanHistoryItem, ScanHistoryAdapter.ScanViewHolder>(DIFF_CALLBACK) {
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ScanHistoryItem>() {
            override fun areItemsTheSame(oldItem: ScanHistoryItem, newItem: ScanHistoryItem): Boolean {
                return oldItem.id == newItem.id
            }
            
            override fun areContentsTheSame(oldItem: ScanHistoryItem, newItem: ScanHistoryItem): Boolean {
                return oldItem == newItem
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scan_history, parent, false)
        return ScanViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ScanViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class ScanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvFoodName: TextView = itemView.findViewById(R.id.tv_food_name)
        private val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        private val tvCalories: TextView = itemView.findViewById(R.id.tv_calories)
        private val tvNutrients: TextView = itemView.findViewById(R.id.tv_nutrients)
        
        fun bind(item: ScanHistoryItem) {
            tvFoodName.text = item.foodName
            
            // Format date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val date = Date(item.timestamp)
            tvDate.text = dateFormat.format(date)
            
            tvCalories.text = "${item.nutrients.calories} kcal"
            tvNutrients.text = "Protein: ${item.nutrients.protein}g | Carbs: ${item.nutrients.carbs}g | Fat: ${item.nutrients.fat}g"
        }
    }
}