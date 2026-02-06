package com.example.ime.views

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.*
import android.util.*
import android.view.*
import android.widget.*
import com.example.ime.FlutterIME
import com.example.ime.R

open class Key
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = android.R.attr.buttonStyle
) : FrameLayout(context, attrs, defStyle) {
    val screenWidth = context.resources.displayMetrics.widthPixels
    val screenHeight = context.resources.displayMetrics.heightPixels
    var onLeftScrollFn={}
    var onRightScrollFn={}
    var hint = ""
        set(value) {
            field = value
            popupKeys = if (value.length != 0) value.split(" ") else emptyList()
            popupWidth = (btnWidth * popupKeys.size).toFloat()
            invalidate()
        }
    var btnWidth = 0
    open var backgroundImg = 0
    set(value) {
        field = value
        if (value != 0) {
            _backgroundImg = context.getDrawable(value)
            _backgroundImg?.setBounds(0, 0, _backgroundImg!!.intrinsicWidth, _backgroundImg!!.intrinsicHeight)
        } else {
            _backgroundImg = null
        }
        invalidate()
    }
    private var _backgroundImg:Drawable? = null

    val longPressTimeout = 200L
    var isHoldKey = false
    var isHoldPressed = false
    val longPressHandler = Handler(Looper.getMainLooper())
    var isLongPressed = false
    var isSpecialKey = false
    var isConstKey = false
    var longPressRunnable = Runnable { onLongPress() }
    var bgColor: Int = 0xFF2D2D2D.toInt()
    
    var text = ""
        set(value) {
            field = value
            invalidate()
        }
    var inset = 0f
    var keyWidth = 0
    var keyHeight = -1
    var keyWeight = 1f
    open var keyCode = 0
    var touchStartX = 0f
    var offset = 0
    open var mtextSize = 18f
        set(value) {
            field = value
            textPaint.textSize = (value)
            invalidate()
        }

    open var hintSize = 10f
        set(value) {
            field = value
            topRightPaint.textSize = (value)
            invalidate()
        }

    val topRightPaint = Paint().apply {
        color = 0xFFAAAAAA.toInt()
        textAlign = Paint.Align.RIGHT
        textSize = spToPx(hintSize)
        isAntiAlias = true
    }
    open val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = spToPx(mtextSize)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
        }
    var popupKeys: List<String> = emptyList()
    var popupWidth = 0f
    companion object {
        val capslock = ValueListener(0)
        val ctrl = ValueListener(0)
        val alt = ValueListener(0)
        val shift = ValueListener(0)
        val keyPress = ValueListener("")
        val isSymbols = ValueListener(false)
    }
    init {
        initAttributes(attrs)
        inset = dpToPx(2f)
        
        // background = null
        setWillNotDraw(false)
    }
    private fun initAttributes(attrs: AttributeSet?) {
        if (attrs == null) return
        val ta = context.obtainStyledAttributes(attrs, R.styleable.keyAttrs)
        try {
            text = ta.getString(R.styleable.keyAttrs_text) ?: ""
            hint = ta.getString(R.styleable.keyAttrs_popupKeys) ?: ""
            // hint = contentDescription.toString().replace(" ", "")
        } finally {
            ta.recycle()
        }
    }
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }
    open fun onKeyPress() {
        if ((Key.capslock.value ?: 1) != 2) Key.capslock.value = 0
        if ((Key.ctrl.value ?: 1) != 2) Key.ctrl.value = 0
        if ((Key.alt.value ?: 1) != 2) Key.alt.value = 0
        if ((Key.shift.value ?: 1) != 2) Key.shift.value = 0
    }
    open fun onClick() {
        onKeyPress()
    }
    open fun onLongPress() {
        isLongPressed = true
    }
    override fun onDraw(canvas: Canvas) {
        
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = (height / 2f - (textPaint.descent() + textPaint.ascent()) / 2)
        val cx = width / 2
        val cy = height / 2
        
        canvas.drawColor(Color.TRANSPARENT)
        val rectPaint = Paint().apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(inset, inset, width.toFloat() - inset, height.toFloat() - inset, rectPaint)
        
        _backgroundImg?.let {
            val imgcx =  it.intrinsicWidth/2
            val imgcy = it.intrinsicHeight/2
            it.setBounds(cx-imgcx, cy-imgcy,cx+imgcx, cy+imgcy) 
            it.draw(canvas)
            return@onDraw
        }
        canvas.drawText(text.toString(), centerX, centerY, textPaint)
        val fm = topRightPaint.fontMetrics
        val y = inset - fm.ascent
        val topTextX = width.toFloat() - 2 * inset
        canvas.drawText(hint.split(" ").take(2).joinToString(""),topTextX,y,topRightPaint) 

    }
  
    override fun setBackgroundColor(color: Int) {
        super.setBackgroundColor(Color.TRANSPARENT)
        bgColor = color
        invalidate()
    }
    override fun setTag(t: Any) {
        super.setTag(t)
        invalidate()
    }
    open fun actionDown(e: MotionEvent): Boolean {
        isLongPressed = false
        longPressHandler.postDelayed(longPressRunnable, longPressTimeout)
        isHoldKey = true
        setBackgroundColor(Color.CYAN.toInt())
        
        touchStartX = e.x 
        offset = 0
        return true
    }
    open fun actionUp(e: MotionEvent): Boolean {
        isHoldKey = false
        longPressHandler.removeCallbacks(longPressRunnable)
        val pressDuration = e.eventTime - e.downTime
        
        if (!isSpecialKey) setBackgroundColor(0xFF2D2D2D.toInt())
        if (offset > (btnWidth / 2) && pressDuration < longPressTimeout) post { 
            // Key.ctrl.notifyListeners()
            // Key.alt.notifyListeners()
            // Key.shift.notifyListeners()
            // Key.capslock.notifyListeners()
            onLeftScrollFn()
            onRightScrollFn()
            setBackgroundColor(0xFF2D2D2D.toInt())
            
        }
        if (!isActionUp(e.rawX.toInt(), e.rawY.toInt())) return false
        if (pressDuration < longPressTimeout) {
            onClick()
        }
        
       // setBackgroundColor(0xFF2D2D2D.toInt())
        
        return true
    }
    open fun actionMove(e: MotionEvent): Boolean {
        offset = Math.abs(e.x - touchStartX).toInt()
        return true
    }
    override fun onTouchEvent(e: MotionEvent): Boolean {
        super.onTouchEvent(e)
        return when (e.action) {
            MotionEvent.ACTION_DOWN -> actionDown(e)
            MotionEvent.ACTION_MOVE -> actionMove(e)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> actionUp(e)
            else -> false
        }
    }
    fun isActionUp(x: Int, y: Int): Boolean {
        val loc = IntArray(2)
        getLocationOnScreen(loc)
        val left = loc[0]
        val top = loc[1]
        val right = left + width
        val bottom = top + height
        return (x in left..right && y in top..bottom)
    }
    fun dpToPx(dp: Float): Float {
        return dp * rootView.context.resources.displayMetrics.density
    }
    fun spToPx(sp: Float): Float =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            context.resources.displayMetrics
        )
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        btnWidth = w
        popupWidth = (btnWidth * popupKeys.size).toFloat()
    }
    fun sendText() {
        FlutterIME.ime.sendKeyPress(text)
    }
    fun sendKeyCode(){
        FlutterIME.ime.sendKeyPress(keyCode)
    }
    val functionMap: Map<String, (Any?) -> Unit> = mapOf(
    "sendText" to { param -> sendKeyPress(param as String) },
    "sendKeyCode" to { param -> sendKeyPress((param as String).toInt()) },
    "sendBtnText" to { _ -> sendKeyPress(text) },
    "sendBtnKeyCode" to { _ -> sendKeyPress(keyCode) },
//     "sendKeyCode" to { _ -> sendKeyCode() }
)
    fun sendKeyPress(param: String) {
        FlutterIME.ime.sendKeyPress(param) 
    }
    fun sendKeyPress(param: Int) {
        FlutterIME.ime.sendKeyPress(param)
    }

}
