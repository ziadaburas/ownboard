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
    
    // UI Elements - Basics
    private lateinit var inputText: EditText
    private lateinit var inputHint: EditText
    private lateinit var inputWeight: EditText
    
    // Spinners (Functions)
    private lateinit var spinnerClick: Spinner
    private lateinit var spinnerLongPress: Spinner
    private lateinit var spinnerHorizontal: Spinner // New: Horizontal Swipe
    private lateinit var spinnerVertical: Spinner   // New: Vertical Swipe

    // Params - Click (Default/General)
    private lateinit var inputTextClick: EditText
    private lateinit var inputCodeClick: EditText
    
    // Params - Long Press
    private lateinit var inputTextLong: EditText
    private lateinit var inputCodeLong: EditText
    
    // Params - Horizontal Swipe
    private lateinit var inputTextHorizontal: EditText
    private lateinit var inputCodeHorizontal: EditText 
    
    // Params - Vertical Swipe
    private lateinit var inputTextVertical: EditText
    private lateinit var inputCodeVertical: EditText 

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
        // Basics
        inputText = findViewById(R.id.input_text)
        inputHint = findViewById(R.id.input_hint)
        inputWeight = findViewById(R.id.input_weight)
        
        // Click Section
        spinnerClick = findViewById(R.id.spinner_click)
        inputTextClick = findViewById(R.id.input_text_click)
        inputCodeClick = findViewById(R.id.input_code_click)
        
        // Long Press Section
        spinnerLongPress = findViewById(R.id.spinner_long_press)
        inputTextLong = findViewById(R.id.input_text_long)
        inputCodeLong = findViewById(R.id.input_code_long)
        
        // Horizontal Swipe Section (New)
        spinnerHorizontal = findViewById(R.id.spinner_horizontal)
        inputTextHorizontal = findViewById(R.id.input_text_horizontal)
        inputCodeHorizontal = findViewById(R.id.input_code_horizontal)
        
        // Vertical Swipe Section (New)
        spinnerVertical = findViewById(R.id.spinner_vertical)
        inputTextVertical = findViewById(R.id.input_text_vertical)
        inputCodeVertical = findViewById(R.id.input_code_vertical)
    }

    private fun setupSpinners() {
        // Click Functions
        val clickAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, All.CLICK_FUNCTIONS)
        spinnerClick.adapter = clickAdapter

        // Long Press Functions
        val longAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, All.LONG_PRESS_FUNCTIONS)
        spinnerLongPress.adapter = longAdapter

        // Swipe Functions (Used for both Horizontal and Vertical)
        val swipeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, All.SWIPE_FUNCTIONS)
        spinnerHorizontal.adapter = swipeAdapter
        spinnerVertical.adapter = swipeAdapter
    }

    private fun loadKeyData() {
        val jsonString = layoutDatabase.getLayoutByLang(langCode)
        try {
            fullJsonObj = JSONObject(jsonString)
            val rowObj = fullJsonObj!!.getJSONObject(rowKey)
            val keysArray = rowObj.getJSONArray("keys")
            keyObj = keysArray.getJSONObject(keyIndex)

            // 1. Basic Info
            inputText.setText(keyObj!!.optString("text", ""))
            inputHint.setText(keyObj!!.optString("hint", ""))
            inputWeight.setText(keyObj!!.optDouble("weight", 1.0).toString())

            // 2. Function Selectors
            setSpinnerSelection(spinnerClick, All.CLICK_FUNCTIONS, keyObj!!.optString("click", ""))
            setSpinnerSelection(spinnerLongPress, All.LONG_PRESS_FUNCTIONS, keyObj!!.optString("longPress", ""))
            setSpinnerSelection(spinnerHorizontal, All.SWIPE_FUNCTIONS, keyObj!!.optString("horizontalSwipe", ""))
            setSpinnerSelection(spinnerVertical, All.SWIPE_FUNCTIONS, keyObj!!.optString("verticalSwipe", ""))

            // 3. Parsing the 'params' Map
            val params = keyObj!!.optJSONObject("params") ?: JSONObject()

            // Click Params (text, code)
            inputTextClick.setText(params.optString("text", ""))
            val code = params.optInt("code", 0)
            inputCodeClick.setText(if (code != 0) code.toString() else "")
            
            // Long Press Params (lpText, lpCode)
            inputTextLong.setText(params.optString("lpText", ""))
            val lpCode = params.optInt("lpCode", 0)
            inputCodeLong.setText(if (lpCode != 0) lpCode.toString() else "")
            
            // Horizontal Params (hText, hCode)
            inputTextHorizontal.setText(params.optString("hText", ""))
            val hCode = params.optInt("hCode", 0)
            inputCodeHorizontal.setText(if (hCode != 0) hCode.toString() else "")

            // Vertical Params (vText, vCode)
            inputTextVertical.setText(params.optString("vText", ""))
            val vCode = params.optInt("vCode", 0)
            inputCodeVertical.setText(if (vCode != 0) vCode.toString() else "")

        } catch (e: Exception) {
            Toast.makeText(this, "خطأ في تحميل البيانات: ${e.message}", Toast.LENGTH_SHORT).show()
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
            // 1. Save Basic Info
            keyObj!!.put("text", inputText.text.toString())
            keyObj!!.put("hint", inputHint.text.toString())
            keyObj!!.put("weight", inputWeight.text.toString().toDoubleOrNull() ?: 1.0)

            // 2. Save Function Selectors
            keyObj!!.put("click", spinnerClick.selectedItem.toString())
            keyObj!!.put("longPress", spinnerLongPress.selectedItem.toString())
            keyObj!!.put("horizontalSwipe", spinnerHorizontal.selectedItem.toString())
            keyObj!!.put("verticalSwipe", spinnerVertical.selectedItem.toString())

            // 3. Construct and Save 'params' Map
            val params = JSONObject()

            // Helper to add if not empty/zero
            fun addToParams(key: String, value: String) {
                if (value.isNotEmpty()) params.put(key, value)
            }
            fun addToParams(key: String, value: Int) {
                if (value != 0) params.put(key, value)
            }

            // Click Params
            addToParams("text", inputTextClick.text.toString())
            addToParams("code", inputCodeClick.text.toString().toIntOrNull() ?: 0)
            
            // Long Press Params
            addToParams("lpText", inputTextLong.text.toString())
            addToParams("lpCode", inputCodeLong.text.toString().toIntOrNull() ?: 0)
            
            // Horizontal Params
            addToParams("hText", inputTextHorizontal.text.toString())
            addToParams("hCode", inputCodeHorizontal.text.toString().toIntOrNull() ?: 0)

            // Vertical Params
            addToParams("vText", inputTextVertical.text.toString())
            addToParams("vCode", inputCodeVertical.text.toString().toIntOrNull() ?: 0)

            // Save params object to key object
            keyObj!!.put("params", params)

            // 4. Update Database
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