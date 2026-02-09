package com.ownboard.app

import android.content.Context
import android.util.AttributeSet
import android.graphics.Color
import android.graphics.Typeface
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.view.HapticFeedbackConstants

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
            "openClipboard"  
        )

        val LONG_PRESS_FUNCTIONS = listOf(
            "",
            "loop",          
            "showPopup",     
            "sendText",
            "sendCode",
            "switchLang",
             "holdSpecial"
        )

        val SWIPE_FUNCTIONS = listOf(
            "",
            "sendText",
            "sendCode",
            "switchLang",
            "delete",
            "holdSpecial"  
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

    open var click = ""
    set(value) {
        field = value
        when(value){
            "sendText"->{
                onClickFn= {
                    val txt = getParamString("text")
                    if(txt.isNotEmpty()) OwnboardIME.ime.sendKeyPress(txt) 
                }
            }
            "sendCode"->{
                 onClickFn = {
                    val code = getParamInt("code")
                    if(code != 0) OwnboardIME.ime.sendKeyPress(code) 
                }
            }
            "switchSymbols"->{
                onClickFn = {
                    Key.isSymbols.value = !(Key.isSymbols.value ?: true)
                    OwnboardIME.ime.switchSymbols(Key.isSymbols.value == true)
                }
                text = if (Key.isSymbols.value == true) "abc" else "123"
                Key.isSymbols.addListener { newVal -> text = if (newVal == true) "abc" else "123" }
            }
            "sendSpecial"->{
                onClickFn = {
                    if ((listener.value ?: 1) != 0) disable() else enable()
                }
                onLongPressFn = { enable(1) }
            }
            "switchLang"->{
                onClickFn = {
                    performLangSwitch()
                }
            }
             "delete"->{
                onClickFn = {
                    OwnboardIME.ime.delete() 
                }
            }
            "openEmoji" -> {
                onClickFn = {
                    OwnboardIME.ime.toggleEmoji()
                }
                backgroundImg = R.drawable.ic_emoji
            }
            "openClipboard" -> {
                onClickFn = {
                    OwnboardIME.ime.toggleClipboard()
                }
                backgroundImg = R.drawable.ic_clipboard
            }
            else -> { onClickFn={} }
        }
    }

    open var longPress = ""
    set(value) {
        field = value
        if(click == "sendSpecial") return
        when(value){
            "sendText"->{
                onLongPressFn= {
                    val txt = if(params.containsKey("lpText")) getParamString("lpText") else getParamString("text")
                    OwnboardIME.ime.sendKeyPress(txt) 
                }
            }
            "sendCode"->{
                 onLongPressFn= {
                    val code = if(params.containsKey("lpCode")) getParamInt("lpCode") else getParamInt("code")
                    OwnboardIME.ime.sendKeyPress(code) 
                }
            }
            "showPopup"->{
                onLongPressFn= {
                    if (popupKeys.isNotEmpty()){
                        isPopupVisible = true
                        showAltChars()
                        selectedIndex = 0
                        highlightButton(selectedIndex)
                    }
                }
            }
            "loop"->{
                onLongPressFn = {
                    if (isHoldKey) {
                        onClick()
                        longPressHandler.postDelayed(longPressRunnable, 50)
                    }
                }
            }
            "switchLang"->{
                onLongPressFn = { performLangSwitch() }
            }
            "holdSpecial"->{
                onLongPressFn = { enable(1) }
            }
            else-> onLongPressFn={}
        }
    }

    open var horizontalSwipe = ""
    set(value) {
        field = value
        when(value){
            "sendText"->{
                onHorizontalSwipeFn = { dir ->
                    val txt = if(params.containsKey("hText")) getParamString("hText") else getParamString("text")
                    if(txt.isNotEmpty()) OwnboardIME.ime.sendKeyPress(txt) 
                }
            }
            "sendCode"->{
                 onHorizontalSwipeFn = { dir ->
                    val code = if(params.containsKey("hCode")) getParamInt("hCode") else getParamInt("code")
                    OwnboardIME.ime.sendKeyPress(code) 
                }
            }
            "switchLang"->{
                onHorizontalSwipeFn = { performLangSwitch() }
            }
            "delete" -> {
                onHorizontalSwipeFn = { OwnboardIME.ime.delete() }
            }
            "holdSpecial"->{
                 onHorizontalSwipeFn = { enable(1) }
            }
            else-> onHorizontalSwipeFn={}
        }
    }

    open var verticalSwipe = ""
    set(value) {
        field = value
        when(value){
            "sendText"->{
                onVerticalSwipeFn = { dir ->
                    val txt = if(params.containsKey("vText")) getParamString("vText") else getParamString("text")
                    if(txt.isNotEmpty()) OwnboardIME.ime.sendKeyPress(txt) 
                }
            }
            "sendCode"->{
                 onVerticalSwipeFn = { dir ->
                    val code = if(params.containsKey("vCode")) getParamInt("vCode") else getParamInt("code")
                    OwnboardIME.ime.sendKeyPress(code) 
                }
            }
            "switchLang"->{
                onVerticalSwipeFn = { performLangSwitch() }
            }
             "delete" -> {
                onVerticalSwipeFn = { OwnboardIME.ime.delete() }
            }
            "holdSpecial"->{
                onVerticalSwipeFn = { enable(1) }
            }
           
            else-> onVerticalSwipeFn={}
        }
    }

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
                val valu = it ?: 0
                if (valu == 0) {
                    setBackgroundColor(0xFF2D2D2D.toInt())
                    disable()
                }else {
                    setBackgroundColor(Color.CYAN)
                }
            }
        }
    open var onClickFn = {}
    open var onLongPressFn  = {}
    
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
    
    open fun disable() {
        val code = getParamInt("code")
        OwnboardIME.ime.sendKeyUp(code)
        listener.value = 0
        setBackgroundColor(0xFF2D2D2D.toInt())
    }
    
    open fun enable(hold: Int = 0) {
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
                var value = it ?: 0
                if (value != 0) {
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
            btn.setBackgroundColor(if (i == index) Color.CYAN else Color.LTGRAY)
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