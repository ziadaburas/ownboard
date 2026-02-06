package com.example.ime.views

import android.content.Context
import android.util.AttributeSet
import android.graphics.Color
import android.graphics.Typeface
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import com.example.ime.FlutterIME
import com.example.ime.R
import com.example.ime.views.Key
import android.view.HapticFeedbackConstants


class All
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = android.R.attr.buttonStyle,
) : Key(context, attrs, defStyle) {

    open var click = ""
    set(value) {
        field = value
        when(value){
            "sendText"->{
                onClickFn= {
                    FlutterIME.ime.sendKeyPress(textToSend) 
                }
            }
            "sendCode"->{
                 onClickFn = {
                    FlutterIME.ime.sendKeyPress(codeToSendClick) 
                }
            }
            "openClipboard"->{
                backgroundImg = R.drawable.ic_clipboard
                onClickFn = {
                    val clip = FlutterIME.ime.rootView.findViewById<com.example.ime.views.ClipboardView>(R.id.clipboard)
                    clip.refresh()
                    clip.visibility = android.view.View.VISIBLE
                }
            }
            "openEmoji"->{
                backgroundImg = R.drawable.emoji_language
                onClickFn = {
                    val clip = FlutterIME.ime.rootView.findViewById<com.example.ime.views.EmojiView>(R.id.emoji)
                    clip.refresh()
                    clip.visibility = android.view.View.VISIBLE
                }
            }
            "switchSymbols"->{
                onClickFn = {
                    Key.isSymbols.value = !(Key.isSymbols.value ?: true)
                    FlutterIME.ime.switchSymbols(Key.isSymbols.value == true)
                }
                
                text = if (Key.isSymbols.value == true) "abc" else "123"
                Key.isSymbols.addListener { newVal -> text = if (newVal == true) "abc" else "123" }
                
                }
            "sendSpecial"->{
                codeToSendClick=codeToSendClick
                onClickFn = {
                    if ((listener.value ?: 1) != 0) disable() else enable()
                }
                onLongPressFn = {enable(1)}
            }
            "switchLang"->{
                onClickFn = {
                    FlutterIME.ime.switchLang()
                    Key.ctrl.notifyListeners()
                    Key.alt.notifyListeners()
                    Key.shift.notifyListeners()
                    Key.capslock.notifyListeners()
                }
            }
             "delete"->{
                onClickFn = {
                    FlutterIME.ime.delete() 
                }
            }

            else -> {onClickFn={}}
        }
    }
    open var longPress = ""
    set(value) {
        field = value
        when(value){
            "sendText"->{
                onLongPressFn= {
                    FlutterIME.ime.sendKeyPress(textToSendLongPress) 
                }
            }
            "sendCode"->{
                 onLongPressFn= {
                    FlutterIME.ime.sendKeyPress(codeToSendLongPress) 
                }
            }
            "openClipboard"->{
                
                onLongPressFn = {
                    val clip = FlutterIME.ime.rootView.findViewById<com.example.ime.views.ClipboardView>(R.id.clipboard)
                    clip.refresh()
                    clip.visibility = android.view.View.VISIBLE
                }
            }
            "openEmoji"->{
                
                onLongPressFn = {
                    val clip = FlutterIME.ime.rootView.findViewById<com.example.ime.views.EmojiView>(R.id.emoji)
                    clip.refresh()
                    clip.visibility = android.view.View.VISIBLE
                }
            }
            
            "showPopup"->{
                onLongPressFn= {
                    FlutterIME.ime.sendKeyPress(textToSend) 
                }
                onLongPressFn= {
                    if (popupKeys.isNotEmpty()){
                        EmojiView.isScrollable.value = true
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
                onLongPressFn = {
                    FlutterIME.ime.switchLang()
                    Key.ctrl.notifyListeners()
                    Key.alt.notifyListeners()
                    Key.shift.notifyListeners()
                    Key.capslock.notifyListeners()
                }
            }
            else-> onLongPressFn={}
        }
    }
    open var leftScroll = ""
    set(value) {
        field = value
        when(value){
            "sendText"->{
                onLeftScrollFn= {
                    FlutterIME.ime.sendKeyPress(textToSendLeftScroll) 
                }
            }
            "sendCode"->{
                 onLeftScrollFn= {
                    FlutterIME.ime.sendKeyPress(codeToSendLeftScroll) 
                }
            }
            "openClipboard"->{
                backgroundImg = R.drawable.ic_clipboard
                onLeftScrollFn = {
                    val clip = FlutterIME.ime.rootView.findViewById<com.example.ime.views.ClipboardView>(R.id.clipboard)
                    clip.refresh()
                    clip.visibility = android.view.View.VISIBLE
                }
            }
            "openEmoji"->{
                backgroundImg = R.drawable.emoji_language
                onLeftScrollFn = {
                    val clip = FlutterIME.ime.rootView.findViewById<com.example.ime.views.EmojiView>(R.id.emoji)
                    clip.refresh()
                    clip.visibility = android.view.View.VISIBLE
                }
            }
            "switchLang"->{
                onRightScrollFn = {
                    FlutterIME.ime.switchLang()
                    Key.ctrl.notifyListeners()
                    Key.alt.notifyListeners()
                    Key.shift.notifyListeners()
                    Key.capslock.notifyListeners()
                }
            }
           
            else-> onLeftScrollFn={}
        }
    }
    open var rightScroll = ""
    set(value) {
        field = value
        when(value){
            "sendText"->{
                onRightScrollFn= {
                    FlutterIME.ime.sendKeyPress(textToSendRightScroll) 
                }
            }
            "sendCode"->{
                 onRightScrollFn= {
                    FlutterIME.ime.sendKeyPress(codeToSendRightScroll) 
                }
            }
            "openClipboard"->{
                backgroundImg = R.drawable.ic_clipboard
                onRightScrollFn = {
                    val clip = FlutterIME.ime.rootView.findViewById<com.example.ime.views.ClipboardView>(R.id.clipboard)
                    clip.refresh()
                    clip.visibility = android.view.View.VISIBLE
                }
            }
            "openEmoji"->{
                backgroundImg = R.drawable.emoji_language
                onRightScrollFn = {
                    val clip = FlutterIME.ime.rootView.findViewById<com.example.ime.views.EmojiView>(R.id.emoji)
                    clip.refresh()
                    clip.visibility = android.view.View.VISIBLE
                }
            }
            "switchLang"->{
                onRightScrollFn = {
                    FlutterIME.ime.switchLang()
                    Key.ctrl.notifyListeners()
                    Key.alt.notifyListeners()
                    Key.shift.notifyListeners()
                    Key.capslock.notifyListeners()
                }
            }
           
            else-> onRightScrollFn={}
        }
    }
    var textToSend = ""
    var textToSendLongPress = ""
    var codeToSendLongPress = -1
    var textToSendRightScroll = ""
    var codeToSendRightScroll = -1
    var textToSendLeftScroll = ""
    var codeToSendLeftScroll = -1
    var codeToSendClick = -1
    set(value) {
        field = value
        isSpecialKey = true
        when(value){
            KeyEvent.KEYCODE_SHIFT_LEFT->listener=Key.shift
            KeyEvent.KEYCODE_ALT_LEFT->listener=Key.alt
            KeyEvent.KEYCODE_CTRL_LEFT->listener=Key.ctrl
            115 ->listener=Key.capslock
            else->isSpecialKey = false
        }
        if(value > 0)
        isConstKey = true 
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
        FlutterIME.ime.sendKeyUp(codeToSendClick)
        listener.value = 0
        setBackgroundColor(0xFF2D2D2D.toInt())
    }
    open fun enable(hold: Int = 0) {
        listener.value = hold + 1
        FlutterIME.ime.sendKeyDown(codeToSendClick)
    }
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        popupContainer = FlutterIME.ime.rootView.findViewById<LinearLayout>(R.id.altPopupContainer)
    }
    override fun actionUp(e: MotionEvent): Boolean {
        super.actionUp(e)
        if (isPopupVisible && autoHidePopup) {
            if (popupKeys.isNotEmpty() && selectedIndex in popupKeys.indices) {
                popupBtns[selectedIndex].performClick() //FlutterIME.ime.sendKeyPress(popupKeys[selectedIndex].toString())
                onKeyPress()
            }
            popupContainer.visibility = View.GONE
            isPopupVisible = false
            popupBtns.clear()
            selectedIndex = 0
            isLongPressed = false
            EmojiView.isScrollable.value = false
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
       val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
        layoutParams = params
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
        popupContainer.setBackgroundColor(Color.WHITE)
        popupContainer.visibility = View.VISIBLE
        val rootViewLoc = IntArray(2)
        val btnLoc = IntArray(2)
        getLocationOnScreen(btnLoc)
        FlutterIME.ime.rootView.getLocationOnScreen(rootViewLoc)
        popupWidth = (btnWidth * popupKeys.size).toFloat()
        val y = btnLoc[1] - rootViewLoc[1] - dpToPx(48f)
        val isReversed = btnLoc[0] + popupWidth + dpToPx(3f) < screenWidth
        val x = if (isReversed) btnLoc[0].toFloat()
            else btnLoc[0] - popupWidth + btnWidth
        popupContainer.x = x
        popupContainer.y = y.toFloat()
        popupBtns.clear()
        
        ( if(! isReversed) popupKeys.reversed() else popupKeys).forEach { altChar ->
            val altBtn = Button(context).apply {
                text = altChar.toString().lowercase()
                setPadding(0, 5, 0, 5)
                setTypeface(null, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(btnWidth, LinearLayout.LayoutParams.MATCH_PARENT)
                setOnClickListener {
                    FlutterIME.ime.sendKeyPress(altChar.toString())
                    onKeyPress()
                    popupContainer.visibility = View.GONE
                    isPopupVisible = false
                    popupBtns.clear()
                    selectedIndex = 0
                    isLongPressed = false
                    EmojiView.isScrollable.value = false
                }
            }
            popupBtns.add(altBtn)
            popupContainer.addView(altBtn)
        }
    }
    private fun highlightButton(index: Int) {
        popupBtns.forEachIndexed { i, btn ->
            btn.setBackgroundColor(if (i == index) Color.CYAN else Color.LTGRAY)
        }
    }
    private fun clearHighlight() {
        popupBtns.forEach { btn -> btn.setBackgroundColor(Color.LTGRAY) }
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
