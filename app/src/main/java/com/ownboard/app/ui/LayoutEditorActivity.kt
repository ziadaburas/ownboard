package com.ownboard.app.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.ownboard.app.R
import com.ownboard.app.db.LayoutDatabase
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Collections

class LayoutEditorActivity : Activity() {

    private lateinit var layoutDatabase: LayoutDatabase
    private lateinit var rowsContainer: LinearLayout
    private lateinit var editorTitle: TextView
    private var currentLangCode: String = ""

    // أكواد لتمييز الطلبات
    private val REQUEST_CODE_EXPORT = 101
    private val REQUEST_CODE_IMPORT = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layout_editor)

        currentLangCode = intent.getStringExtra("LANG_CODE") ?: return finish()

        layoutDatabase = LayoutDatabase(this)
        initViews()
        setupButtons()
        loadRowsList()
    }

    private fun initViews() {
        rowsContainer = findViewById(R.id.rows_container)
        editorTitle = findViewById(R.id.editor_title)
        editorTitle.text = "تعديل تخطيط: ${getLocalizedLangName(currentLangCode)} ($currentLangCode)"
    }

    private fun setupButtons() {
        // تغيير الأزرار لفتح الملفات بدلاً من الديالوج
        findViewById<Button>(R.id.btn_export_json).setOnClickListener { startExportFile() }
        findViewById<Button>(R.id.btn_import_json).setOnClickListener { startImportFile() }
        findViewById<Button>(R.id.btn_reset_lang).setOnClickListener { resetToDefault() }
    }

    // ==========================================
    // 1. منطق التصدير (Export) - حفظ ملف
    // ==========================================
    private fun startExportFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json" // نوع الملف
            putExtra(Intent.EXTRA_TITLE, "layout_$currentLangCode.json") // اسم الملف الافتراضي
        }
        startActivityForResult(intent, REQUEST_CODE_EXPORT)
    }

    private fun saveJsonToFile(uri: Uri) {
        try {
            val jsonString = layoutDatabase.getLayoutByLang(currentLangCode)
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray())
            }
            Toast.makeText(this, "تم حفظ الملف بنجاح", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "فشل الحفظ: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    // ==========================================
    // 2. منطق الاستيراد (Import) - فتح ملف
    // ==========================================
    private fun startImportFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json" // تصفية لملفات JSON فقط
            // بعض الأجهزة قد لا تتعرف على mime type للـ json، يمكن إضافة هذا السطر للاحتياط:
            // type = "*/*" 
        }
        startActivityForResult(intent, REQUEST_CODE_IMPORT)
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
            
            // التحقق والحفظ
            if (validateAndSaveJson(newJson)) {
                loadRowsList() // تحديث الشاشة
                Toast.makeText(this, "تم استيراد الملف وتحديث التخطيط!", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Toast.makeText(this, "فشل قراءة الملف: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    // ==========================================
    // 3. استقبال النتائج (onActivityResult)
    // ==========================================
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

    // ==========================================
    // بقية الدوال (عرض الصفوف وغيرها)
    // ==========================================
    private fun loadRowsList() {
        rowsContainer.removeAllViews()
        val jsonString = layoutDatabase.getLayoutByLang(currentLangCode)
        if (jsonString.isEmpty()) return

        try {
            val jsonObject = JSONObject(jsonString)
            val keysIterator = jsonObject.keys()
            val rowsList = mutableListOf<String>()
            while (keysIterator.hasNext()) {
                rowsList.add(keysIterator.next())
            }
            Collections.sort(rowsList)

            rowsList.forEach { rowKey ->
                rowsContainer.addView(createRowItemView(rowKey))
            }

        } catch (e: Exception) {
            Toast.makeText(this, "خطأ JSON: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createRowItemView(rowName: String): TextView {
        return TextView(this).apply {
            text = rowName
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            
            // الحفاظ على إصلاح الـ Padding الذي طلبته سابقاً
            val p = dpToPx(16)
            setPadding(p, p, p, p)
            
            setBackgroundColor(Color.WHITE)
            gravity = Gravity.CENTER_VERTICAL
            elevation = dpToPx(2).toFloat()
            
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, dpToPx(8))
            layoutParams = params

            setOnClickListener {
                val intent = android.content.Intent(this@LayoutEditorActivity, RowEditorActivity::class.java)
                 intent.putExtra("LANG_CODE", currentLangCode)
                 intent.putExtra("ROW_KEY", rowName)
                 startActivity(intent)
            }
        }
    }

    private fun validateAndSaveJson(jsonString: String): Boolean {
        return try {
            if (jsonString.isBlank()) throw Exception("الملف فارغ")
            JSONObject(jsonString) // فحص الصلاحية
            layoutDatabase.updateLayout(currentLangCode, jsonString)
            true
        } catch (e: Exception) {
            Toast.makeText(this, "ملف JSON غير صالح: ${e.message}", Toast.LENGTH_LONG).show()
            false
        }
    }

    private fun resetToDefault() {
        AlertDialog.Builder(this)
            .setTitle("تأكيد إعادة التعيين")
            .setMessage("هل أنت متأكد من استعادة التخطيط الافتراضي للغة ($currentLangCode)؟")
            .setPositiveButton("نعم") { _, _ ->
                layoutDatabase.resetSingleLayoutToDefault(currentLangCode)
                loadRowsList()
                Toast.makeText(this, "تمت الاستعادة", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun getLocalizedLangName(code: String): String {
        return when (code) {
            "ar" -> "العربية"
            "en" -> "English"
            else -> code.uppercase()
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