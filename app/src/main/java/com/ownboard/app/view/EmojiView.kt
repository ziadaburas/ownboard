package com.ownboard.app.view

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.ownboard.app.OwnboardIME
import com.ownboard.app.R
import com.ownboard.app.db.RecentEmojiDB
import com.ownboard.app.utils.EmojiCategory
import com.ownboard.app.utils.EmojiDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EmojiView(context: Context) : FrameLayout(context) {

    private val db = RecentEmojiDB(context)
    private var viewPager: ViewPager2? = null
    private var tabsContainer: LinearLayout? = null
    
    private var allCategories = mutableListOf<EmojiCategory>()
    private var displayCategories = mutableListOf<EmojiCategory>()
    private var pagerAdapter: EmojiPagerAdapter? = null

    private val repeatHandler = Handler(Looper.getMainLooper())
    private val repeatRunnable = object : Runnable {
        override fun run() {
            OwnboardIME.ime.delete()
            repeatHandler.postDelayed(this, 50)
        }
    }
    
    private val colorAccentBlue = ContextCompat.getColor(context, R.color.emoji_accent_blue)
    private val colorTextWhite = ContextCompat.getColor(context, R.color.emoji_text_white)

    init {
        try {
            LayoutInflater.from(context).inflate(R.layout.layout_emoji_board, this, true)
            
            viewPager = findViewById(R.id.emoji_view_pager)
            tabsContainer = findViewById(R.id.tabs_container)

            // === زر الإغلاق (يمين) ===
            findViewById<View>(R.id.btn_close_emoji)?.setOnClickListener {
                OwnboardIME.ime.toggleEmoji()
            }
            
            // === زر الحذف (يسار) ===
            val btnDelete = findViewById<View>(R.id.btn_backspace)
            btnDelete?.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        OwnboardIME.ime.delete()
                        repeatHandler.postDelayed(repeatRunnable, 400)
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        repeatHandler.removeCallbacks(repeatRunnable)
                        true
                    }
                    else -> false
                }
            }

            initialLoad()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ... (بقية الدوال كما هي تماماً بدون تغيير: initialLoad, refreshRecentsList, resetToFirstTab, setupPager, setupTabs, Adapters...)
    
    private fun initialLoad() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                EmojiDataManager.loadEmojis(context)
                allCategories = EmojiDataManager.getCategories().toMutableList()
                refreshRecentsList()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun refreshRecentsList() {
        CoroutineScope(Dispatchers.Main).launch {
            val recents = withContext(Dispatchers.IO) {
                db.getRecentEmojis(60)
            }

            displayCategories.clear()
            displayCategories.add(EmojiCategory("Recent", recents))
            displayCategories.addAll(allCategories)

            if (pagerAdapter == null) {
                setupPager()
                setupTabs()
            } else {
                pagerAdapter?.notifyDataSetChanged()
            }
        }
    }

    fun resetToFirstTab() {
        viewPager?.setCurrentItem(0, false)
        refreshRecentsList()
    }

    private fun setupPager() {
        if (viewPager == null) return
        pagerAdapter = EmojiPagerAdapter(displayCategories, 
            onClick = { emoji -> onEmojiClicked(emoji) },
            onLongClick = { emoji, categoryTitle -> onEmojiLongClicked(emoji, categoryTitle) }
        )
        viewPager!!.adapter = pagerAdapter
        viewPager!!.offscreenPageLimit = 1
        viewPager!!.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) { updateActiveTab(position) }
        })
    }

    private fun setupTabs() {
        if (tabsContainer == null) return
        tabsContainer!!.removeAllViews()
        
        displayCategories.forEachIndexed { index, cat ->
            val tab = TextView(context).apply {
                text = getTabIcon(cat.title)
                textSize = 20f
                gravity = Gravity.CENTER
                setTextColor(colorTextWhite)
                setBackgroundColor(Color.TRANSPARENT)
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
                setOnClickListener { viewPager?.setCurrentItem(index, true) }
            }
            tabsContainer!!.addView(tab)
        }
        updateActiveTab(0)
    }

    private fun updateActiveTab(position: Int) {
        if (tabsContainer == null) return
        for (i in 0 until tabsContainer!!.childCount) {
            val child = tabsContainer!!.getChildAt(i) as? TextView ?: continue
            if (i == position) {
                child.setTextColor(colorAccentBlue)
            } else {
                child.setTextColor(colorTextWhite)
            }
        }
    }
    
    private fun getTabIcon(title: String): String {
        return when {
            title == "Recent" -> "🕒"
            title.startsWith("Smileys") -> "😀"
            title.startsWith("People") -> "👋"
            title.startsWith("Animals") -> "🐻"
            title.startsWith("Food") -> "🍔"
            title.startsWith("Travel") -> "🚗"
            title.startsWith("Activities") -> "⚽"
            title.startsWith("Objects") -> "💡"
            title.startsWith("Symbols") -> "❤️"
            title.startsWith("Flags") -> "🏳️"
            else -> title.take(2)
        }
    }

    private fun onEmojiClicked(emoji: String) {
        OwnboardIME.ime.sendKeyPress(emoji)
        CoroutineScope(Dispatchers.IO).launch {
            db.addEmoji(emoji)
        }
    }

    private fun onEmojiLongClicked(emoji: String, categoryTitle: String) {
        if (categoryTitle == "Recent") {
            CoroutineScope(Dispatchers.IO).launch {
                db.deleteEmoji(emoji)
                withContext(Dispatchers.Main) {
                    refreshRecentsList()
                    Toast.makeText(context, "تم الحذف", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    inner class EmojiPagerAdapter(
        private val categories: List<EmojiCategory>, 
        private val onClick: (String) -> Unit,
        private val onLongClick: (String, String) -> Unit
    ) : RecyclerView.Adapter<EmojiPageViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiPageViewHolder {
            val rv = RecyclerView(parent.context)
            rv.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            rv.layoutManager = GridLayoutManager(parent.context, 7)
            rv.setHasFixedSize(true)
            rv.setPadding(4, 4, 4, 4)
            rv.clipToPadding = false
            return EmojiPageViewHolder(rv)
        }
        override fun onBindViewHolder(holder: EmojiPageViewHolder, position: Int) {
            val cat = categories[position]
            holder.bind(cat.emojis, cat.title, onClick, onLongClick)
        }
        override fun getItemCount() = categories.size
    }
    
    inner class EmojiPageViewHolder(val recyclerView: RecyclerView) : RecyclerView.ViewHolder(recyclerView) {
        fun bind(emojis: List<String>, categoryTitle: String, onClick: (String) -> Unit, onLongClick: (String, String) -> Unit) { 
            recyclerView.adapter = EmojiGridAdapter(emojis, categoryTitle, onClick, onLongClick) 
        }
    }

    inner class EmojiGridAdapter(
        private val emojis: List<String>,
        private val categoryTitle: String,
        private val onClick: (String) -> Unit,
        private val onLongClick: (String, String) -> Unit
    ) : RecyclerView.Adapter<EmojiCellViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiCellViewHolder {
            val tv = TextView(parent.context).apply {
                val params = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120)
                params.setMargins(0, 20, 0, 20)
                layoutParams = params
                textSize = 28f 
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                background = null 
                isClickable = true
                isFocusable = true
            }
            return EmojiCellViewHolder(tv)
        }

        override fun onBindViewHolder(holder: EmojiCellViewHolder, position: Int) {
            val emoji = emojis[position]
            holder.tv.text = emoji
            holder.tv.setOnClickListener { onClick(emoji) }
            holder.tv.setOnLongClickListener {
                onLongClick(emoji, categoryTitle)
                true
            }
        }

        override fun getItemCount() = emojis.size
    }

    inner class EmojiCellViewHolder(val tv: TextView) : RecyclerView.ViewHolder(tv)
}