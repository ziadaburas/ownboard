package com.ownboard.app

import android.content.ClipboardManager
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.KeyEvent
import android.widget.*
import android.view.inputmethod.InputConnection
import android.view.inputmethod.EditorInfo
import android.view.*
import android.graphics.Color
import android.util.Log
import org.json.JSONObject
import java.util.Collections
import android.view.Gravity
import com.ownboard.app.db.*

// 1. إضافة واجهة الاستماع للحافظة
class OwnboardIME : InputMethodService(), ClipboardManager.OnPrimaryClipChangedListener {

    companion object {
        lateinit var ime: OwnboardIME
    }

    lateinit var rootView: FrameLayout
    lateinit var keyboardContainer: LinearLayout
    lateinit var popupContainer: LinearLayout 
    lateinit var clipboardView: com.ownboard.app.view.ClipboardView
    
    // متغير الاتصال بقاعدة البيانات
    lateinit var dbHelper: LayoutDatabase

    // مدير الحافظة للنظام
    private var clipboardManager: ClipboardManager? = null

    var currentLang = "ar"
    
    val backTexts = listOf("<>","</>","/**/","\"\"","''","()","{}","[]")
    
    // متغير المابر
    private lateinit var mapper: UsbGamepadMapper

    init {
        ime = this
    }

    override fun onCreate() {
        super.onCreate()
        // تهيئة قاعدة البيانات
        dbHelper = LayoutDatabase(this)
        
        // تهيئة المابر (كودك الأصلي)
        mapper = UsbGamepadMapper(currentInputConnection)

        // 2. تهيئة مراقب الحافظة
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager?.addPrimaryClipChangedListener(this)
    }

    // 3. تنظيف المراقب عند الإغلاق
    override fun onDestroy() {
        super.onDestroy()
        clipboardManager?.removePrimaryClipChangedListener(this)
    }

