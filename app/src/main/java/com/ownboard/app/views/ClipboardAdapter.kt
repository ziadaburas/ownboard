package com.ownboard.app.view

import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.ownboard.app.R
import com.ownboard.app.db.ClipboardItem

class ClipboardAdapter(
    private val onItemClick: (String) -> Unit,
    private val onItemLongClick: (ClipboardItem) -> Unit
) : RecyclerView.Adapter<ClipboardAdapter.ClipViewHolder>() {

    var items: List<ClipboardItem> = emptyList()

    fun updateList(newItems: List<ClipboardItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClipViewHolder {
        // حساب الارتفاع الثابت (مثلاً 60dp)
        val fixedHeight = (60 * parent.context.resources.displayMetrics.density).toInt()

        val btn = Button(parent.context).apply {
            setTextColor(Color.WHITE)
            setPadding(20, 0, 20, 0) // تقليل الهوامش العلوية والسفلية
            maxLines = 2 // سطرين كحد أقصى عشان ما يختفي النص
            ellipsize = android.text.TextUtils.TruncateAt.END // إضافة (...) إذا النص طويل
            textSize = 14f
            gravity = Gravity.CENTER // محاذاة النص
            
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                fixedHeight // استخدام الارتفاع الثابت هنا بدلاً من WRAP_CONTENT
            ).apply {
                setMargins(0, 5, 0, 5)
            }
        }
        return ClipViewHolder(btn)
    }

    override fun onBindViewHolder(holder: ClipViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ClipViewHolder(val btn: Button) : RecyclerView.ViewHolder(btn) {
        fun bind(item: ClipboardItem) {
            btn.text = item.text
            
            // تعيين الخلفية حسب التثبيت (كما في كودك)
            if (item.isPinned) {
                // حاول استخدام لون مميز أو أيقونة إذا لم تكن الصورة موجودة
                btn.setBackgroundColor(Color.parseColor("#444444")) 
                btn.text = "📌 ${item.text}"
            } else {
                btn.setBackgroundColor(Color.parseColor("#2D2D2D"))
            }

            btn.setOnClickListener { onItemClick(item.text) }
            btn.setOnLongClickListener {
                onItemLongClick(item)
                true
            }
        }
    }
}