package com.ownboard.app.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import com.ownboard.app.R
import com.ownboard.app.db.LayoutDatabase
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class LayoutEditorActivity : Activity() {

    private lateinit var layoutDatabase: LayoutDatabase
    private lateinit var rowsContainer: LinearLayout
    private lateinit var editorTitle: TextView
    private var currentLangCode: String = ""
    
    // المصفوفة الرئيسية التي سنعدل عليها
    private var jsonArray = JSONArray()

    private val REQUEST_CODE_EXPORT = 101
    private val REQUEST_CODE_IMPORT = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout_editor)

        currentLangCode = intent.getStringExtra("LANG_CODE") ?: return finish()

        layoutDatabase = LayoutDatabase(this)
        initViews()
        setupButtons()
    }
    
    override fun onResume() {
        super.onResume()
        loadRowsList()
    }

    private fun initViews() {
        rowsContainer = findViewById(R.id.rows_container)
        editorTitle = findViewById(R.id.editor_title)
        editorTitle.text = "تعديل: $currentLangCode"
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btn_export_json).setOnClickListener { startExportFile() }
        findViewById<Button>(R.id.btn_import_json).setOnClickListener { startImportFile() }
        findViewById<Button>(R.id.btn_reset_lang).setOnClickListener { resetToDefault() }
        
        // زر إضافة صف جديد (الذي لم يكن يعمل)
        val btnAddRow = findViewById<Button>(R.id.btn_add_row)
        // التحقق من وجود الزر لتجنب الانهيار إذا لم يكن في XML
        btnAddRow?.setOnClickListener { addNewRow() }
    }

    private fun loadRowsList() {
        rowsContainer.removeAllViews()
        val jsonString = layoutDatabase.getLayoutByLang(currentLangCode)
        if (jsonString.isEmpty() || jsonString == "[]") {
            Toast.makeText(this, "التخطيط فارغ", Toast.LENGTH_SHORT).show()
            jsonArray = JSONArray() // تهيئة مصفوفة فارغة
            return
        }

        try {
            // تحويل النص إلى مصفوفة
            jsonArray = JSONArray(jsonString)
            
            // بناء الواجهة لكل صف في المصفوفة
            for (i in 0 until jsonArray.length()) {
                rowsContainer.addView(createRowItemView(i)) 
            }

        } catch (e: Exception) {
            Toast.makeText(this, "خطأ في البيانات، يرجى إعادة التعيين", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun createRowItemView(index: Int): View {
        val rowLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, dpToPx(8)) }
            setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12))
            setBackgroundColor(Color.WHITE)
            elevation = dpToPx(2).toFloat()
            gravity = Gravity.CENTER_VERTICAL
        }

        val txtTitle = TextView(this).apply {
            text = "الصف رقم ${index + 1}"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        
        txtTitle.setOnClickListener { openRowEditor(index) }
        rowLayout.addView(txtTitle)
        
        // زر التحريك للأعلى
        if (index > 0) {
            val btnUp = createActionButton("▲") { moveRow(index, -1) }
            rowLayout.addView(btnUp)
        }

        // زر التحريك للأسفل
        if (index < jsonArray.length() - 1) {
            val btnDown = createActionButton("▼") { moveRow(index, 1) }
            rowLayout.addView(btnDown)
        }

        // زر الحذف
        val btnDelete = createActionButton("❌") { confirmDeleteRow(index) }
        btnDelete.setTextColor(Color.RED)
        rowLayout.addView(btnDelete)

        rowLayout.setOnClickListener { openRowEditor(index) }

        return rowLayout
    }
    
    private fun createActionButton(text: String, onClick: () -> Unit): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 20f
            setPadding(dpToPx(12), 0, dpToPx(12), 0)
            setOnClickListener { onClick() }
        }
    }

    private fun openRowEditor(index: Int) {
        val intent = Intent(this, RowEditorActivity::class.java)
        intent.putExtra("LANG_CODE", currentLangCode)
        // نمرر الـ Index كنص لأن RowEditorActivity يتوقع String حالياً
        intent.putExtra("ROW_KEY", index.toString()) 
        startActivity(intent)
    }

    // === دوال التعديل الفعلي ===

    private fun addNewRow() {
        val newRow = JSONObject()
        newRow.put("height", 50.0)
        newRow.put("keys", JSONArray()) // مفاتيح فارغة
        
        jsonArray.put(newRow)
        saveChanges()
    }

    private fun confirmDeleteRow(index: Int) {
        AlertDialog.Builder(this)
            .setTitle("حذف الصف")
            .setMessage("هل أنت متأكد من حذف الصف ${index + 1}؟")
            .setPositiveButton("حذف") { _, _ ->
                // إزالة العنصر من المصفوفة (تتطلب API 19+)
                jsonArray.remove(index)
                saveChanges()
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }
    
    private fun moveRow(index: Int, direction: Int) {
        val newIndex = index + direction
        if (newIndex in 0 until jsonArray.length()) {
            val temp = jsonArray.get(index)
            jsonArray.put(index, jsonArray.get(newIndex))
            jsonArray.put(newIndex, temp)
            saveChanges()
        }
    }

    private fun saveChanges() {
        layoutDatabase.updateLayout(currentLangCode, jsonArray.toString())
        loadRowsList() // إعادة رسم الواجهة لتعكس التغييرات
    }

    // ... (دوال التصدير والاستيراد و resetToDefault كما هي في الرد السابق) ...
    
    private fun resetToDefault() {
        AlertDialog.Builder(this)
            .setTitle("تأكيد")
            .setMessage("استعادة الافتراضي؟")
            .setPositiveButton("نعم") { _, _ ->
                layoutDatabase.resetSingleLayoutToDefault(currentLangCode)
                loadRowsList()
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }
    
    // ... (startExportFile, startImportFile, onActivityResult كما هي) ...
    private fun startExportFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "layout_$currentLangCode.json")
        }
        startActivityForResult(intent, REQUEST_CODE_EXPORT)
    }

    private fun startImportFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        startActivityForResult(intent, REQUEST_CODE_IMPORT)
    }
    
    private fun saveJsonToFile(uri: Uri) {
        try {
            val jsonString = layoutDatabase.getLayoutByLang(currentLangCode)
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray())
            }
            Toast.makeText(this, "تم الحفظ", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "فشل الحفظ", Toast.LENGTH_LONG).show()
        }
    }

    private fun readJsonFromFile(uri: Uri) {
        try {
            val stringBuilder = StringBuilder()
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String? = reader.readLine()
                    while (line != null) {
                        stringBuilder.append(line)
                        line = reader.readLine()
                    }
                }
            }
            val newJson = stringBuilder.toString()
            // تحقق بسيط
            if (newJson.trim().startsWith("[")) {
                layoutDatabase.updateLayout(currentLangCode, newJson)
                loadRowsList()
                Toast.makeText(this, "تم الاستيراد", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "الملف يجب أن يكون مصفوفة []", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "فشل القراءة", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            data.data?.let { uri ->
                when (requestCode) {
                    REQUEST_CODE_EXPORT -> saveJsonToFile(uri)
                    REQUEST_CODE_IMPORT -> readJsonFromFile(uri)
                }
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        layoutDatabase.close()
    }
}