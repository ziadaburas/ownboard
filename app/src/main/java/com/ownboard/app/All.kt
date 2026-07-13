package com.ownboard.app

import android.content.Context
import android.util.AttributeSet
import android.graphics.Color
import android.graphics.Typeface
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.view.HapticFeedbackConstants
import kotlinx.coroutines.*
import android.widget.Toast

// تم إضافة هذا الكلاس لتحديد نوع الحدث وتسهيل المركزية
enum class TriggerType { CLICK, LONG_PRESS, H_SWIPE, V_SWIPE }

class All
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = android.R.attr.buttonStyle,
) : Key(context, attrs, defStyle) {

    companion object {
        
        val CLICK_FUNCTIONS = listOf(
            "", 
            "sendText",      
            "sendCode",      
            "sendSpecial",   
            "switchLang",    
            "switchSymbols", 
            "delete",        
            "openEmoji",     
            "openClipboard",
            "openSettings",
            "copy",
            "cut",
            "paste",
            "selectAll",
            "clear"
        )

        val LONG_PRESS_FUNCTIONS = listOf(
            "",
            "loop",          
            "showPopup",     
            "sendText",
            "sendCode",
            "holdSpecial",
            "switchLang",    
            "switchSymbols", 
            "delete",        
            "openEmoji",     
            "openClipboard",
            "openSettings",
            "copy",
            "cut",
            "paste",
            "selectAll",
            "clear"
        )

        val SWIPE_FUNCTIONS = listOf(
            "",
            "sendText",
            "sendCode",
            "switchLang",    
            "switchSymbols", 
            "delete",        
            "openEmoji",     
            "openClipboard",
            "openSettings",
            "holdSpecial",
            "copy",
            "cut",
            "paste",
            "selectAll",
            "clear"
        )
    }

    // ============================================================
    // هنا يتم استقبال البيانات وتحليل الـ popup
    // ============================================================
    var params: Map<String, Any> = emptyMap()
        set(value) {
            field = value
            updateSpecialKeyStatus()
            
            // التعديل الجوهري: استخراج القائمة المنبثقة من المعلمات
            val popupStr = value["popup"] as? String ?: ""
            if (popupStr.isNotEmpty()) {
                popupKeys = popupStr.split(" ")
            } else {
                popupKeys = emptyList()
            }
            // تحديث عرض القائمة المنبثقة
            if (btnWidth > 0) {
                popupWidth = (btnWidth * popupKeys.size).toFloat()
            }
        }

    private fun getParamString(key: String): String = params[key] as? String ?: ""
    private fun getParamInt(key: String): Int = (params[key] as? Number)?.toInt() ?: 0

    private fun updateSpecialKeyStatus() {
        val code = getParamInt("code")
        isSpecialKey = true
        when(code) {
            KeyEvent.KEYCODE_SHIFT_LEFT, 59 -> listener = Key.shift
            KeyEvent.KEYCODE_ALT_LEFT, 57 -> listener = Key.alt
            KeyEvent.KEYCODE_CTRL_LEFT, 113 -> listener = Key.ctrl
            115 -> listener = Key.capslock
            else -> isSpecialKey = false
        }
        if(code > 0) isConstKey = true 
    }

    // ============================================================
    // الدالة المركزية لتوزيع الوظائف (Centralized Action Handler)
    // ============================================================
    private fun bindAction(actionName: String, trigger: TriggerType): () -> Unit {
        
        // 1. إعدادات الواجهة التي تتم بمجرد تعيين الوظيفة (للنقر فقط لتجنب التكرار)
        if (trigger == TriggerType.CLICK) {
            when (actionName) {
                "switchSymbols" -> {
                    text = if (Key.isSymbols.value == true) "abc" else "123"
                    Key.isSymbols.addListener { newVal -> text = if (newVal == true) "abc" else "123" }
                }
                "openEmoji" -> backgroundImg = R.drawable.ic_emoji
                "openClipboard" -> backgroundImg = R.drawable.ic_clipboard
            }
        }

        // 2. إرجاع دالة التنفيذ التي سيتم استدعاؤها عند وقوع الحدث
        return {
            when (actionName) {
                "sendText" -> {
                    val key = when (trigger) {
                        TriggerType.CLICK -> "text"
                        TriggerType.LONG_PRESS -> if (params.containsKey("lpText")) "lpText" else "text"
                        TriggerType.H_SWIPE -> if (params.containsKey("hText")) "hText" else "text"
                        TriggerType.V_SWIPE -> if (params.containsKey("vText")) "vText" else "text"
                    }
                    val txt = getParamString(key)
                    if (txt.isNotEmpty()) OwnboardIME.ime.sendKeyPress(txt)
                }
                "sendCode" -> {
                    val key = when (trigger) {
                        TriggerType.CLICK -> "code"
                        TriggerType.LONG_PRESS -> if (params.containsKey("lpCode")) "lpCode" else "code"
                        TriggerType.H_SWIPE -> if (params.containsKey("hCode")) "hCode" else "code"
                        TriggerType.V_SWIPE -> if (params.containsKey("vCode")) "vCode" else "code"
                    }
                    val code = getParamInt(key)
                    if (code != 0) OwnboardIME.ime.sendKeyPress(code)
                }
                "sendSpecial" -> {
                    if (trigger == TriggerType.CLICK) {
                        if (listener.value != 0) disable() else enable()
                    }
                }
                "holdSpecial" -> enable(1)
                "switchLang" -> performLangSwitch()
                "switchSymbols" -> {
                    Key.isSymbols.value = !(Key.isSymbols.value)
                    OwnboardIME.ime.switchSymbols(Key.isSymbols.value == true)
                }
                "delete" -> OwnboardIME.ime.delete()
                "openEmoji" -> OwnboardIME.ime.toggleEmoji()
                "openClipboard" -> OwnboardIME.ime.toggleClipboard()
                "openSettings" -> OwnboardIME.ime.openSettings()
                "copy" -> OwnboardIME.ime.performContextMenuAction(android.R.id.copy)
                "cut" -> OwnboardIME.ime.performContextMenuAction(android.R.id.cut)
                "paste" -> OwnboardIME.ime.performContextMenuAction(android.R.id.paste)
                "selectAll" -> OwnboardIME.ime.performContextMenuAction(android.R.id.selectAll)
                "clear" -> { 
                    OwnboardIME.ime.performContextMenuAction(android.R.id.selectAll)
                    OwnboardIME.ime.sendKeyPress("")

                }

                // الوظائف الخاصة بالضغط المطول فقط
                "showPopup" -> {
                    if (trigger == TriggerType.LONG_PRESS && popupKeys.isNotEmpty()) {
                        isPopupVisible = true
                        showAltChars()
                        selectedIndex = 0
                        highlightButton(selectedIndex)
                    }
                }
                "loop" -> {
                    if (trigger == TriggerType.LONG_PRESS && isHoldKey) {
                        onClick()
                        longPressHandler!!.postDelayed(longPressRunnable!!, 50)
                    }
                }
            }
        }
    }

    // ============================================================
    // تطبيق الوظائف التلقائي باستخدام المركزية
    // ============================================================

    var click = ""
        set(value) {
            field = value
            // حالة استثنائية كما كانت في الكود القديم
            if (value == "sendSpecial") onLongPressFn = { enable(1) }
            onClickFn = bindAction(value, TriggerType.CLICK)
        }

    var longPress = ""
        set(value) {
            field = value
            if (click == "sendSpecial") return
            onLongPressFn = bindAction(value, TriggerType.LONG_PRESS)
        }

    var horizontalSwipe = ""
        set(value) {
            field = value
            val action = bindAction(value, TriggerType.H_SWIPE)
            // استخدام دالة مغلفة لتتوافق مع البارامتر Float
            onHorizontalSwipeFn = { _ -> action() }
        }

    var verticalSwipe = ""
        set(value) {
            field = value
            val action = bindAction(value, TriggerType.V_SWIPE)
            // استخدام دالة مغلفة لتتوافق مع البارامتر Float
            onVerticalSwipeFn = { _ -> action() }
        }

    // ============================================================
    // بقية الكود الأساسي بدون أي تغيير
    // ============================================================

    private fun performLangSwitch() {
        OwnboardIME.ime.switchLang()
        Key.ctrl.notifyListeners()
        Key.alt.notifyListeners()
        Key.shift.notifyListeners()
        Key.capslock.notifyListeners()
    }

    val popupBtns = mutableListOf<Button>()
    var isPopupVisible = false
    var selectedIndex = 0
    var autoHidePopup = true
    lateinit var popupContainer: LinearLayout
    var listener= ValueListener(0)
    set(value) {
        field = value
        listener.addListener {
                 if (it == 0) {
                    setBackgroundColor(0xFF2D2D2D.toInt())
                     disable()
                }else {
                    setBackgroundColor(0xFF701921.toInt())
                }
            }
        if (listener.value == 0) {
                    setBackgroundColor(0xFF2D2D2D.toInt())
                    // disable()
                }else {
                    setBackgroundColor(0xFF701921.toInt())
                }
        }
    var onClickFn: () -> Unit = {}
    var onLongPressFn: () -> Unit = {}
    
    // تم إزالة تعريف onHorizontalSwipeFn و onVerticalSwipeFn من هنا 
    // لأنهما معرّفان بالفعل في كلاس Key الأب
    
    override fun onLongPress() {
        if(!isLongPressed) {
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
        super.onLongPress()
        onLongPressFn()
    }
    
    override fun onClick() {
        onClickFn()
        if(!isSpecialKey)
        super.onClick()
    }
    
    fun disable() {
        if(!isSpecialKey) return 
        val code = getParamInt("code")
        OwnboardIME.ime.sendKeyUp(code)
        //OwnboardIME.ime.sendKeyPress("# ")
        listener.value = 0
        setBackgroundColor(0xFF2D2D2D.toInt())
    }
    
    fun enable(hold: Int = 0) {
        val code = getParamInt("code")
        listener.value = hold + 1
        OwnboardIME.ime.sendKeyDown(code)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        popupContainer = OwnboardIME.ime.popupContainer
    }

    override fun actionUp(e: MotionEvent): Boolean {
        super.actionUp(e)
        if (isPopupVisible && autoHidePopup) {
            if (popupKeys.isNotEmpty() && selectedIndex in popupKeys.indices) {
                popupBtns[selectedIndex].performClick() 
                onKeyPress()
            }
            popupContainer.visibility = View.GONE
            isPopupVisible = false
            popupBtns.clear()
            selectedIndex = 0
            isLongPressed = false
            return true
        }
        return false
    }
    
    override fun actionMove(e: MotionEvent): Boolean {
        super.actionMove(e)
        if (isPopupVisible && autoHidePopup) {
            val x = e.rawX.toInt()
            val y = popupBtns.getOrNull(0)?.let {
                val loc = IntArray(2)
                it.getLocationOnScreen(loc)
                loc[1] + it.height / 2
            } ?: 0
            val btnUnder = findButtonUnderRaw(x, y)
            val newIndex = popupBtns.indexOf(btnUnder)
            if (newIndex != selectedIndex) {
                selectedIndex = newIndex
                highlightButton(selectedIndex)
            }
        }
        return true
    }
    


    init {
        val paramsLayout = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
        layoutParams = paramsLayout
        Key.capslock.addListener {
            if(!isConstKey){
                if (it != 0) {
                    text = text.uppercase()
                 } else {
                    text = text.lowercase()
                 }
            }
        }
    }

    private fun showAltChars() {
        popupContainer.removeAllViews()
        popupContainer.visibility = View.INVISIBLE 
        popupContainer.setBackgroundColor(Color.WHITE)

        val btnLoc = IntArray(2)
        getLocationOnScreen(btnLoc)
        val estimatedWidth = (btnWidth * popupKeys.size)
        val isReversed = (btnLoc[0] + estimatedWidth) > screenWidth
        val listToShow = if (isReversed) popupKeys.reversed() else popupKeys

        listToShow.forEach { altChar ->
            val altBtn = Button(context).apply {
                text = altChar
                setTextColor(Color.BLACK)
                setBackgroundColor(Color.LTGRAY)
                textSize = 18f
                setPadding(10, 0, 10, 0)
                minWidth = 0 
                minimumWidth = 0
                typeface = Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(
                    btnWidth, 
                    dpToPx(45f).toInt()
                ).apply {
                    setMargins(2, 2, 2, 2)
                }
                
                setOnClickListener {
                    OwnboardIME.ime.sendKeyPress(altChar)
                    popupContainer.visibility = View.GONE
                    isPopupVisible = false
                    popupBtns.clear()
                    selectedIndex = 0
                    isLongPressed = false
                    this@All.setBackgroundColor(0xFF2D2D2D.toInt())
                }
            }
            popupBtns.add(altBtn)
            popupContainer.addView(altBtn)
        }

        popupContainer.post {
            if (!isPopupVisible) return@post

            val rootViewLoc = IntArray(2)
            getLocationOnScreen(btnLoc)
            OwnboardIME.ime.rootView.getLocationOnScreen(rootViewLoc)

            val actualWidth = popupContainer.width.toFloat()
            val actualHeight = popupContainer.height.toFloat()

            var x: Float
            if (isReversed) {
                x = (btnLoc[0] - rootViewLoc[0] + btnWidth - actualWidth)
            } else {
                x = (btnLoc[0] - rootViewLoc[0]).toFloat()
            }

            if (x < 0) x = 0f
            val y = (btnLoc[1] - rootViewLoc[1] - actualHeight - dpToPx(2f))

            popupContainer.x = x
            popupContainer.y = y
            
            popupContainer.visibility = View.VISIBLE
            popupContainer.bringToFront()
        }
    }

    private fun highlightButton(index: Int) {
        popupBtns.forEachIndexed { i, btn ->
            btn.setBackgroundColor(if (i == index) 0xFF701921.toInt() else Color.LTGRAY)
        }
    }
    
    private fun findButtonUnderRaw(rawX: Int, rawY: Int): Button? {
        val loc = IntArray(2)
        popupBtns.forEach { btn ->
            btn.getLocationOnScreen(loc)
            val left = loc[0]
            val top = loc[1]
            val right = left + btn.width
            val bottom = top + btn.height
            if (rawX in left..right && rawY in top..bottom) {
                return btn
            }
        }
        return null
    }
}