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
import org.json.JSONArray // تأكد من وجود هذا
import java.util.Collections
import android.view.Gravity
import com.ownboard.app.db.*
import com.ownboard.app.utils.*
import android.view.HapticFeedbackConstants
import android.text.InputType 

class OwnboardIME : InputMethodService(), ClipboardManager.OnPrimaryClipChangedListener {

    companion object {
        lateinit var ime: OwnboardIME
    }

    lateinit var rootView: FrameLayout
    lateinit var keyboardContainer: LinearLayout
    lateinit var popupContainer: LinearLayout 
    lateinit var clipboardView: com.ownboard.app.view.ClipboardView
    lateinit var emojiBoard: com.ownboard.app.view.EmojiView
    
    lateinit var dbHelper: LayoutDatabase
    lateinit var appLangDb: AppLanguageDbHelper 

    private var clipboardManager: ClipboardManager? = null

    // تتبع اللغة الحالية (ar/en)
    var currentLang = "ar"
    // تتبع حالة الرموز
    var isSymbolsMode = false
    
    private var currentAppPackage: String = ""
    private var lastInternalPasteTime: Long = 0 

    private lateinit var mapper: UsbGamepadMapper

    // --- تم حذف المتغيرات الثابتة القديمة واستبدالها باستدعاءات SettingsManager في الأسفل ---

    init {
        ime = this
    }

