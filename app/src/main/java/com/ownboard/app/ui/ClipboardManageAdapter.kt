package com.ownboard.app.ui

import android.graphics.Color
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ownboard.app.db.ClipboardItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClipboardManageAdapter(
    private val onItemClick: (ClipboardItem) -> Unit
) : RecyclerView.Adapter<ClipboardManageAdapter.ClipViewHolder>() {

    private var allItems: List<ClipboardItem> = ArrayList()
    private var displayedItems: List<ClipboardItem> = ArrayList()
    
    // لتنسيق التاريخ
    private val dateFormatter = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())

    fun updateList(list: List<ClipboardItem>) {
        this.allItems = list
        this.displayedItems = list
        notifyDataSetChanged()
    }

    // فلترة بالنص
    fun filter(query: String, caseSensitive: Boolean) {
        if (query.isEmpty()) {
            displayedItems = allItems
        } else {
            displayedItems = allItems.filter { item ->
                if (caseSensitive) {
                    item.text.contains(query, ignoreCase = false)
                } else {
                    item.text.contains(query, ignoreCase = true)
                }
            }
        }
        notifyDataSetChanged()
    }

    // فلترة بالتاريخ
    fun filterByDate(startTime: Long, endTime: Long) {
        displayedItems = allItems.filter { item ->
            item.timestamp in startTime..endTime
        }
        notifyDataSetChanged()
    }

    // إعادة عرض الكل
    fun showAll() {
        displayedItems = allItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClipViewHolder {
        // إنشاء Layout للعنصر يحتوي على نصين (المحتوى + التاريخ)
        val container = LinearLayout(parent.context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
            setPadding(30, 30, 30, 30)
            setBackgroundColor(Color.parseColor("#2D2D2D"))
        }

        val tvText = TextView(parent.context).apply {
            textSize = 16f
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val tvDate = TextView(parent.context).apply {
            textSize = 12f
            setTextColor(Color.parseColor("#AAAAAA")) // لون رمادي للتاريخ
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 10
            }
        }

        container.addView(tvText)
        container.addView(tvDate)

        return ClipViewHolder(container, tvText, tvDate)
    }

    override fun onBindViewHolder(holder: ClipViewHolder, position: Int) {
        val item = displayedItems[position]
        
        // عرض جزء من النص
        val displayStr = if (item.text.length > 100) item.text.substring(0, 100) + "..." else item.text
        holder.tvText.text = displayStr
        
        // عرض التاريخ
        holder.tvDate.text = dateFormatter.format(Date(item.timestamp))

        // تمييز المثبت
        if (item.isPinned) {
            holder.itemView.setBackgroundColor(Color.parseColor("#3D3D3D"))
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#2D2D2D"))
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = displayedItems.size

    class ClipViewHolder(
        itemView: android.view.View, 
        val tvText: TextView, 
        val tvDate: TextView
    ) : RecyclerView.ViewHolder(itemView)
}