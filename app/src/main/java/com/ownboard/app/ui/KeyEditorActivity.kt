package com.ownboard.app.ui

import android.app.Activity
import android.os.Bundle
import android.widget.*
import com.ownboard.app.All
import com.ownboard.app.R
import com.ownboard.app.db.LayoutDatabase
import org.json.JSONObject

class KeyEditorActivity : Activity() {

    private lateinit var layoutDatabase: LayoutDatabase
    
    // UI Elements
    private lateinit var inputText: EditText
    private lateinit var inputHint: EditText
    private lateinit var inputWeight: EditText
    
    // Spinners
    private lateinit var spinnerClick: Spinner
    private lateinit var spinnerLongPress: Spinner
    private lateinit var spinnerSwipeLeft: Spinner
    private lateinit var spinnerSwipeRight: Spinner

    // Params Inputs
    private lateinit var inputTextClick: EditText
    private lateinit var inputCodeClick: EditText
    
    private lateinit var inputTextLong: EditText
    private lateinit var inputCodeLong: EditText
    
    // Swipe Inputs
    private lateinit var inputTextLeft: EditText
    private lateinit var inputCodeLeft: EditText 
    
    private lateinit var inputTextRight: EditText
    private lateinit var inputCodeRight: EditText 

    private var langCode = ""
    private var rowKey = ""
    private var keyIndex = -1
    private var fullJsonObj: JSONObject? = null
    private var keyObj: JSONObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_key_editor)

        langCode = intent.getStringExtra("LANG_CODE") ?: return finish()
        rowKey = intent.getStringExtra("ROW_KEY") ?: return finish()
        keyIndex = intent.getIntExtra("KEY_INDEX", -1)
        if (keyIndex == -1) return finish()

        layoutDatabase = LayoutDatabase(this)
        
        initViews()
        setupSpinners()
        loadKeyData()

        findViewById<Button>(R.id.btn_save_key).setOnClickListener {
            saveKeyData()
        }
    }

    private fun initViews() {
        inputText = findViewById(R.id.input_text)
        inputHint = findViewById(R.id.input_hint)
        inputWeight = findViewById(R.id.input_weight)
        
        spinnerClick = findViewById(R.id.spinner_click)
        inputTextClick = findViewById(R.id.input_text_click)
        inputCodeClick = findViewById(R.id.input_code_click)
        
        spinnerLongPress = findViewById(R.id.spinner_long_press)
        inputTextLong = findViewById(R.id.input_text_long)
        inputCodeLong = findViewById(R.id.input_code_long)
        
        // === التصحيح هنا: إضافة تعريف حقول الكود للسحب ===
        spinnerSwipeLeft = findViewById(R.id.spinner_swipe_left)
        inputTextLeft = findViewById(R.id.input_text_left)
        inputCodeLeft = findViewById(R.id.input_code_left) // هذا السطر كان ناقصاً
        
        spinnerSwipeRight = findViewById(R.id.spinner_swipe_right)
        inputTextRight = findViewById(R.id.input_text_right)
        inputCodeRight = findViewById(R.id.input_code_right) // وهذا السطر كان ناقصاً
    }

    private fun setupSpinners() {
        val clickAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, All.CLICK_FUNCTIONS)
        spinnerClick.adapter = clickAdapter

        val longAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, All.LONG_PRESS_FUNCTIONS)
        spinnerLongPress.adapter = longAdapter

        val scrollAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, All.SCROLL_FUNCTIONS)
        spinnerSwipeLeft.adapter = scrollAdapter
        spinnerSwipeRight.adapter = scrollAdapter
    }

    private fun loadKeyData() {
        val jsonString = layoutDatabase.getLayoutByLang(langCode)
        try {
            fullJsonObj = JSONObject(jsonString)
            val rowObj = fullJsonObj!!.getJSONObject(rowKey)
            val keysArray = rowObj.getJSONArray("keys")
            keyObj = keysArray.getJSONObject(keyIndex)

            inputText.setText(keyObj!!.optString("text", ""))
            inputHint.setText(keyObj!!.optString("hint", ""))
            inputWeight.setText(keyObj!!.optDouble("weight", 1.0).toString())

            setSpinnerSelection(spinnerClick, All.CLICK_FUNCTIONS, keyObj!!.optString("click", ""))
            setSpinnerSelection(spinnerLongPress, All.LONG_PRESS_FUNCTIONS, keyObj!!.optString("longPress", ""))
            setSpinnerSelection(spinnerSwipeLeft, All.SCROLL_FUNCTIONS, keyObj!!.optString("leftScroll", ""))
            setSpinnerSelection(spinnerSwipeRight, All.SCROLL_FUNCTIONS, keyObj!!.optString("rightScroll", ""))

            inputTextClick.setText(keyObj!!.optString("textToSend", ""))
            inputCodeClick.setText(keyObj!!.optInt("codeToSendClick", 0).takeIf { it != 0 }?.toString() ?: "")
            
            inputTextLong.setText(keyObj!!.optString("textToSendLongPress", ""))
            inputCodeLong.setText(keyObj!!.optInt("codeToSendLongPress", 0).takeIf { it != 0 }?.toString() ?: "")
            
            // تعبئة حقول السحب
            inputTextLeft.setText(keyObj!!.optString("textToSendLeftScroll", ""))
            inputCodeLeft.setText(keyObj!!.optInt("codeToSendLeftScroll", 0).takeIf { it != 0 }?.toString() ?: "")

            inputTextRight.setText(keyObj!!.optString("textToSendRightScroll", ""))
            inputCodeRight.setText(keyObj!!.optInt("codeToSendRightScroll", 0).takeIf { it != 0 }?.toString() ?: "")

        } catch (e: Exception) {
            Toast.makeText(this, "خطأ: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setSpinnerSelection(spinner: Spinner, list: List<String>, value: String) {
        val index = list.indexOf(value)
        if (index >= 0) {
            spinner.setSelection(index)
        }
    }

    private fun saveKeyData() {
        try {
            keyObj!!.put("text", inputText.text.toString())
            keyObj!!.put("hint", inputHint.text.toString())
            keyObj!!.put("weight", inputWeight.text.toString().toDoubleOrNull() ?: 1.0)

            keyObj!!.put("click", spinnerClick.selectedItem.toString())
            keyObj!!.put("longPress", spinnerLongPress.selectedItem.toString())
            keyObj!!.put("leftScroll", spinnerSwipeLeft.selectedItem.toString())
            keyObj!!.put("rightScroll", spinnerSwipeRight.selectedItem.toString())

            keyObj!!.put("textToSend", inputTextClick.text.toString())
            keyObj!!.put("codeToSendClick", inputCodeClick.text.toString().toIntOrNull() ?: 0)
            
            keyObj!!.put("textToSendLongPress", inputTextLong.text.toString())
            keyObj!!.put("codeToSendLongPress", inputCodeLong.text.toString().toIntOrNull() ?: 0)
            
            // حفظ حقول السحب
            keyObj!!.put("textToSendLeftScroll", inputTextLeft.text.toString())
            keyObj!!.put("codeToSendLeftScroll", inputCodeLeft.text.toString().toIntOrNull() ?: 0)

            keyObj!!.put("textToSendRightScroll", inputTextRight.text.toString())
            keyObj!!.put("codeToSendRightScroll", inputCodeRight.text.toString().toIntOrNull() ?: 0)

            val rowObj = fullJsonObj!!.getJSONObject(rowKey)
            val keysArray = rowObj.getJSONArray("keys")
            keysArray.put(keyIndex, keyObj)
            
            layoutDatabase.updateLayout(langCode, fullJsonObj!!.toString())
            
            Toast.makeText(this, "تم تحديث الزر بنجاح", Toast.LENGTH_SHORT).show()
            finish()

        } catch (e: Exception) {
            Toast.makeText(this, "فشل الحفظ: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}