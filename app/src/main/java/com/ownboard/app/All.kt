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
        // ... (المتغيرات الموجودة سابقاً مثل capslock وغيرها) ...

        // ============================================================
        // قوائم الوظائف المتاحة (Static Lists for Dropdowns)
        // ============================================================
        
        val CLICK_FUNCTIONS = listOf(
            "", // خيار فارغ
            "sendText",      // إرسال نص
            "sendCode",      // إرسال كود زر
            "sendSpecial",   // وظيفة خاصة (Shift/Ctrl/Alt)
            "switchLang",    // تغيير اللغة
            "switchSymbols", // تبديل الرموز
            "delete",        // حذف
            "openEmoji",     // فتح الإيموجي
            "openClipboard"  // فتح الحافظة
        )

        val LONG_PRESS_FUNCTIONS = listOf(
            "",
            "loop",          // تكرار الحرف عند التعليق
            "showPopup",     // إظهار القائمة المنبثقة
            "sendText",
            "sendCode",
            "switchLang",
             "holdSpecial"
        )

        val SCROLL_FUNCTIONS = listOf(
            "",
            "sendText",
            "sendCode",
            "switchLang",
             "holdSpecial"  
        )
    }
    open var click = ""
    set(value) {
        field = value
        when(value){
            "sendText"->{
                onClickFn= {
                    OwnboardIME.ime.sendKeyPress(textToSend) 
                }
            }
            "sendCode"->{
                 onClickFn = {
                    OwnboardIME.ime.sendKeyPress(codeToSendClick) 
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
                // codeToSendClick=codeToSendClick
                onClickFn = {
                    if ((listener.value ?: 1) != 0) disable() else enable()
                }
                onLongPressFn = {enable(1)}
            }
            "switchLang"->{
                onClickFn = {
                    OwnboardIME.ime.switchLang()
                    Key.ctrl.notifyListeners()
                    Key.alt.notifyListeners()
                    Key.shift.notifyListeners()
                    Key.capslock.notifyListeners()
                }
            }
             "delete"->{
                onClickFn = {
                    OwnboardIME.ime.delete() 
                }
            }
            // داخل دالة معالجة الأزرار
            "openClipboard" -> {
                onClickFn = {
                    //OwnboardIME.ime.delete() 
                    OwnboardIME.ime.toggleClipboard()
                }
                // OwnboardIME.ime.keyboardContainer.visibility = View.GONE // اختياري: إخفاء الكيبورد
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
                    OwnboardIME.ime.sendKeyPress(textToSendLongPress) 
                }
            }
            "sendCode"->{
                 onLongPressFn= {
                    OwnboardIME.ime.sendKeyPress(codeToSendLongPress) 
                }
            }
            "showPopup"->{
                // onLongPressFn= { OwnboardIME.ime.sendKeyPress(textToSend) } 
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
                onLongPressFn = {
                    OwnboardIME.ime.switchLang()
                    Key.ctrl.notifyListeners()
                    Key.alt.notifyListeners()
                    Key.shift.notifyListeners()
                    Key.capslock.notifyListeners()
                }
            }
            "holdSpecial"->{
                onLongPressFn = {
                    enable(1)
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
                    OwnboardIME.ime.sendKeyPress(textToSendLeftScroll) 
                }
            }
            "sendCode"->{
                 onLeftScrollFn= {
                    OwnboardIME.ime.sendKeyPress(codeToSendLeftScroll) 
                }
            }
            "switchLang"->{
                onLeftScrollFn  = {
                    OwnboardIME.ime.switchLang()
                    Key.ctrl.notifyListeners()
                    Key.alt.notifyListeners()
                    Key.shift.notifyListeners()
                    Key.capslock.notifyListeners()
                }
            }
            "holdSpecial"->{
                 onLeftScrollFn= {
                    enable(1)
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
                    OwnboardIME.ime.sendKeyPress(textToSendRightScroll) 
                }
            }
            "sendCode"->{
                 onRightScrollFn= {
                    OwnboardIME.ime.sendKeyPress(codeToSendRightScroll) 
                }
            }
            "switchLang"->{
                onRightScrollFn = {
                    OwnboardIME.ime.switchLang()
                    Key.ctrl.notifyListeners()
                    Key.alt.notifyListeners()
                    Key.shift.notifyListeners()
                    Key.capslock.notifyListeners()
                }
            }
            "holdSpecial"->{
                onRightScrollFn = {
                     enable(1)
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
        OwnboardIME.ime.sendKeyUp(codeToSendClick)
        listener.value = 0
        setBackgroundColor(0xFF2D2D2D.toInt())
    }
    
    open fun enable(hold: Int = 0) {
        listener.value = hold + 1
        OwnboardIME.ime.sendKeyDown(codeToSendClick)
    }

    // التعديل الهام هنا: ربط popupContainer مباشرة
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
       val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
        layoutParams = params
        if(!isConstKey){
            Key.capslock.addListener { 
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

        // 1. الحساب المبدئي للمكان لتحديد الاتجاه
        val btnLoc = IntArray(2)
        getLocationOnScreen(btnLoc)
        
        // حساب العرض المتوقع للقائمة كاملة
        val estimatedWidth = (btnWidth * popupKeys.size)
        
        // === الشرط الذي كان ناقصاً ===
        // هل مكان الزر الحالي + عرض القائمة سيخرج عن عرض الشاشة؟
        // إذا نعم، نعتبر الحالة "معكوسة" (Reversed)
        val isReversed = (btnLoc[0] + estimatedWidth) > screenWidth

        // 2. ترتيب القائمة بناءً على الحالة
        // إذا عكسنا القائمة (عشان تظهر يسار الزر)، نعكس ترتيب الحروف أيضاً لتكون قريبة من الإصبع
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

        // 3. الحساب النهائي للموقع والإظهار
        popupContainer.post {
            if (!isPopupVisible) return@post

            val rootViewLoc = IntArray(2)
            // نعيد جلب مكان الزر للتأكد
            getLocationOnScreen(btnLoc)
            OwnboardIME.ime.rootView.getLocationOnScreen(rootViewLoc)

            val actualWidth = popupContainer.width.toFloat()
            val actualHeight = popupContainer.height.toFloat()

            // === حساب X (الموقع الأفقي) ===
            var x: Float
            
            if (isReversed) {
                // الحالة: الزر في اليمين والمساحة لا تكفي
                // المعادلة: (مكان الزر) - (مكان الروت) + (عرض الزر) - (عرض القائمة)
                // هذا يجعل الحافة اليمنى للقائمة محاذية للحافة اليمنى للزر
                x = (btnLoc[0] - rootViewLoc[0] + btnWidth - actualWidth)
            } else {
                // الحالة العادية: الزر في اليسار
                // القائمة تبدأ من يسار الزر
                x = (btnLoc[0] - rootViewLoc[0]).toFloat()
            }

            // حماية إضافية: لا تسمح للقائمة بالخروج من يسار الشاشة
            if (x < 0) x = 0f

            // === حساب Y (الموقع العمودي) ===
            val y = (btnLoc[1] - rootViewLoc[1] - actualHeight - dpToPx(2f))

            popupContainer.x = x
            popupContainer.y = y
            
            popupContainer.visibility = View.VISIBLE
            popupContainer.bringToFront()
        }
    }
    private fun showAltChars1() {
        // 1. إخفاء الحاوية تماماً وتنظيفها
        popupContainer.visibility = View.INVISIBLE // نستخدم INVISIBLE وليس GONE مؤقتاً ليتمكن النظام من قياسها
        popupContainer.removeAllViews()
        popupContainer.setBackgroundColor(Color.WHITE)
        
        // 2. إضافة الأزرار (تعبئة المحتوى)
        // التحقق المبدئي هل نحتاج للعكس أم لا
        val estimatedWidth = (btnWidth * popupKeys.size) + dpToPx(10f)
        val btnScreenLoc = IntArray(2)
        getLocationOnScreen(btnScreenLoc)
        val isReversed = (btnScreenLoc[0] + estimatedWidth) > screenWidth
        
        val listToShow = if (!isReversed) popupKeys.reversed() else popupKeys
        
        listToShow.forEach { altChar ->
            val altBtn = Button(context).apply {
                text = altChar.toString().lowercase()
                setTextColor(Color.BLACK)
                textSize = 18f
                setPadding(5, 0, 5, 0) // تقليل الحواشي
                minWidth = 0 
                minimumWidth = 0
                typeface = Typeface.DEFAULT_BOLD
                // نجبر الأزرار أن تأخذ نفس عرض الزر الأصلي
                layoutParams = LinearLayout.LayoutParams(btnWidth, LinearLayout.LayoutParams.WRAP_CONTENT)
                
                setOnClickListener {
                    OwnboardIME.ime.sendKeyPress(altChar.toString())
                    onKeyPress()
                    popupContainer.visibility = View.GONE
                    isPopupVisible = false
                    popupBtns.clear()
                    selectedIndex = 0
                    isLongPressed = false
                }
            }
            popupBtns.add(altBtn)
            popupContainer.addView(altBtn)
        }

        // 3. (الجزء السحري) نؤجل حساب الموقع والحجم لجزء من الثانية حتى ينتهي النظام من إضافة الأزرار
        popupContainer.post {
            if (!isPopupVisible) return@post // حماية في حال رفع المستخدم إصبعه بسرعة

            val rootViewLoc = IntArray(2)
            val btnLoc = IntArray(2)
            
            // نجلب المواقع الحالية بدقة بعد الرسم
            getLocationOnScreen(btnLoc)
            OwnboardIME.ime.rootView.getLocationOnScreen(rootViewLoc)
            
            // عرض الـ Popup الفعلي بعد أن تم رسمه
            val actualPopupWidth = popupContainer.width.takeIf { it > 0 } ?: (btnWidth * popupKeys.size)
            
            // حساب X (اليسار واليمين)
            // بما أن الروت FrameLayout، الإحداثيات نسبية (نطرح موقع الروت من موقع الزر)
            val relativeBtnX = btnLoc[0] - rootViewLoc[0]
            
            // التحقق النهائي من المساحة
            val finalReversed = (btnLoc[0] + actualPopupWidth) > screenWidth
            
            val x = if (finalReversed) {
                // إذا انعدم المكان يمين، نضعه يمين الشاشة تماماً أو بمحاذاة الزر
                 // (relativeBtnX + btnWidth - actualPopupWidth).toFloat() // محاذاة لليمين
                 (screenWidth - actualPopupWidth - rootViewLoc[0]).toFloat() // يلتصق بحافة الشاشة اليمنى
            } else {
                (relativeBtnX - actualPopupWidth + btnWidth).toFloat()
            }

            // حساب Y (الارتفاع)
            // نضعه فوق الزر مباشرة
            val relativeBtnY = btnLoc[1] - rootViewLoc[1]
            val y = (relativeBtnY - popupContainer.height).toFloat() // نرفعه بمقدار ارتفاعه هو

            // تطبيق الإحداثيات
            popupContainer.x = x
            // إذا كان ارتفاع الـ popup مازال 0 (نادر الحدوث مع post)، نستخدم تقدير
            popupContainer.y = if(popupContainer.height > 0) y else (relativeBtnY - dpToPx(55f))
            
            // أخيراً الإظهار الفعلي
            popupContainer.visibility = View.VISIBLE
            popupContainer.bringToFront()
            popupContainer.requestLayout() // تأكيد الرسم
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