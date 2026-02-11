package com.ownboard.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.ownboard.app.ui.* // تأكد أن هذا يستورد GlobalSettingsActivity

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // تعريف الأزرار
        val btnManage = findViewById<Button>(R.id.btn_manage_layouts)
        val btnClipboard = findViewById<Button>(R.id.btn_manage_clipboard)
        val btnSettings = findViewById<Button>(R.id.btn_global_settings) // الزر الجديد
        
        // 1. الانتقال لشاشة إدارة التخطيطات
        btnManage.setOnClickListener {
            val intent = Intent(this, ManageLayoutsActivity::class.java)
            startActivity(intent)
        }

        // 2. الانتقال لشاشة إدارة الحافظة
        btnClipboard.setOnClickListener {
            val intent = Intent(this, ClipboardManageActivity::class.java)
            startActivity(intent)
        }

        // 3. الانتقال لشاشة الإعدادات العامة (الديناميكية)
        btnSettings.setOnClickListener {
            val intent = Intent(this, GlobalSettingsActivity::class.java)
            startActivity(intent)
        }
    }
}