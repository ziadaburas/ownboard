package com.ownboard.app.ui

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.view.ViewGroup
import com.ownboard.app.R // هام جداً: استيراد R من البكج الرئيسي
import com.ownboard.app.db.LayoutDatabase

class ManageLayoutsActivity : Activity() {

    private lateinit var layoutDatabase: LayoutDatabase
    private lateinit var layoutsContainer: LinearLayout
    private lateinit var resetLayoutsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_layouts)

        // تهيئة قاعدة البيانات والعناصر
        layoutDatabase = LayoutDatabase(this)
        layoutsContainer = findViewById(R.id.layouts_container)
        resetLayoutsButton = findViewById(R.id.reset_layouts_button)

        // تحميل الأزرار عند الفتح
        loadLayoutButtons()

        // زر إعادة التعيين
        resetLayoutsButton.setOnClickListener {
            layoutDatabase.resetToDefaultLayouts()
            loadLayoutButtons()
            Toast.makeText(this, "تمت إعادة تعيين التخطيطات الافتراضية", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadLayoutButtons() {
        layoutsContainer.removeAllViews() // تنظيف القائمة قبل الرسم

        val allLayouts = layoutDatabase.getAllLayouts()

        if (allLayouts.isEmpty()) {
            val textView = TextView(this).apply {
                text = "لا توجد تخطيطات محفوظة."
                gravity = Gravity.CENTER
                textSize = 18f
                setPadding(0, 32, 0, 32)
            }
            layoutsContainer.addView(textView)
            return
        }

        // إنشاء زر لكل لغة موجودة في قاعدة البيانات
        allLayouts.forEach { (lang, _) ->
            val button = Button(this).apply {
                text = "${getLocalizedLayoutName(lang)} ($lang)"
                textSize = 18f
                
                // إعداد الهوامش (Margins)
                val params = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 16) // مسافة سفلية بين الأزرار
                layoutParams = params

                setOnClickListener {
                    // الانتقال لشاشة المحرر مع تمرير كود اللغة
                    val intent = android.content.Intent(this@ManageLayoutsActivity, LayoutEditorActivity::class.java)
                    intent.putExtra("LANG_CODE", lang) // نمرر مثلاً "ar" أو "en"
                    startActivity(intent)
                }
            }
            layoutsContainer.addView(button)
        }
    }

    private fun getLocalizedLayoutName(lang: String): String {
        return when (lang) {
            "ar" -> "العربية"
            "en" -> "English"
            else -> lang.uppercase()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // إغلاق قاعدة البيانات عند الخروج لتجنب تسريب الذاكرة
        layoutDatabase.close()
    }
}