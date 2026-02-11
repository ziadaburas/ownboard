package com.ownboard.app.ui

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.ownboard.app.R
import com.ownboard.app.utils.SettingItem
import com.ownboard.app.utils.SettingsManager

class GlobalSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_global_settings)

        val container = findViewById<LinearLayout>(R.id.settingsContainer)

        // 1. بناء حقول الإعدادات
        for (item in SettingsManager.settingsList) {
            val view = createViewForItem(item)
            container.addView(view)
            
            // فاصل بسيط
            val spacer = View(this).apply { 
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 20)
            }
            container.addView(spacer)
        }

        // 2. إضافة زر "استعادة الافتراضي" في نهاية القائمة
        val resetBtn = Button(this).apply {
            text = "استعادة الإعدادات الافتراضية"
            textSize = 16f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#D32F2F")) // لون أحمر للتحذير
            
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                150 // ارتفاع الزر
            ).apply {
                setMargins(20, 50, 20, 50) // هوامش كبيرة لفصله عن البقية
            }

            setOnClickListener {
                showResetConfirmationDialog()
            }
        }
        
        container.addView(resetBtn)
    }

    private fun showResetConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("تنبيه")
            .setMessage("هل أنت متأكد أنك تريد حذف كل تخصيصاتك والعودة للإعدادات الأصلية؟")
            .setPositiveButton("نعم، استعدها") { dialog, _ ->
                // استدعاء دالة الريسيت
                SettingsManager.resetToDefaults(applicationContext)
                
                Toast.makeText(this, "تمت الاستعادة بنجاح", Toast.LENGTH_SHORT).show()
                
                // إعادة تشغيل النشاط لتحديث الواجهة بالقيم الجديدة
                recreate()
                dialog.dismiss()
            }
            .setNegativeButton("إلغاء") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun createViewForItem(item: SettingItem): View {
        val context = this
        
        // الحاوية الخارجية للعنصر
        val itemLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(Color.parseColor("#333333"))
            setPadding(30, 30, 30, 30)
        }

        // نص الوصف
        val label = TextView(context).apply {
            text = item.description
            setTextColor(Color.WHITE)
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 15 }
        }
        itemLayout.addView(label)

        // تحديد نوع الحقل (number, text, boolean)
        when (item.type) {
            "boolean" -> {
                val switchView = Switch(context).apply {
                    isChecked = item.value.toString().toBoolean()
                    text = if(isChecked) "مفعل (On)" else "معطل (Off)"
                    setTextColor(Color.LTGRAY)
                    setOnCheckedChangeListener { _, isChecked ->
                        text = if(isChecked) "مفعل (On)" else "معطل (Off)"
                        SettingsManager.updateValue(item.key, isChecked)
                    }
                }
                itemLayout.addView(switchView)
            }

            "number", "text" -> {
                val editText = EditText(context).apply {
                    setText(item.value.toString())
                    setTextColor(Color.WHITE)
                    setHintTextColor(Color.GRAY)
                    setBackgroundColor(Color.parseColor("#444444"))
                    setPadding(20, 20, 20, 20)

                    inputType = if (item.type == "number") {
                        InputType.TYPE_CLASS_NUMBER
                    } else {
                        InputType.TYPE_CLASS_TEXT
                    }
                }

                val saveBtn = Button(context).apply {
                    text = "حفظ"
                    setOnClickListener {
                        val newValue = editText.text.toString()
                        if (item.type == "number") {
                            // حفظ كرقم
                            SettingsManager.updateValue(item.key, newValue.toIntOrNull() ?: 0)
                        } else {
                            // حفظ كنص
                            SettingsManager.updateValue(item.key, newValue)
                        }
                        Toast.makeText(context, "تم حفظ ${item.key}", Toast.LENGTH_SHORT).show()
                    }
                }

                val inputRow = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    addView(editText, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                    addView(saveBtn)
                }
                itemLayout.addView(inputRow)
            }
        }

        return itemLayout
    }
}