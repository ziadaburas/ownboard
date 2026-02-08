package com.ownboard.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.ownboard.app.ui.*

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnManage = findViewById<Button>(R.id.btn_manage_layouts)
        
        btnManage.setOnClickListener {
            val intent = Intent(this, ManageLayoutsActivity::class.java)
            startActivity(intent)
        }
    }
}