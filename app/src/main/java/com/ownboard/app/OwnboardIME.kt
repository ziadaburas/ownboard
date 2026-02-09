package com.ownboard.app

import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
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
import android.view.HapticFeedbackConstants

class OwnboardIME : InputMethodService(), ClipboardManager.OnPrimaryClipChangedListener {

    companion object {
        lateinit var ime: OwnboardIME
    }

    lateinit var rootView: FrameLayout
    lateinit var keyboardContainer: LinearLayout
    lateinit var popupContainer: LinearLayout 
    lateinit var clipboardView: com.ownboard.app.view.ClipboardView
    
    // قواعد البيانات
    lateinit var dbHelper: LayoutDatabase
    lateinit var appLangDb: AppLanguageDbHelper 

    private var clipboardManager: ClipboardManager? = null

    var currentLang = "ar"
    private var currentAppPackage: String = ""

    val backTexts = listOf("<>","</>","/**/","\"\"","''","()","{}","[]")
    
    private lateinit var mapper: UsbGamepadMapper

    // ==========================================
    // متغيرات التحكم بارتفاع الكيبورد
    // ==========================================
    var keyboardHeightPortraitDp = 340f  
    var keyboardHeightLandscapeDp = 300f 
    var bottomPaddingDp = 15f

    init {
        ime = this
    }

    override fun onCreate() {
        super.onCreate()
        dbHelper = LayoutDatabase(this)
        appLangDb = AppLanguageDbHelper(this) 
        
        mapper = UsbGamepadMapper(currentInputConnection)

        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager?.addPrimaryClipChangedListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        clipboardManager?.removePrimaryClipChangedListener(this)
    }