    // 4. الدالة التي تعمل عند نسخ أي نص في الهاتف
    override fun onPrimaryClipChanged() {
        if (clipboardManager?.hasPrimaryClip() == true) {
            val clipData = clipboardManager?.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val text = clipData.getItemAt(0).text?.toString() ?: ""
                
                // التأكد أن النص موجود وأن واجهة الحافظة جاهزة
                if (text.isNotEmpty() && ::clipboardView.isInitialized) {
                    clipboardView.addClip(text)
                }
            }
        }
    }

    override fun onCreateInputView(): View {
        rootView = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundColor(Color.parseColor("#222222"))
        }

        keyboardContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.gravity = Gravity.BOTTOM
            layoutParams = params
        }

        popupContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            visibility = View.GONE
            setBackgroundColor(Color.WHITE)
            elevation = 20f
        }

        rootView.addView(keyboardContainer)
        rootView.addView(popupContainer, FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        loadKeyboardFromDB("ar")

        clipboardView = com.ownboard.app.view.ClipboardView(this)
        
        // التعديل هنا: نجعل الارتفاع 0 مبدئياً، ونضبط الجاذبية للأسفل
        val clipParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 
            0 // الارتفاع صفر، سنقوم بتحديثه عند الفتح
        )
        clipParams.gravity = Gravity.BOTTOM // مهم جداً
        
        rootView.addView(clipboardView, clipParams)
        
        return rootView
    }
    fun toggleClipboard() {
        if (clipboardView.visibility == View.VISIBLE) {
            // إغلاق الحافظة
            clipboardView.visibility = View.GONE
            keyboardContainer.visibility = View.VISIBLE
        } else {
            // فتح الحافظة
            // 1. نأخذ ارتفاع الكيبورد الحالي
            val height = keyboardContainer.height
            
            // 2. نطبق الارتفاع على الحافظة
            if (height > 0) {
                val params = clipboardView.layoutParams
                params.height = height
                clipboardView.layoutParams = params
            }
            
            // 3. التبديل
            keyboardContainer.visibility = View.GONE // إخفاء الكيبورد
            clipboardView.visibility = View.VISIBLE
        }
    }
    private fun loadKeyboardFromDB(lang: String) {
        val jsonLayout = dbHelper.getLayoutByLang(lang)
        if (jsonLayout.isNotEmpty()) {
            buildKeyboard(jsonLayout)
        } else {
            Log.e("OwnboardIME", "Layout not found for lang: $lang")
        }
    }

    private fun buildKeyboard(jsonString: String) {
        try {
            keyboardContainer.removeAllViews()
            val jsonObject = JSONObject(jsonString)
            
            val keysIterator = jsonObject.keys()
            val keysList = mutableListOf<String>()
            while (keysIterator.hasNext()) {
                keysList.add(keysIterator.next())
            }
            Collections.sort(keysList)

            for (rowKey in keysList) {
                val rowObj = jsonObject.getJSONObject(rowKey)
                val rowHeight = rowObj.optDouble("height", 50.0).toFloat()
                val keysArray = rowObj.getJSONArray("keys")

                val rowLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutDirection = View.LAYOUT_DIRECTION_LTR 
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dpToPx(rowHeight)
                    )
                }

                for (i in 0 until keysArray.length()) {
                    val keyData = keysArray.getJSONObject(i)
                    
                    val keyView = All(this).apply {
                        text = keyData.optString("text", "")
                        hint = keyData.optString("hint", "")
                        val weightVal = keyData.optDouble("weight", 1.0).toFloat()
                        
                        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, weightVal)

                        click = keyData.optString("click", "")
                        longPress = keyData.optString("longPress", "")
                        leftScroll = keyData.optString("leftScroll", "")
                        rightScroll = keyData.optString("rightScroll", "")

                        textToSend = keyData.optString("textToSend", "")
                        textToSendLongPress = keyData.optString("textToSendLongPress", "")
                        textToSendLeftScroll = keyData.optString("textToSendLeftScroll", "")
                        textToSendRightScroll = keyData.optString("textToSendRightScroll", "")
                        
                        codeToSendClick = keyData.optInt("codeToSendClick", -1)
                        codeToSendLongPress = keyData.optInt("codeToSendLongPress", -1)
                        codeToSendLeftScroll = keyData.optInt("codeToSendLeftScroll", -1)
                        codeToSendRightScroll = keyData.optInt("codeToSendRightScroll", -1)
                    }

                    rowLayout.addView(keyView)
                }

                keyboardContainer.addView(rowLayout)
            }

        } catch (e: Exception) {
            Log.e("OwnboardIME", "Error building keyboard: ${e.message}")
            e.printStackTrace()
        }
    }

    fun switchLang() {
        if (currentLang == "ar") {
            currentLang = "en"
            loadKeyboardFromDB("en")
        } else {
            currentLang = "ar"
            loadKeyboardFromDB("ar")
        }
        
        Key.isSymbols.value = false
        Key.capslock.value = 0
    }

    fun switchSymbols(isSymbols: Boolean) {
        // منطق الرموز
    }

    fun sendKeyPress(text: String) {
        val ic = currentInputConnection ?: return
        var textToSend = if ((Key.capslock.value ?: 1) != 0) text.uppercase() else text
        
        ic.commitText(textToSend, 1)

        if (text in backTexts) {
             val backAmount = text.length / 2
             ic.commitText("", 1) 
             for(i in 1..backAmount) {
                 sendKeyPress(KeyEvent.KEYCODE_DPAD_LEFT)
             }
        }
    }

    fun sendKeyPress(keyCode: Int) {
        if (keyCode <= 0) return
        val ic = currentInputConnection ?: return
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
    }

    fun sendKeyDown(keyCode: Int) {
        if (keyCode <= 0) return
        val ic = currentInputConnection ?: return
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
    }

    fun sendKeyUp(keyCode: Int) {
        if (keyCode <= 0) return
        val ic = currentInputConnection ?: return
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
    }

    fun delete() {
        val ic = currentInputConnection ?: return
        val selectedText = ic.getSelectedText(InputConnection.GET_TEXT_WITH_STYLES)
        if (selectedText != null && selectedText.isNotEmpty()) {
            ic.commitText("", 1)
        } else {
            ic.deleteSurroundingText(1, 0)
        }
    }

    private fun dpToPx(dp: Float): Int {
        val density = resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }
    
    // === التعامل مع Gamepad Mapper ===
    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        // تحديث الاتصال للمابر
        if (::mapper.isInitialized) {
            mapper.setConnection(currentInputConnection)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // التحقق من المابر
        if (::mapper.isInitialized && event != null && mapper.processKey(event)) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        // التحقق من المابر
        if (::mapper.isInitialized && event != null && mapper.processKey(event)) {
            return true
        }
        return super.onKeyUp(keyCode, event)
    }
}