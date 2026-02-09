package com.ownboard.app.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

// كلاس البيانات (كما هو)
data class EmojiCategory(val title: String, val emojis: List<String>)

object EmojiDataManager {
    private var categories: List<EmojiCategory> = emptyList()
    var isLoaded = false

    // دالة التحميل المحدثة للملف الجديد
    suspend fun loadEmojis(context: Context) = withContext(Dispatchers.IO) {
        if (isLoaded) return@withContext

        val tempList = mutableListOf<EmojiCategory>()
        try {
            // 1. قراءة الملف الجديد "emojis_optimized.json"
            val inputStream = context.assets.open("emojis_optimized.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            
            // 2. التحليل كـ JSONObject لأن الهيكلة أصبحت { "Category": [...] }
            val jsonObject = JSONObject(jsonString)
            val keysIterator = jsonObject.keys()

            // 3. المرور على كل الفئات
            while (keysIterator.hasNext()) {
                val categoryName = keysIterator.next() // اسم الفئة (المفتاح)
                val emojisArray = jsonObject.getJSONArray(categoryName) // مصفوفة الإيموجي
                
                val emojisList = ArrayList<String>(emojisArray.length())
                
                // تحويل JSONArray إلى List<String>
                for (i in 0 until emojisArray.length()) {
                    emojisList.add(emojisArray.getString(i))
                }

                // إضافة الفئة للقائمة
                tempList.add(EmojiCategory(categoryName, emojisList))
            }

            categories = tempList
            isLoaded = true
        } catch (e: Exception) {
            e.printStackTrace()
            // في حال حدوث خطأ، يمكنك إضافة فئة فارغة أو التعامل معه هنا
        }
    }

    fun getCategories(): List<EmojiCategory> = categories
}