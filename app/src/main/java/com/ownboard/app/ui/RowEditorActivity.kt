package com.ownboard.app.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
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
    
    private var langCode: String = ""
    private var rowIndex: Int = -1 // تغيير الاسم من rowKey إلى rowIndex
    
    private var fullJsonArray: JSONArray? = null // تغيير من Object إلى Array
    private var keysList = mutableListOf<JSONObject>()
    private var hasUnsavedChanges = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_row_editor)

        langCode = intent.getStringExtra("LANG_CODE") ?: return finish()
        // نستقبل الرقم كنص ونحوله
        val indexStr = intent.getStringExtra("ROW_KEY") ?: return finish()
        rowIndex = indexStr.toIntOrNull() ?: return finish()

        layoutDatabase = LayoutDatabase(this)
        
        initViews()
        loadRowData()
        
        findViewById<Button>(R.id.btn_save_changes).setOnClickListener { saveRowData() }
        findViewById<Button>(R.id.btn_add_key).setOnClickListener { addNewKey() }
    }

    private fun initViews() {
        keysContainer = findViewById(R.id.keys_list_container)
        inputRowHeight = findViewById(R.id.input_row_height)
        txtRowTitle = findViewById(R.id.txt_row_title)
        
        txtRowTitle.text = "تعديل الصف رقم: ${rowIndex + 1}"
        
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
            fullJsonArray = JSONArray(jsonString)
            // الوصول للصف عن طريق الـ Index
            val rowObj = fullJsonArray!!.getJSONObject(rowIndex)
            
            val height = rowObj.optDouble("height", 50.0)
            inputRowHeight.setText(height.toString())

            val keysArray = rowObj.getJSONArray("keys")
            keysList.clear()
            for (i in 0 until keysArray.length()) {
                keysList.add(keysArray.getJSONObject(i))
            }
            
            renderKeysList()
            hasUnsavedChanges = false

        } catch (e: Exception) {
            Toast.makeText(this, "خطأ: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun renderKeysList() {
        keysContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)

        keysList.forEachIndexed { index, keyObj ->
            val itemView = inflater.inflate(R.layout.item_key_card, keysContainer, false)
            
            val txtLabel = itemView.findViewById<TextView>(R.id.txt_key_label)
            val txtType = itemView.findViewById<TextView>(R.id.txt_key_type)
            val txtWeight = itemView.findViewById<TextView>(R.id.txt_key_weight)
            val btnDelete = itemView.findViewById<View>(R.id.btn_delete_key)
            val btnEdit = itemView.findViewById<View>(R.id.btn_edit_key)
            val btnMove = itemView.findViewById<View>(R.id.btn_move_key)

            val textVal = keyObj.optString("text", "")
            val label = if (textVal.isNotEmpty()) textVal else "زر"
            
            txtLabel.text = label
            txtType.text = "" 
            txtWeight.text = "الوزن: ${keyObj.optDouble("weight", 1.0)}"

            btnDelete.setOnClickListener { confirmDeleteKey(index) }

            btnEdit.setOnClickListener {
                val intent = Intent(this, KeyEditorActivity::class.java)
                intent.putExtra("LANG_CODE", langCode)
                intent.putExtra("ROW_KEY", rowIndex.toString()) // نمرر الـ Index
                intent.putExtra("KEY_INDEX", index)
                startActivity(intent)
            }

            btnMove.setOnClickListener { view -> showMoveMenu(view, index) }

            keysContainer.addView(itemView)
        }
    }

    private fun showMoveMenu(view: View, index: Int) {
        val popup = PopupMenu(this, view)
        popup.menu.add(0, 1, 0, "تحريك لليمين (سابق)")
        popup.menu.add(0, 2, 0, "تحريك لليسار (تالي)")
        
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> moveKey(index, -1)
                2 -> moveKey(index, 1)
            }
            true
        }
        popup.show()
    }

    private fun moveKey(currentIndex: Int, direction: Int) {
        val newIndex = currentIndex + direction
        if (newIndex in 0 until keysList.size) {
            Collections.swap(keysList, currentIndex, newIndex)
            renderKeysList()
            hasUnsavedChanges = true
        }
    }

    private fun confirmDeleteKey(index: Int) {
        AlertDialog.Builder(this)
            .setTitle("حذف الزر")
            .setMessage("تأكيد الحذف؟")
            .setPositiveButton("نعم") { _, _ ->
                keysList.removeAt(index)
                renderKeysList()
                hasUnsavedChanges = true
            }
            .setNegativeButton("لا", null)
            .show()
    }

    private fun addNewKey() {
        val newKey = JSONObject()
        newKey.put("text", "جديد")
        newKey.put("weight", 1.0)
        newKey.put("click", "sendText")
        newKey.put("params", JSONObject().put("text", "جديد")) // هيكلة Params الجديدة
        
        keysList.add(newKey)
        renderKeysList()
        hasUnsavedChanges = true
        keysContainer.post { 
            (keysContainer.parent as ScrollView).fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun saveRowData() {
        try {
            val rowObj = fullJsonArray!!.getJSONObject(rowIndex)
            
            val newHeight = inputRowHeight.text.toString().toDoubleOrNull() ?: 50.0
            rowObj.put("height", newHeight)

            val newKeysArray = JSONArray()
            keysList.forEach { newKeysArray.put(it) }
            rowObj.put("keys", newKeysArray)

            // تحديث المصفوفة الرئيسية في موقعها
            fullJsonArray!!.put(rowIndex, rowObj)

            layoutDatabase.updateLayout(langCode, fullJsonArray!!.toString())
            
            hasUnsavedChanges = false
            Toast.makeText(this, "تم الحفظ!", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "فشل الحفظ: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onBackPressed() {
        if (hasUnsavedChanges) {
            AlertDialog.Builder(this)
                .setTitle("تغييرات غير محفوظة")
                .setMessage("هل تريد الخروج؟")
                .setPositiveButton("خروج") { _, _ -> super.onBackPressed() }
                .setNegativeButton("إلغاء", null)
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
        loadRowData()
    }
}