package com.ownboard.app.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

data class EmojiCategory(val title: String, val emojis: List<String>)

object EmojiDataManager {
    private var categories: List<EmojiCategory> = emptyList()
    var isLoaded = false

    suspend fun loadEmojis(context: Context) = withContext(Dispatchers.IO) {
        if (isLoaded) return@withContext

        val tempList = mutableListOf<EmojiCategory>()
        try {
            val jsonString = context.assets.open("all-emoji.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)

            var currentTitle = "Smileys & Emotion" // عنوان افتراضي
            var currentEmojis = mutableListOf<String>()

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONArray(i)
                
                // إذا كان العنصر مصفوفة بطول 1 فهو عنوان
                if (item.length() == 1) {
                    val rawTitle = item.getString(0)
                    
                    // الشرط الذكي: نعتبره تبويب جديد فقط إذا كان يبدأ بحرف كبير (مثل "Animals & Nature")
                    // العناوين الفرعية مثل "face-smiling" سيتم تجاهلها ودمجها مع التبويب الحالي
                    if (rawTitle[0].isUpperCase()) {
                        // حفظ الفئة السابقة
                        if (currentEmojis.isNotEmpty()) {
                            tempList.add(EmojiCategory(currentTitle, ArrayList(currentEmojis)))
                            currentEmojis = mutableListOf()
                        }
                        currentTitle = rawTitle
                    }
                } else if (item.length() >= 3) {
                    // إضافة الإيموجي للقائمة الحالية
                    currentEmojis.add(item.getString(2))
                }
            }
            // إضافة آخر فئة
            if (currentEmojis.isNotEmpty()) {
                tempList.add(EmojiCategory(currentTitle, currentEmojis))
            }

            categories = tempList
            isLoaded = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCategories(): List<EmojiCategory> = categories
}