package com.ownboard.app.ui

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.view.ViewGroup
import com.ownboard.app.R
import com.ownboard.app.db.LayoutDatabase

class ManageLayoutsActivity : Activity() {

    private lateinit var layoutDatabase: LayoutDatabase
    private lateinit var layoutsContainer: LinearLayout
    private lateinit var resetLayoutsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_layouts)

        layoutDatabase = LayoutDatabase(this)
        layoutsContainer = findViewById(R.id.layouts_container)
        resetLayoutsButton = findViewById(R.id.reset_layouts_button)

        loadLayoutButtons()

        resetLayoutsButton.setOnClickListener {
            layoutDatabase.resetToDefaultLayouts()
            loadLayoutButtons()
            Toast.makeText(this, "تمت إعادة التعيين", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadLayoutButtons() {
        layoutsContainer.removeAllViews()

        val allLayouts = layoutDatabase.getAllLayouts()

        if (allLayouts.isEmpty()) {
            val textView = TextView(this).apply {
                text = "لا توجد بيانات."
                gravity = Gravity.CENTER
                textSize = 18f
                setPadding(0, 32, 0, 32)
            }
            layoutsContainer.addView(textView)
            return
        }

        // استخدام حلقة for صريحة لحل مشكلة الغموض في Kotlin
        for (i in allLayouts.indices) {
            val lang = allLayouts[i].first
            
            val button = Button(this).apply {
                text = "${getLocalizedLayoutName(lang)} ($lang)"
                textSize = 18f
                
                val params = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 16)
                layoutParams = params

                setOnClickListener {
                    val intent = android.content.Intent(this@ManageLayoutsActivity, LayoutEditorActivity::class.java)
                    // تحديد النوع String صراحة لحل مشكلة putExtra
                    intent.putExtra("LANG_CODE", lang as String)
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
            "symbols" -> "الرموز"
            else -> lang.uppercase()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        layoutDatabase.close()
    }
}