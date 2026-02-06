package com.ownboard.app

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

class OwnboardIME : InputMethodService() {

    companion object {
        lateinit var ime: OwnboardIME
    }

    lateinit var rootView: FrameLayout
    lateinit var keyboardContainer: LinearLayout
    // التعديل الأساسي: تعريف المتغير هنا ليصبح عاماً ويمكن الوصول إليه من All.kt
    lateinit var popupContainer: LinearLayout 
    
    var currentLang = "ar"
    
    val backTexts = listOf("<>","</>","/**/","\"\"","''","()","{}","[]")

    // بيانات الكيبورد العربي (JSON)
    private val arabicLayoutJson = """
    {
      "row1": {
        "height": 45.0,
        "keys": [
          { "weight": 1.0, "text": "←", "click": "sendCode", "longPress": "loop", "codeToSendClick": 21 },
          { "weight": 1.0, "text": "↑", "hint": "Home", "click": "sendCode", "longPress": "sendCode", "codeToSendClick": 19, "codeToSendLongPress": 122 },
          { "weight": 1.0, "text": "⇥", "click": "sendCode", "codeToSendClick": 61 },
          { "weight": 1.0, "text": "Ctrl", "click": "sendSpecial", "codeToSendClick": 113 }, 
          { "weight": 1.0, "text": "Alt", "click": "sendSpecial", "codeToSendClick": 57 },
          { "weight": 1.0, "text": "Shift", "click": "sendSpecial", "codeToSendClick": 59 },
          { "weight": 1.0, "text": "↓", "hint": "End", "click": "sendCode", "longPress": "sendCode", "codeToSendClick": 20, "codeToSendLongPress": 123 },
          { "weight": 1.0, "text": "→", "click": "sendCode", "longPress": "loop", "codeToSendClick": 22 }
        ]
      },
      "row2": {
        "height": 55.0,
        "keys": [
          { "weight": 1.0, "text": "1", "hint": "j k", "click": "sendText", "longPress": "showPopup", "textToSend": "1" },
          { "weight": 1.0, "text": "2", "hint": "\"", "click": "sendText", "longPress": "showPopup", "textToSend": "2" },
          { "weight": 1.0, "text": "3", "hint": "·", "click": "sendText", "longPress": "showPopup", "textToSend": "3" },
          { "weight": 1.0, "text": "4", "hint": ":", "click": "sendText", "longPress": "showPopup", "textToSend": "4" },
          { "weight": 1.0, "text": "5", "hint": "؟", "click": "sendText", "longPress": "showPopup", "textToSend": "5" },
          { "weight": 1.0, "text": "6", "hint": "؛ j k", "click": "sendText", "longPress": "showPopup", "textToSend": "6", "leftScroll": "switchLang", "rightScroll": "switchLang" },
          { "weight": 1.0, "text": "7", "hint": "-", "click": "sendText", "longPress": "showPopup", "textToSend": "7", "leftScroll": "sendText", "textToSendLeftScroll": "cc" },
          { "weight": 1.0, "text": "8", "hint": "_", "click": "sendText", "longPress": "showPopup", "textToSend": "8" },
          { "weight": 1.0, "text": "9", "hint": "(", "click": "sendText", "longPress": "showPopup", "textToSend": "9" },
          { "weight": 1.0, "text": "0", "hint": ")", "click": "sendText", "longPress": "showPopup", "textToSend": "0" }
        ]
      },
      "row3": {
        "height": 55.0,
        "keys": [
          { "weight": 1.0, "text": "ض", "hint": "!", "click": "sendText", "longPress": "showPopup", "textToSend": "ض" },
          { "weight": 1.0, "text": "ص", "hint": "!", "click": "sendText", "longPress": "loop", "textToSend": "ص" },
          { "weight": 1.0, "text": "ق", "hint": "!", "click": "sendText", "longPress": "showPopup", "textToSend": "ق" },
          { "weight": 1.0, "text": "ف", "hint": "!", "click": "sendText", "longPress": "showPopup", "textToSend": "ف" },
          { "weight": 1.0, "text": "غ", "hint": "!", "click": "sendText", "longPress": "showPopup", "textToSend": "غ" },
          { "weight": 1.0, "text": "ع", "hint": "!", "click": "sendText", "longPress": "showPopup", "textToSend": "ع" },
          { "weight": 1.0, "text": "ه", "hint": "!", "click": "sendText", "longPress": "showPopup", "textToSend": "ه" },
          { "weight": 1.0, "text": "خ", "hint": "!", "click": "sendText", "longPress": "showPopup", "textToSend": "خ" },
          { "weight": 1.0, "text": "ح", "hint": "!", "click": "sendText", "longPress": "showPopup", "textToSend": "ح" },
          { "weight": 1.0, "text": "ج", "hint": "!", "click": "sendText", "longPress": "showPopup", "textToSend": "ج" }
        ]
      },
      "row4": {
        "height": 55.0,
        "keys": [
          { "weight": 1.0, "text": "ش", "hint": "!", "click": "sendText", "longPress": "showPopup", "textToSend": "ش" },
          { "weight": 1.0, "text": "س", "hint": "!", "click": "sendText", "longPress": "showPopup", "textToSend": "س" },
          { "weight": 1.0, "text": "ي", "hint": "ى ئ", "click": "sendText", "longPress": "showPopup", "textToSend": "ي" },
          { "weight": 1.0, "text": "ب", "hint": "!", "click": "sendText", "longPress": "showPopup", "textToSend": "ب" },
          { "weight": 1.0, "text": "ل", "hint": "!", "click": "sendText", "longPress": "showPopup", "textToSend": "ل" },
          { "weight": 1.0, "text": "ا", "hint": "ء أ إ آ", "click": "sendText", "longPress": "showPopup", "textToSend": "ا" },
          { "weight": 1.0, "text": "ت", "hint": "ـ", "click": "sendText", "longPress": "showPopup", "textToSend": "ت" },
          { "weight": 1.0, "text": "ن", "hint": "!", "click": "sendText", "longPress": "showPopup", "textToSend": "ن" },
          { "weight": 1.0, "text": "م", "hint": "!", "click": "sendText", "longPress": "showPopup", "textToSend": "م" },
          { "weight": 1.0, "text": "ك", "hint": "؛", "click": "sendText", "longPress": "showPopup", "textToSend": "ك" }
        ]
      },
      "row5": {
        "height": 55.0,
        "keys": [
          { "weight": 1.0, "text": "ظ", "hint": "َ ِ ُ ً ٍ ٌ ّ ْ", "click": "sendText", "longPress": "showPopup", "textToSend": "ظ" },
          { "weight": 1.0, "text": "ط", "hint": "!", "click": "sendText", "longPress": "showPopup", "textToSend": "ط" },
          { "weight": 1.0, "text": "ذ", "hint": "!", "click": "sendText", "longPress": "showPopup", "textToSend": "ذ" },
          { "weight": 1.0, "text": "د", "hint": "!", "click": "sendText", "longPress": "showPopup", "textToSend": "د" },
          { "weight": 1.0, "text": "ز", "hint": "!", "click": "sendText", "longPress": "showPopup", "textToSend": "ز" },
          { "weight": 1.0, "text": "ر", "hint": "!", "click": "sendText", "longPress": "showPopup", "textToSend": "ر" },
          { "weight": 1.0, "text": "و", "hint": "ؤ", "click": "sendText", "longPress": "showPopup", "textToSend": "و" },
          { "weight": 1.0, "text": "ة", "hint": "!", "click": "sendText", "longPress": "showPopup", "textToSend": "ة" },
          { "weight": 1.0, "text": "ث", "hint": "!", "click": "sendText", "longPress": "showPopup", "textToSend": "ث" },
          { "weight": 1.5, "text": "⌫", "click": "delete", "longPress": "loop" }
        ]
      },
      "row6": {
        "height": 60.0,
        "keys": [
          { "weight": 1.5, "text": "123", "click": "switchSymbols" },
          { "weight": 1.0, "text": "😁", "click": "openEmoji" },
          { "weight": 1.0, "text": "،", "click": "sendText", "textToSend": "،" },
          { "weight": 3.0, "text": "العربية", "hint": "English", "click": "sendText", "textToSend": " ", "leftScroll": "switchLang", "rightScroll": "switchLang" },
          { "weight": 1.0, "text": ".", "click": "sendText", "textToSend": "." },
          { "weight": 1.0, "text": "📋", "click": "openClipboard" },
          { "weight": 1.5, "text": "⏎", "click": "sendCode", "codeToSendClick": 66 }
        ]
      }
    }
    """

