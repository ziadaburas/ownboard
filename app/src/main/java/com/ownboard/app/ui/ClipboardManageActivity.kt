package com.ownboard.app.ui

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
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
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ownboard.app.R
import com.ownboard.app.db.ClipboardDbHelper
import com.ownboard.app.db.ClipboardItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ClipboardManageActivity : Activity() {

    private lateinit var dbHelper: ClipboardDbHelper
    private lateinit var adapter: ClipboardManageAdapter
    private lateinit var etSearch: EditText
    private lateinit var cbCaseSensitive: CheckBox
    private lateinit var btnFilterDate: ImageButton

    // متغيرات لحفظ التواريخ المختارة للفلتر
    private var filterStartTimestamp: Long = 0
    private var filterEndTimestamp: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clipboard_manage)

        dbHelper = ClipboardDbHelper(this)

        etSearch = findViewById(R.id.et_search)
        cbCaseSensitive = findViewById(R.id.cb_case_sensitive)
        btnFilterDate = findViewById(R.id.btn_filter_date)
        
        val recyclerView = findViewById<RecyclerView>(R.id.rv_clipboard_manage)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        adapter = ClipboardManageAdapter { item ->
            showEditDialog(item)
        }
        recyclerView.adapter = adapter

        loadData()

        // مستمع البحث
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { performSearch() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        cbCaseSensitive.setOnCheckedChangeListener { _, _ -> performSearch() }

        // مستمع زر الفلتر
        btnFilterDate.setOnClickListener {
            showDateFilterDialog()
        }
    }

    private fun loadData() {
        val items = dbHelper.getClipboardItems()
        adapter.updateList(items)
        performSearch()
    }

    private fun performSearch() {
        val query = etSearch.text.toString()
        val isCaseSensitive = cbCaseSensitive.isChecked
        adapter.filter(query, isCaseSensitive)
    }

    // دالة عرض نافذة الفلتر
    private fun showDateFilterDialog() {
        val context = this
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            // تم حذف السطر المسبب للخطأ (padding = 50)
            setPadding(40, 40, 40, 40)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val dateFormatter = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        
        // إعداد التاريخ الافتراضي (اليوم)
        val calendar = Calendar.getInstance()
        
        // أزرار اختيار التاريخ
        val btnStartDate = Button(context).apply { 
            text = "من تاريخ: اضغط للاختيار"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.DKGRAY)
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
            setBackgroundColor(Color.DKGRAY)
             val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 20)
            layoutParams = params
        }

        // متغيرات مؤقتة للديالوج
        var tempStart: Long = 0
        var tempEnd: Long = System.currentTimeMillis()

        btnStartDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(context, { _, year, month, day ->
                val cal = Calendar.getInstance()
                cal.set(year, month, day, 0, 0, 0) // بداية اليوم
                tempStart = cal.timeInMillis
                val date = cal.time
                btnStartDate.text = "من: ${dateFormatter.format(date)}"
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            datePickerDialog.show()
        }

        btnEndDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(context, { _, year, month, day ->
                val cal = Calendar.getInstance()
                cal.set(year, month, day, 23, 59, 59) // نهاية اليوم
                tempEnd = cal.timeInMillis
                val date = cal.time
                btnEndDate.text = "إلى: ${dateFormatter.format(date)}"
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            datePickerDialog.show()
        }

        layout.addView(btnStartDate)
        layout.addView(btnEndDate)

        AlertDialog.Builder(context)
            .setTitle("فلترة حسب التاريخ")
            .setView(layout)
            .setPositiveButton("تطبيق") { _, _ ->
                if (tempStart > 0) {
                    adapter.filterByDate(tempStart, tempEnd)
                    Toast.makeText(context, "تم تطبيق الفلتر", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "الرجاء اختيار تاريخ البداية", Toast.LENGTH_SHORT).show()
                }
            }
            .setNeutralButton("عرض الكل") { _, _ ->
                adapter.showAll()
                etSearch.setText("") 
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun showEditDialog(item: ClipboardItem) {
        val editText = EditText(this).apply {
            setText(item.text)
            setTextColor(Color.BLACK)
            setPadding(40, 40, 40, 40)
            background = null
        }

        AlertDialog.Builder(this)
            .setTitle("تعديل النص")
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
            .show()
    }
}