    override fun onPrimaryClipChanged() {
        if (clipboardManager?.hasPrimaryClip() == true) {
            val clipData = clipboardManager?.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val text = clipData.getItemAt(0).text?.toString() ?: ""
                if (text.isNotEmpty() && ::clipboardView.isInitialized) {
                    clipboardView.addClip(text)
                }
            }
        }
    }

    // دالة مساعدة لتحديد الارتفاع الحالي بناءً على الاتجاه
    private fun getCurrentKeyboardHeight(): Float {
        val configuration = resources.configuration
        return if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            keyboardHeightLandscapeDp
        } else {
            keyboardHeightPortraitDp
        }
    }

    override fun onCreateInputView(): View {
        rootView = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundColor(Color.parseColor("#222222"))
        }

        keyboardContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val height = dpToPx(getCurrentKeyboardHeight())
            val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
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

        clipboardView = com.ownboard.app.view.ClipboardView(this)
        
        val clipParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 
            0 
        )
        clipParams.gravity = Gravity.BOTTOM
        
        rootView.addView(clipboardView, clipParams)
        
        return rootView
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        
        if (::keyboardContainer.isInitialized) {
            val newHeight = dpToPx(getCurrentKeyboardHeight())
            
            val params = keyboardContainer.layoutParams
            params.height = newHeight
            keyboardContainer.layoutParams = params

            if (clipboardView.visibility == View.VISIBLE) {
                 val clipParams = clipboardView.layoutParams
                 clipParams.height = newHeight
                 clipboardView.layoutParams = clipParams
            }
            
            loadKeyboardFromDB(currentLang)
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)

        if (info != null && info.packageName != null) {
            currentAppPackage = info.packageName
            currentLang = appLangDb.getAppLanguage(currentAppPackage)
        } else {
            currentAppPackage = ""
        }

        loadKeyboardFromDB(currentLang)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        if (currentAppPackage.isNotEmpty()) {
            appLangDb.setAppLanguage(currentAppPackage, currentLang)
        }
    }

    fun toggleClipboard() {
        if (clipboardView.visibility == View.VISIBLE) {
            clipboardView.visibility = View.GONE
            keyboardContainer.visibility = View.VISIBLE
        } else {
            val currentHeight = dpToPx(getCurrentKeyboardHeight())
            val params = clipboardView.layoutParams
            params.height = currentHeight
            clipboardView.layoutParams = params
            
            keyboardContainer.visibility = View.GONE 
            clipboardView.visibility = View.VISIBLE
        }
    }

    private fun loadKeyboardFromDB(lang: String) {
        val jsonLayout = dbHelper.getLayoutByLang(lang)
        if (jsonLayout.isNotEmpty()) {
            buildKeyboard(jsonLayout)
        } else {
            Log.e("OwnboardIME", "Layout not found for lang: $lang")
            if(lang != "ar") loadKeyboardFromDB("ar")
        }
    }

    // ============================================================
    // الدالة المحدثة لبناء الكيبورد وفق الهيكلة الجديدة
    // ============================================================
    private fun buildKeyboard(jsonString: String) {
        try {
            keyboardContainer.removeAllViews()
            
            val totalHeightPx = dpToPx(getCurrentKeyboardHeight())
            val containerParams = keyboardContainer.layoutParams
            containerParams.height = totalHeightPx
            keyboardContainer.layoutParams = containerParams

            val jsonObject = JSONObject(jsonString)
            
            val keysIterator = jsonObject.keys()
            val keysList = mutableListOf<String>()
            while (keysIterator.hasNext()) {
                keysList.add(keysIterator.next())
            }
            Collections.sort(keysList)

            for (rowKey in keysList) {
                val rowObj = jsonObject.getJSONObject(rowKey)
                
                val rowWeight = rowObj.optDouble("height", 1.0).toFloat()
                val keysArray = rowObj.getJSONArray("keys")

                val rowLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutDirection = View.LAYOUT_DIRECTION_LTR 
                    
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0, 
                        rowWeight 
                    )
                }

                for (i in 0 until keysArray.length()) {
                    val keyData = keysArray.getJSONObject(i)
                    
                    val keyView = All(this).apply {
                        // 1. البيانات الأساسية
                        text = keyData.optString("text", "")
                        hint = keyData.optString("hint", "")
                        val weightVal = keyData.optDouble("weight", 1.0).toFloat()
                        
                        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, weightVal)

                        // 2. تعيين الوظائف (بما في ذلك السحب الجديد)
                        click = keyData.optString("click", "")
                        longPress = keyData.optString("longPress", "")
                        
                        // استبدال leftScroll/rightScroll بـ horizontalSwipe/verticalSwipe
                        horizontalSwipe = keyData.optString("horizontalSwipe", "")
                        verticalSwipe = keyData.optString("verticalSwipe", "")

                        // 3. بناء خريطة المعلمات (Params Map)
                        val paramsObj = keyData.optJSONObject("params")
                        val paramsMap = mutableMapOf<String, Any>()
                        if (paramsObj != null) {
                            val iter = paramsObj.keys()
                            while (iter.hasNext()) {
                                val key = iter.next()
                                paramsMap[key] = paramsObj.get(key)
                            }
                        }
                        // إسناد الخريطة إلى الزر (All.kt سيقوم بالباقي)
                        params = paramsMap
                    }

                    rowLayout.addView(keyView)
                }

                keyboardContainer.addView(rowLayout)
            }

            // الشريط السفلي (Navigation Bar)
            if (bottomPaddingDp > 0) {
                val navBar = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dpToPx(bottomPaddingDp)
                    )
                    gravity = Gravity.TOP
                    setBackgroundColor(Color.parseColor("#1A1A1A"))
                }

                // دالة مساعدة داخلية لإنشاء الأزرار
                fun createNavBarBtn(textStr: String, onClick: () -> Unit): TextView {
                    return TextView(this).apply {
                        text = textStr
                        textSize = bottomPaddingDp
                        setTextColor(Color.LTGRAY)
                        includeFontPadding = false 
                        setPadding(0, 0, 0, 0)
                        gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(
                            dpToPx(50f), 
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setOnClickListener { 
                            it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            onClick() 
                        }
                    }
                }

                // زر تغيير الكيبورد
                val switchImeBtn = createNavBarBtn("\u2328") {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                    imm.showInputMethodPicker()
                }

                // مسافة فارغة
                val centerSpace = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
                    setOnTouchListener { _, _ -> true }
                }

                // زر الإغلاق
                val hideKeyboardBtn = createNavBarBtn("\u25BC") {
                    requestHideSelf(0)
                }

                navBar.addView(switchImeBtn)
                navBar.addView(centerSpace)
                navBar.addView(hideKeyboardBtn)

                keyboardContainer.addView(navBar)
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
        
        if (currentAppPackage.isNotEmpty()) {
            appLangDb.setAppLanguage(currentAppPackage, currentLang)
        }
        
        Key.isSymbols.value = false
        Key.capslock.value = 0 
    }

    fun switchSymbols(isSymbols: Boolean) {
        // يمكنك تنفيذ منطق التبديل هنا إذا لزم الأمر
    }

    fun sendKeyPress(text: String) {
        val ic = currentInputConnection ?: return
        val textToSend = if ((Key.capslock.value ?: 1) != 0) text.uppercase() else text
        
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
    
    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        if (::mapper.isInitialized) {
            mapper.setConnection(currentInputConnection)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (::clipboardView.isInitialized && clipboardView.visibility == View.VISIBLE) {
                toggleClipboard()
                return true
            }
            if (isInputViewShown) {
                requestHideSelf(0)
                return true
            }
        }

        if (::mapper.isInitialized && event != null && mapper.processKey(event)) {
            return true
        }
        
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((::clipboardView.isInitialized && clipboardView.visibility == View.VISIBLE) || isInputViewShown) {
                return true
            }
        }

        if (::mapper.isInitialized && event != null && mapper.processKey(event)) {
            return true
        }
        
        return super.onKeyUp(keyCode, event)
    }
}