    override fun onCreate() {
        super.onCreate()
        // تهيئة مدير الإعدادات أولاً
        SettingsManager.init(applicationContext)
        
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
        if (System.currentTimeMillis() - lastInternalPasteTime < 500) return
        

        if (clipboardManager?.hasPrimaryClip() == true) {
            val clipData = clipboardManager?.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val text = clipData.getItemAt(0).text?.toString() ?: ""
                // تأكدنا أنه ليس نسخ داخلي، الآن نحفظه
                if (text.isNotEmpty() && ::clipboardView.isInitialized) {
                    clipboardView.addClip(text)
                }
            }
        }
    }

    // دالة اللصق الذكية (تم تحديثها في خطوات سابقة)
    fun pasteFromHistory(text: String) {
        val ic = currentInputConnection ?: return
        lastInternalPasteTime = System.currentTimeMillis()
        // 1. إعداد طلب استخراج النص للتحقق من حالة الحقل
        val req = android.view.inputmethod.ExtractedTextRequest().apply {
            token = 1
            flags = 0
            hintMaxLines = 2
            hintMaxChars = 500
        }

        // 2. محاولة الحصول على النص
        val extractedText = ic.getExtractedText(req, 0)

        // 3. اتخاذ القرار
        if (extractedText == null) {
            // الحالة أ: التطبيق "غبي" أو خام (Raw Input) -> نرسل النص مباشرة
            ic.commitText(text, 1)
        } else {
            // الحالة ب: التطبيق قياسي ويدعم التحرير -> نستخدم اللصق النظامي
            try {
               

                // وضع النص في حافظة النظام
                val clip = android.content.ClipData.newPlainText("copied_text", text)
                clipboardManager?.setPrimaryClip(clip)

                // إرسال أمر اللصق
                val sent = ic.performContextMenuAction(android.R.id.paste)

                // خطة طوارئ
                if (!sent) {
                    ic.commitText(text, 1)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ic.commitText(text, 1)
            }
        }
    }

    // دالة جلب الارتفاع من الإعدادات العامة
    private fun getCurrentKeyboardHeight(): Float {
        val configuration = resources.configuration
        return if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // جلب القيمة من الإعدادات (الوضع العرضي)، الافتراضي 300
            SettingsManager.getInt("keyboardHeightLandscape", 300).toFloat()
        } else {
            // جلب القيمة من الإعدادات (الوضع الطولي)، الافتراضي 340
            SettingsManager.getInt("keyboardHeightPortrait", 340).toFloat()
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
        
        emojiBoard = com.ownboard.app.view.EmojiView(this)
        val emojiParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dpToPx(getCurrentKeyboardHeight())
        )
        emojiParams.gravity = Gravity.BOTTOM
        emojiBoard.visibility = View.GONE
        
        rootView.addView(emojiBoard, emojiParams)
         
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
            
            if (isSymbolsMode) {
                loadKeyboardFromDB("symbols")
            } else {
                loadKeyboardFromDB(currentLang)
            }
        }
        if (::emojiBoard.isInitialized) {
            val params = emojiBoard.layoutParams
            params.height = dpToPx(getCurrentKeyboardHeight())
            emojiBoard.layoutParams = params
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        if (emojiBoard != null) {
            emojiBoard!!.visibility = View.GONE
        }
        if (::keyboardContainer.isInitialized) {
            keyboardContainer.visibility = View.VISIBLE
        }
        if (info != null && info.packageName != null) {
            currentAppPackage = info.packageName
            currentLang = appLangDb.getAppLanguage(currentAppPackage)
        } else {
            currentAppPackage = ""
        }
        
        isSymbolsMode = false
        loadKeyboardFromDB(currentLang)
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        
        if (emojiBoard != null) {
            emojiBoard!!.visibility = View.GONE
        }
        if (::keyboardContainer.isInitialized) {
            keyboardContainer.visibility = View.VISIBLE
        }

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
    
    fun toggleEmoji() {
        if (emojiBoard == null) return

        if (emojiBoard!!.visibility == View.VISIBLE) {
            emojiBoard!!.visibility = View.GONE
            keyboardContainer.visibility = View.VISIBLE
        } else {
            keyboardContainer.visibility = View.GONE
            clipboardView.visibility = View.GONE
            emojiBoard!!.resetToFirstTab()
            emojiBoard!!.visibility = View.VISIBLE
        }
    }

    private fun loadKeyboardFromDB(lang: String) {
        val jsonLayout = dbHelper.getLayoutByLang(lang)
        if (jsonLayout.isNotEmpty()) {
            buildKeyboard(jsonLayout)
        } else {
            Log.e("OwnboardIME", "Layout not found for lang: $lang")
            if(lang == "symbols") {
                 isSymbolsMode = false
                 loadKeyboardFromDB("ar")
            } else if(lang != "ar") {
                 loadKeyboardFromDB("ar")
            }
        }
    }
    
    private fun buildKeyboard(jsonString: String) {
        try {
            keyboardContainer.removeAllViews()
            
            val totalHeightPx = dpToPx(getCurrentKeyboardHeight())
            val containerParams = keyboardContainer.layoutParams
            containerParams.height = totalHeightPx
            keyboardContainer.layoutParams = containerParams

            // جلب الهامش السفلي من الإعدادات (الافتراضي 15)
            val bottomPaddingDp = SettingsManager.getInt("bottomPadding", 15).toFloat()

            val rowsArray = JSONArray(jsonString)

            for (i in 0 until rowsArray.length()) {
                val rowObj = rowsArray.getJSONObject(i)
                
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

                for (j in 0 until keysArray.length()) {
                    val keyData = keysArray.getJSONObject(j)
                    
                    val keyView = All(this).apply {
                        text = keyData.optString("text", "")
                        hint = keyData.optString("hint", "")
                        val weightVal = keyData.optDouble("weight", 1.0).toFloat()
                        
                        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, weightVal)

                        click = keyData.optString("click", "")
                        longPress = keyData.optString("longPress", "")
                        
                        horizontalSwipe = keyData.optString("horizontalSwipe", "")
                        verticalSwipe = keyData.optString("verticalSwipe", "")

                        val paramsObj = keyData.optJSONObject("params")
                        val paramsMap = mutableMapOf<String, Any>()
                        if (paramsObj != null) {
                            val iter = paramsObj.keys()
                            while (iter.hasNext()) {
                                val key = iter.next()
                                paramsMap[key] = paramsObj.get(key)
                            }
                        }
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
                        dpToPx(bottomPaddingDp) // استخدام القيمة من الإعدادات
                    )
                    gravity = Gravity.TOP
                    setBackgroundColor(Color.parseColor("#1A1A1A"))
                }
                
                 fun createNavBarBtn(textStr: String, onClick: () -> Unit): TextView {
                    return TextView(this).apply {
                        text = textStr
                        textSize = bottomPaddingDp // حجم الخط نسبي للارتفاع
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

                val switchImeBtn = createNavBarBtn("\u2328") {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                    imm.showInputMethodPicker()
                }

                val centerSpace = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
                    setOnTouchListener { _, _ -> true }
                }

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
        } else {
            currentLang = "ar"
        }
        
        if (currentAppPackage.isNotEmpty()) {
            appLangDb.setAppLanguage(currentAppPackage, currentLang)
        }
        
        isSymbolsMode = false
        loadKeyboardFromDB(currentLang)

        Key.isSymbols.value = false
        Key.capslock.value = 0 
    }

    fun switchSymbols(ignored: Boolean) {
        if (!isSymbolsMode) {
            isSymbolsMode = true
            loadKeyboardFromDB("symbols")
        } else {
            isSymbolsMode = false
            loadKeyboardFromDB(currentLang)
        }
    }

    // دالة إرسال النص (مع منطق إرجاع المؤشر من الإعدادات)
    fun sendKeyPress(text: String) {
        val ic = currentInputConnection ?: return
        val textToSend = if ((Key.capslock.value ?: 1) != 0) text.uppercase() else text
        
        ic.commitText(textToSend, 1)

        // 1. جلب قائمة النصوص التي تتطلب رجوع المؤشر (من الإعدادات)
        val rawBackTexts = SettingsManager.getString("backTexts")
        val backList = if (rawBackTexts.isNotEmpty()) rawBackTexts.split(" ") else emptyList()

        // 2. التحقق هل النص المرسل (text الأصلية) موجود في القائمة؟
        if (backList.contains(text)) {
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