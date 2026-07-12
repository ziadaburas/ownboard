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
import android.view.LayoutInflater // تأكد من استدعاء هذه المكتبة في الأعلى


class GlobalSettingsActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_global_settings)

        container = findViewById(R.id.settingsContainer)

        SettingsManager.init(this) 

        // 2. الآن القائمة settingsList ممتلئة، يمكننا بناء الواجهة بأمان
        buildSettingsViews()
        
    }

    private fun buildSettingsViews() {
        // تنظيف الحاوية أولاً لتجنب التكرار في حال إعادة الرسم
        container.removeAllViews()

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
       val inflater = LayoutInflater.from(this)

        // نقوم بتمرير container كأب (Parent) مع false، لكي يتم احترام الهوامش (Margins) الموجودة في الـ XML
        val resetBtn = inflater.inflate(R.layout.item_btn, container, false) as Button

        resetBtn.setOnClickListener {
            showResetConfirmationDialog()
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
    val inflater = LayoutInflater.from(this)

    return when (item.type) {
        "boolean" -> {
            // استدعاء ملف الـ xml الخاص بالـ Switch
            val view = inflater.inflate(R.layout.item_setting_boolean, null, false)
            
            // ربط العناصر
            val tvLabel = view.findViewById<TextView>(R.id.tvLabel)
            val switchValue = view.findViewById<Switch>(R.id.switchValue)

            // وضع القيم المبدئية
            tvLabel.text = item.description
            val isCheckedInitial = item.value.toString().toBoolean()
            switchValue.isChecked = isCheckedInitial
            switchValue.text = if (isCheckedInitial) "مفعل (On)" else "معطل (Off)"

            // إضافة مستمع التغييرات
            switchValue.setOnCheckedChangeListener { _, isChecked ->
                switchValue.text = if (isChecked) "مفعل (On)" else "معطل (Off)"
                SettingsManager.updateValue(item.key, isChecked)
            }
            
            view // إرجاع الواجهة الجاهزة
        }

        "number", "text" -> {
            // استدعاء ملف الـ xml الخاص بالحقول النصية
            val view = inflater.inflate(R.layout.item_setting_input, null, false)
            
            // ربط العناصر
            val tvLabel = view.findViewById<TextView>(R.id.tvLabel)
            val editValue = view.findViewById<EditText>(R.id.editValue)
            val btnSave = view.findViewById<Button>(R.id.btnSave)

            // وضع القيم المبدئية
            tvLabel.text = item.description
            editValue.setText(item.value.toString())
            editValue.inputType = if (item.type == "number") {
                InputType.TYPE_CLASS_NUMBER
            } else {
                InputType.TYPE_CLASS_TEXT
            }

            // إضافة مستمع النقرات لزر الحفظ
            btnSave.setOnClickListener {
                val newValue = editValue.text.toString()
                if (item.type == "number") {
                    SettingsManager.updateValue(item.key, newValue.toIntOrNull() ?: 0)
                } else {
                    SettingsManager.updateValue(item.key, newValue)
                }
                Toast.makeText(this, "تم حفظ ${item.key}", Toast.LENGTH_SHORT).show()
            }
            
            view // إرجاع الواجهة الجاهزة
        }

        else -> {
            // في حال وجود نوع غير معروف، نعيد حاوية فارغة
            View(this)
        }
    }
}
    private fun createViewForItem1(item: SettingItem): View {
        val context = this
        
        // الحاوية الخارجية للعنصر
        val itemLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(Color.parseColor("#FF2D2D2D"))
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

                // إضافة أبعاد صريحة لحل مشكلة اختفاء حقول النص والأرقام
                val inputRow = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    addView(editText, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                    addView(saveBtn)
                }
                itemLayout.addView(inputRow)
            }
        }

        return itemLayout
    }
}