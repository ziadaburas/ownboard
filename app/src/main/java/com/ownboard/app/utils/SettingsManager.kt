package com.ownboard.app.utils

import android.content.Context
import com.ownboard.app.db.SettingsDbHelper // تأكد من المسار
import org.json.JSONArray
import org.json.JSONObject

data class SettingItem(
    val key: String,       // مثل: keyboardHeight
    var value: Any,        // القيمة (300, true, "dark")
    val type: String,      // نوع الحقل: number, boolean, text
    val description: String // الوصف الظاهر للمستخدم
)

object SettingsManager {

    // الخريطة السريعة: key -> value
    private val valuesMap = mutableMapOf<String, Any>()

    // القائمة الكاملة لبناء الواجهة
    val settingsList = mutableListOf<SettingItem>()

    private lateinit var dbHelper: SettingsDbHelper

    fun init(context: Context) {
        dbHelper = SettingsDbHelper(context)

        var jsonString = dbHelper.getSettingsJson()

        if (jsonString == null || jsonString.isEmpty()) {
            try {
                // قراءة الملف من assets عند التشغيل لأول مرة
                jsonString = context.assets.open("appSettings.json").bufferedReader().use { it.readText() }
                dbHelper.saveSettingsJson(jsonString)
            } catch (e: Exception) {
                e.printStackTrace()
                jsonString = "[]" // مصفوفة فارغة في حال الفشل
            }
        }

        parseJsonToMemory(jsonString ?: "[]")
    }

    private fun parseJsonToMemory(jsonString: String) {
        valuesMap.clear()
        settingsList.clear()

        try {
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                
                val key = obj.getString("key")
                val type = obj.getString("type")
                val description = obj.getString("description")
                var value = obj.get("value") // نأخذ القيمة كـ Object عام

                // تصحيح نوع الأرقام (لأن JSON قد يرجعها كـ Integer أو Double)
                if (type == "number") {
                     value = value.toString().toDouble().toInt()
                } else if (type == "boolean") {
                     value = value.toString().toBoolean()
                } else {
                     value = value.toString()
                }

                val item = SettingItem(key, value, type, description)
                
                settingsList.add(item)
                valuesMap[key] = value
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun resetToDefaults(context: Context) {
        try {
            // 1. قراءة الملف الأصلي من الـ Assets
            val defaultJson = context.assets.open("appSettings.json").bufferedReader().use { it.readText() }
            
            // 2. حفظه في قاعدة البيانات (سيستبدل البيانات الحالية)
            dbHelper.saveSettingsJson(defaultJson)
            
            // 3. تحديث المتغيرات في الذاكرة الحية
            parseJsonToMemory(defaultJson)
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- دوال الوصول (Getters) ---
    
    fun getInt(key: String, defaultValue: Int): Int {
        val rawValue = valuesMap[key]
        
        // 1. إذا القيمة غير موجودة في الماب، رجع القيمة الافتراضية فوراً
        if (rawValue == null) return defaultValue

        // 2. محاولة التحويل
        return try {
            // التعامل مع الأرقام سواء كانت Double أو String أو Int
            rawValue.toString().toDouble().toInt()
        } catch (e: Exception) {
            // 3. في حال فشل التحويل، رجع القيمة الافتراضية
            defaultValue
        }
    } 

    fun getBoolean(key: String): Boolean {
        return valuesMap[key]?.toString()?.toBoolean() ?: false
    }

    fun getString(key: String): String {
        return valuesMap[key]?.toString() ?: ""
    }
    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        // يحاول يجيب القيمة ويحولها، إذا كانت null (مش موجودة) يرجع الـ defaultValue
        return valuesMap[key]?.toString()?.toBoolean() ?: defaultValue
    }

    fun getString(key: String, defaultValue: String): String {
        // يحاول يجيب النص، إذا كان null يرجع الـ defaultValue
        return valuesMap[key]?.toString() ?: defaultValue
    }
    
    // دالة للحصول على القوائم المفصولة بمسافات (مثل backTexts)
    fun getList(key: String): List<String> {
        val rawText = getString(key)
        if (rawText.isEmpty()) return emptyList()
        return rawText.split(" ")
    }

    // --- دالة التحديث (Update) ---
    fun updateValue(key: String, newValue: Any) {
        // 1. تحديث الذاكرة الحية
        valuesMap[key] = newValue
        settingsList.find { it.key == key }?.value = newValue

        // 2. إعادة بناء JSON يدوياً للحفظ
        saveListToJson()
    }
    
    private fun saveListToJson() {
        try {
            val jsonArray = JSONArray()
            
            for (item in settingsList) {
                val obj = JSONObject()
                obj.put("key", item.key)
                obj.put("value", item.value)
                obj.put("type", item.type)
                obj.put("description", item.description)
                
                jsonArray.put(obj)
            }
            
            // تحويل المصفوفة لنص وحفظها في قاعدة البيانات
            dbHelper.saveSettingsJson(jsonArray.toString())
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}