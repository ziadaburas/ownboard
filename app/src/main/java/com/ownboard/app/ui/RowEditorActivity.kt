package com.ownboard.app.ui

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.ownboard.app.R
import com.ownboard.app.db.LayoutDatabase
import org.json.JSONArray
import org.json.JSONObject
import java.util.Collections

class RowEditorActivity : Activity() {

    private lateinit var layoutDatabase: LayoutDatabase
    private lateinit var keysContainer: LinearLayout
    private lateinit var inputRowHeight: EditText
    private lateinit var txtRowTitle: TextView
    
    // متغيرات الحالة
    private var langCode: String = ""
    private var rowKey: String = ""
    private var fullJsonObj: JSONObject? = null
    private var keysList = mutableListOf<JSONObject>()
    private var hasUnsavedChanges = false // علم (Flag) لتتبع التعديلات

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_row_editor)

        // استلام البيانات
        langCode = intent.getStringExtra("LANG_CODE") ?: return finish()
        rowKey = intent.getStringExtra("ROW_KEY") ?: return finish()

        layoutDatabase = LayoutDatabase(this)
        
        initViews()
        loadRowData()
        
        // زر الحفظ
        findViewById<Button>(R.id.btn_save_changes).setOnClickListener {
            saveRowData()
        }

        // زر إضافة مفتاح جديد (مبدئي)
        findViewById<Button>(R.id.btn_add_key).setOnClickListener {
            addNewKey()
        }
    }

    private fun initViews() {
        keysContainer = findViewById(R.id.keys_list_container)
        inputRowHeight = findViewById(R.id.input_row_height)
        txtRowTitle = findViewById(R.id.txt_row_title)
        
        txtRowTitle.text = "تعديل الصف: $rowKey"
        
        // مراقبة تغيير الارتفاع
        inputRowHeight.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) { hasUnsavedChanges = true }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loadRowData() {
        val jsonString = layoutDatabase.getLayoutByLang(langCode)
        if (jsonString.isEmpty()) return

        try {
            fullJsonObj = JSONObject(jsonString)
            val rowObj = fullJsonObj!!.getJSONObject(rowKey)
            
            // تعيين الارتفاع
            val height = rowObj.optDouble("height", 50.0)
            inputRowHeight.setText(height.toString())

            // تعيين الأزرار
            val keysArray = rowObj.getJSONArray("keys")
            keysList.clear()
            for (i in 0 until keysArray.length()) {
                keysList.add(keysArray.getJSONObject(i))
            }
            
            renderKeysList()
            hasUnsavedChanges = false // إعادة تعيين العلم لأننا حملنا البيانات للتو

        } catch (e: Exception) {
            Toast.makeText(this, "خطأ في تحميل البيانات: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun renderKeysList() {
        keysContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)

        keysList.forEachIndexed { index, keyObj ->
            val itemView = inflater.inflate(R.layout.item_key_card, keysContainer, false)
            
            // ربط العناصر
            val txtLabel = itemView.findViewById<TextView>(R.id.txt_key_label)
            val txtType = itemView.findViewById<TextView>(R.id.txt_key_type)
            val txtWeight = itemView.findViewById<TextView>(R.id.txt_key_weight)
            val btnDelete = itemView.findViewById<View>(R.id.btn_delete_key)
            val btnEdit = itemView.findViewById<View>(R.id.btn_edit_key)
            val btnMove = itemView.findViewById<View>(R.id.btn_move_key)

            // تعبئة البيانات
            val textVal = keyObj.optString("text", "")
            val label = if (textVal.isNotEmpty()) textVal else keyObj.optString("type", "Key")
            
            txtLabel.text = label
            txtType.text = "All :النوع" // يمكن جعله ديناميكي لاحقاً
            txtWeight.text = "${keyObj.optDouble("weight", 1.0)} :الوزن"

            // === برمجة الأزرار ===
            
            // 1. الحذف
            btnDelete.setOnClickListener {
                confirmDeleteKey(index)
            }

            // 2. التعديل (سنفتحه في خطوة قادمة، الآن مجرد توست)
            // 2. التعديل (فتح شاشة محرر الزر)
            btnEdit.setOnClickListener {
                val intent = android.content.Intent(this, KeyEditorActivity::class.java)
                intent.putExtra("LANG_CODE", langCode)
                intent.putExtra("ROW_KEY", rowKey)
                intent.putExtra("KEY_INDEX", index) // نمرر رقم الزر في المصفوفة
                startActivity(intent)
            }

            // 3. الترتيب (قائمة منبثقة للتحريك)
            btnMove.setOnClickListener { view ->
                showMoveMenu(view, index)
            }

            keysContainer.addView(itemView)
        }
    }

    // منطق الترتيب (تحريك للأعلى/الأسفل)
    private fun showMoveMenu(view: View, index: Int) {
        val popup = PopupMenu(this, view)
        popup.menu.add(0, 1, 0, "تحريك للأعلى (يمين)")
        popup.menu.add(0, 2, 0, "تحريك للأسفل (يسار)")
        
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> moveKey(index, -1) // -1 يعني للأعلى في القائمة (يمين في الكيبورد العربي)
                2 -> moveKey(index, 1)  // +1 يعني للأسفل
            }
            true
        }
        popup.show()
    }

    private fun moveKey(currentIndex: Int, direction: Int) {
        val newIndex = currentIndex + direction
        if (newIndex in 0 until keysList.size) {
            Collections.swap(keysList, currentIndex, newIndex)
            renderKeysList() // إعادة رسم القائمة
            hasUnsavedChanges = true
        }
    }

    private fun confirmDeleteKey(index: Int) {
        AlertDialog.Builder(this)
            .setTitle("حذف الزر")
            .setMessage("هل أنت متأكد من حذف هذا الزر؟")
            .setPositiveButton("حذف") { _, _ ->
                keysList.removeAt(index)
                renderKeysList()
                hasUnsavedChanges = true
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun addNewKey() {
        // إضافة زر افتراضي جديد
        val newKey = JSONObject()
        newKey.put("text", "جديد")
        newKey.put("weight", 1.0)
        newKey.put("type", "All")
        
        keysList.add(newKey)
        renderKeysList()
        hasUnsavedChanges = true
        // تمرير لأسفل القائمة لرؤية الزر الجديد
        keysContainer.post { 
            (keysContainer.parent as ScrollView).fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun saveRowData() {
        try {
            // تحديث كائن الصف
            val rowObj = fullJsonObj!!.getJSONObject(rowKey)
            
            // 1. حفظ الارتفاع
            val newHeight = inputRowHeight.text.toString().toDoubleOrNull() ?: 50.0
            rowObj.put("height", newHeight)

            // 2. حفظ مصفوفة الأزرار الجديدة
            val newKeysArray = JSONArray()
            keysList.forEach { newKeysArray.put(it) }
            rowObj.put("keys", newKeysArray)

            // 3. تحديث الكائن الرئيسي
            fullJsonObj!!.put(rowKey, rowObj)

            // 4. الحفظ في قاعدة البيانات
            layoutDatabase.updateLayout(langCode, fullJsonObj!!.toString())
            
            hasUnsavedChanges = false
            Toast.makeText(this, "تم حفظ التعديلات بنجاح!", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "فشل الحفظ: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // === التعامل مع زر الرجوع (حماية التغييرات) ===
    override fun onBackPressed() {
        if (hasUnsavedChanges) {
            AlertDialog.Builder(this)
                .setTitle("تغييرات غير محفوظة")
                .setMessage("لديك تعديلات لم تقم بحفظها. هل تريد الخروج وفقدان التعديلات؟")
                .setPositiveButton("خروج") { _, _ ->
                    super.onBackPressed() // الخروج الفعلي
                }
                .setNegativeButton("بقاء", null) // إلغاء الخروج
                .setNeutralButton("حفظ وخروج") { _, _ ->
                    saveRowData()
                    super.onBackPressed()
                }
                .show()
        } else {
            super.onBackPressed()
        }
    }
    override fun onResume() {
        super.onResume()
        // إعادة تحميل البيانات عند العودة من شاشة تعديل الزر
        loadRowData() 
    }
}