    init {
        ime = this
    }

    override fun onCreateInputView(): View {
        // 1. التغيير الأساسي: استخدام FrameLayout بدلاً من LinearLayout
        // هذا يسمح للـ Popup أن يطفو فوق الكيبورد
        rootView = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundColor(Color.parseColor("#1a1a1a"))
        }

        // 2. إعداد حاوية الأزرار لتكون في أسفل الشاشة
        keyboardContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            // نستخدم FrameLayout.LayoutParams لتحديد المكان في الأسفل
            val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.gravity = Gravity.BOTTOM // <--- مهم جداً: تثبيت الكيبورد في الأسفل
            layoutParams = params
        }

        // 3. إعداد الـ Popup (القائمة المنبثقة)
        popupContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            visibility = View.GONE
            setBackgroundColor(Color.WHITE)
            elevation = 20f // رفعنا القيمة لضمان ظهورها فوق كل شيء
            
            // إضافة حدود ناعمة (اختياري)
            // background = getDrawable(R.drawable.popup_bg) // اذا عندك shape
        }

        // إضافة الحاويات: الترتيب لا يهم كثيراً في FrameLayout لكن يفضل الـ Popup أخيراً
        rootView.addView(keyboardContainer)
        
        // نضيف الـ Popup ونعطيه خصائص FrameLayout لكي يتحرك بحرية
        rootView.addView(popupContainer, FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        buildKeyboard(arabicLayoutJson)

        return rootView
    }

    override fun onStartInputView(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(attribute, restarting)
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
                        
                        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, weightVal).apply {
                            setMargins(1, 1, 1, 1)
                        }

                        // تعيين الخواص
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
            // buildKeyboard(englishLayoutJson) // ضع متغير الانجليزية هنا لاحقاً
        } else {
            currentLang = "ar"
            buildKeyboard(arabicLayoutJson)
        }
        Key.isSymbols.value = false
    }

    fun switchSymbols(isSymbols: Boolean) {
        // منطق الرموز
    }

    fun sendKeyPress(text: String) {
        val ic = currentInputConnection// ?: return
        var textToSend =text // if ((Key.capslock.value ?: 1) != 0) text.uppercase() else text
        
        ic.commitText(textToSend, 1)

       /* if (text in backTexts) {
             val backAmount = text.length / 2
             ic.commitText("", 1) 
             for(i in 1..backAmount) {
                 sendKeyPress(KeyEvent.KEYCODE_DPAD_LEFT)
             }
        }*/
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
}