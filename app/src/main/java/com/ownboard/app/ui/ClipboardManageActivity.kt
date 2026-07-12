package com.ownboard.app.ui

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ownboard.app.R
import com.ownboard.app.db.ClipboardDbHelper
import com.ownboard.app.db.ClipboardItem
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ClipboardManageActivity : Activity() {

    private lateinit var dbHelper: ClipboardDbHelper
    private lateinit var adapter: ClipboardManageAdapter
    private lateinit var etSearch: EditText
    private lateinit var cbCaseSensitive: CheckBox
    private lateinit var btnFilterDate: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView

    private var filterStartTimestamp: Long = 0
    private var filterEndTimestamp: Long = 0

    // أكواد طلب التصاريح للتصدير والاستيراد
    private val REQUEST_CODE_EXPORT = 201
    private val REQUEST_CODE_IMPORT = 202

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clipboard_manage)

        dbHelper = ClipboardDbHelper(this)

        etSearch = findViewById(R.id.et_search)
        cbCaseSensitive = findViewById(R.id.cb_case_sensitive)
        btnFilterDate = findViewById(R.id.btn_filter_date)
        progressBar = findViewById(R.id.progress_bar)
        recyclerView = findViewById(R.id.rv_clipboard_manage)
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        adapter = ClipboardManageAdapter { item ->
            showEditDialog(item)
        }
        recyclerView.adapter = adapter

        loadData()

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { performSearch() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        cbCaseSensitive.setOnCheckedChangeListener { _, _ -> performSearch() }

        btnFilterDate.setOnClickListener {
            showDateFilterDialog()
        }

        // أزرار الاستيراد والتصدير الجديدة
        findViewById<Button>(R.id.btn_export_clipboard).setOnClickListener { startExportFile() }
        findViewById<Button>(R.id.btn_import_clipboard).setOnClickListener { startImportFile() }
    }

    private fun loadData() {
        val items = dbHelper.getClipboardItems()
        adapter.updateList(items)
        performSearch()
    }

    private fun performSearch() {
        val query = etSearch.text.toString()
        val isCaseSensitive = cbCaseSensitive.isChecked
        
        recyclerView.visibility = View.INVISIBLE
        progressBar.visibility = View.VISIBLE
        
        adapter.filter(query, isCaseSensitive) {
            progressBar.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    // ==========================================
    // ===== دوال التصدير والاستيراد =====
    // ==========================================

    private fun startExportFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            val timeString = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            putExtra(Intent.EXTRA_TITLE, "clipboard_$timeString.json")
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
            val items = dbHelper.getClipboardItems()
            val jsonArray = JSONArray()
            
            for (item in items) {
                val obj = JSONObject()
                obj.put("text", item.text)
                obj.put("isPinned", item.isPinned)
                obj.put("timestamp", item.timestamp)
                jsonArray.put(obj)
            }
            
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonArray.toString().toByteArray())
            }
            Toast.makeText(this, "تم التصدير بنجاح", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "فشل التصدير", Toast.LENGTH_LONG).show()
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
            val jsonString = stringBuilder.toString()
            val jsonArray = JSONArray(jsonString)
            
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val text = obj.getString("text")
                val isPinned = obj.optBoolean("isPinned", false)
                val timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                
                dbHelper.importClip(text, isPinned, timestamp)
            }
            loadData() // تحديث القائمة بعد الاستيراد
            Toast.makeText(this, "تم الاستيراد بنجاح", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "فشل الاستيراد أو صيغة الملف غير صحيحة", Toast.LENGTH_LONG).show()
            e.printStackTrace()
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
    
    // ==========================================

    private fun showDateFilterDialog() {
        val context = this
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val dateFormatter = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        
        // تصميم موحد للأزرار الداخلية
        val innerButtonBackground = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            cornerRadius = 15f
            setColor(Color.parseColor("#FF2D2D2D"))
        }

        val btnStartDate = Button(context).apply { 
            text = "من تاريخ: اضغط للاختيار"
            setTextColor(Color.WHITE)
            background = innerButtonBackground // استخدام التصميم الجديد
             val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 20)
            layoutParams = params
        }
        
        val btnEndDate = Button(context).apply { 
            text = "إلى تاريخ: اضغط للاختيار"
            setTextColor(Color.WHITE)
            background = innerButtonBackground // استخدام التصميم الجديد
             val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 20)
            layoutParams = params
        }

        var tempStart: Long = 0
        var tempEnd: Long = System.currentTimeMillis()

        btnStartDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(context, { _, year, month, day ->
                val cal = Calendar.getInstance()
                cal.set(year, month, day, 0, 0, 0)
                tempStart = cal.timeInMillis
                val date = cal.time
                btnStartDate.text = "من: ${dateFormatter.format(date)}"
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            datePickerDialog.show()
        }

        btnEndDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(context, { _, year, month, day ->
                val cal = Calendar.getInstance()
                cal.set(year, month, day, 23, 59, 59)
                tempEnd = cal.timeInMillis
                val date = cal.time
                btnEndDate.text = "إلى: ${dateFormatter.format(date)}"
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            datePickerDialog.show()
        }

        layout.addView(btnStartDate)
        layout.addView(btnEndDate)

        // 1. عنوان مخصص باللون الأبيض
        val customTitle = android.widget.TextView(context).apply {
            text = "فلترة حسب التاريخ"
            setTextColor(Color.WHITE)
            textSize = 20f
            setPadding(50, 50, 50, 20)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val dialog = AlertDialog.Builder(context)
            .setCustomTitle(customTitle)
            .setView(layout)
            .setPositiveButton("تطبيق") { _, _ ->
                if (tempStart > 0) {
                    recyclerView.visibility = View.INVISIBLE
                    progressBar.visibility = View.VISIBLE
                    
                    adapter.filterByDate(tempStart, tempEnd) {
                        progressBar.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        Toast.makeText(context, "تم تطبيق الفلتر", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "الرجاء اختيار تاريخ البداية", Toast.LENGTH_SHORT).show()
                }
            }
            .setNeutralButton("عرض الكل") { _, _ ->
                adapter.showAll()
                etSearch.setText("") 
            }
            .setNegativeButton("إلغاء", null)
            .create() // إنشاء بدلاً من العرض المباشر

        // 2. تغيير لون خلفية الدايلوج إلى #222222
        val dialogBackground = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            cornerRadius = 40f
            setColor(Color.parseColor("#222222"))
        }
        dialog.window?.setBackgroundDrawable(dialogBackground)

        // 3. عرض الدايلوج أولاً
        dialog.show()

        // 4. تغيير ألوان أزرار الأكشن (تطبيق، عرض الكل، إلغاء)
        val btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val btnNegative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        val btnNeutral = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)

        val buttons = listOf(btnPositive, btnNegative, btnNeutral)
        for (btn in buttons) {
            btn?.apply {
                setTextColor(Color.WHITE) 
                
                background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                    cornerRadius = 15f
                    setColor(Color.parseColor("#FF2D2D2D"))
                }
                
                val params = layoutParams as LinearLayout.LayoutParams
                params.setMargins(15, 0, 15, 0)
                layoutParams = params
            }
        }
    }
    private fun showEditDialog(item: ClipboardItem) {
        val editText = EditText(this).apply {
            setText(item.text)
            setPadding(40, 40, 40, 40)
            
            // جعل نص حقل الإدخال أبيض ليتناسب مع الخلفية الداكنة
            setTextColor(Color.WHITE)
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 20f
                setColor(Color.parseColor("#FF2D2D2D"))
            }
            
            val query = etSearch.text.toString()
            if (query.isNotEmpty()) {
                val isCaseSensitive = cbCaseSensitive.isChecked
                val startIndex = item.text.indexOf(query, ignoreCase = !isCaseSensitive)
                
                if (startIndex != -1) {
                    setSelection(startIndex, startIndex + query.length)
                } else {
                    setSelection(item.text.length)
                }
            } else {
                setSelection(item.text.length)
            }
        }

        // 1. إنشاء عنوان مخصص ليظهر باللون الأبيض
        val customTitle = android.widget.TextView(this).apply {
            text = "تعديل النص"
            setTextColor(Color.WHITE)
            textSize = 20f
            setPadding(50, 50, 50, 20)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val dialog = AlertDialog.Builder(this)
            .setCustomTitle(customTitle) // استخدام العنوان المخصص
            .setView(editText)
            .setPositiveButton("حفظ") { _, _ ->
                val newText = editText.text.toString()
                if (newText.isNotBlank()) {
                    val success = dbHelper.updateClipText(item.id, newText)
                    if (success) {
                        Toast.makeText(this, "تم التعديل بنجاح", Toast.LENGTH_SHORT).show()
                        loadData()
                    } else {
                        Toast.makeText(this, "فشل التعديل", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("إلغاء", null)
            .setNeutralButton("حذف") { _, _ ->
                dbHelper.deleteText(item.text)
                loadData()
                Toast.makeText(this, "تم الحذف", Toast.LENGTH_SHORT).show()
            }
            .create() 

        // 2. تغيير لون خلفية الدايلوج إلى #222222 مع زوايا دائرية
        val dialogBackground = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            cornerRadius = 40f
            setColor(Color.parseColor("#FF2D2D2D"))
        }
        dialog.window?.setBackgroundDrawable(dialogBackground)

        dialog.setOnShowListener {
            editText.requestFocus()
        }
        
        // 3. يجب عرض الدايلوج أولاً قبل التعديل على أزراره
        dialog.show()

        // 4. تغيير لون خلفية ونص الأزرار
        val btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val btnNegative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        val btnNeutral = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)

        val buttons = listOf(btnPositive, btnNegative, btnNeutral)
        for (btn in buttons) {
            btn?.apply {
                setTextColor(Color.WHITE) // لون النص أبيض
                
                // إضافة خلفية #FF2D2D2D للأزرار مع زوايا دائرية خفيفة
                background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                    cornerRadius = 15f
                    setColor(Color.parseColor("#222222"))
                }
                
                // إضافة هوامش (Margins) لإبعاد الأزرار عن بعضها قليلاً
                val params = layoutParams as LinearLayout.LayoutParams
                params.setMargins(5, 0, 5, 0)
                layoutParams = params
            }
        }
    }
   
}