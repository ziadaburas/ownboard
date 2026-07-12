package com.ownboard.app.ui

import android.graphics.Color
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ownboard.app.db.ClipboardItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClipboardManageAdapter(
    private val onItemClick: (ClipboardItem) -> Unit
) : RecyclerView.Adapter<ClipboardManageAdapter.ClipViewHolder>() {

    private var allItems: List<ClipboardItem> = ArrayList()
    private var displayedItems: List<ClipboardItem> = ArrayList()
    
    // متغير لحفظ عملية البحث الحالية حتى نتمكن من إلغائها إذا كتب المستخدم بسرعة
    private var searchJob: Job? = null
    
    // لتنسيق التاريخ
    private val dateFormatter = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())

    fun updateList(list: List<ClipboardItem>) {
        this.allItems = list
        this.displayedItems = list
        notifyDataSetChanged()
    }

    // فلترة بالنص (بشكل غير متزامن)
    // فلترة بالنص (تستقبل دالة عند الانتهاء)
    fun filter(query: String, caseSensitive: Boolean, onComplete: () -> Unit = {}) {
        searchJob?.cancel()
        
        searchJob = CoroutineScope(Dispatchers.Default).launch {
            val filteredList = if (query.isEmpty()) {
                allItems
            } else {
                allItems.filter { item ->
                    if (caseSensitive) {
                        item.text.contains(query, ignoreCase = false)
                    } else {
                        item.text.contains(query, ignoreCase = true)
                    }
                }
            }

            withContext(Dispatchers.Main) {
                displayedItems = filteredList
                notifyDataSetChanged()
                onComplete() // استدعاء الدالة لإخفاء شريط التحميل
            }
        }
    }

    // فلترة بالتاريخ
    fun filterByDate(startTime: Long, endTime: Long, onComplete: () -> Unit = {}) {
        searchJob?.cancel()
        searchJob = CoroutineScope(Dispatchers.Default).launch {
            val filteredList = allItems.filter { item ->
                item.timestamp in startTime..endTime
            }
            withContext(Dispatchers.Main) {
                displayedItems = filteredList
                notifyDataSetChanged()
                onComplete()
            }
        }
    }

    // إعادة عرض الكل
    fun showAll() {
        searchJob?.cancel()
        displayedItems = allItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClipViewHolder {
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
            setTextColor(Color.parseColor("#AAAAAA")) 
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
        
        val displayStr = if (item.text.length > 100) item.text.substring(0, 100) + "..." else item.text
        holder.tvText.text = displayStr
        
        holder.tvDate.text = dateFormatter.format(Date(item.timestamp))

        if (item.isPinned) {
            holder.itemView.setBackgroundColor(Color.parseColor("#3D3D3D"))
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#222222"))
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