package com.ownboard.app

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.*
import android.util.*
import android.view.*
import android.widget.*
import com.ownboard.app.OwnboardIME
import com.ownboard.app.R
import kotlin.math.abs

open class Key
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = android.R.attr.buttonStyle
) : FrameLayout(context, attrs, defStyle) {
    
    val screenWidth = context.resources.displayMetrics.widthPixels
    val screenHeight = context.resources.displayMetrics.heightPixels

    // ============================================================
    // دوال المعالجة الجديدة (New Swipe Handlers)
    // ============================================================
    // Int Parameter indicates direction:
    // Horizontal: 1 = Right, -1 = Left
    // Vertical:   1 = Down,  -1 = Up
    var onHorizontalSwipeFn: (Int) -> Unit = {}
    var onVerticalSwipeFn: (Int) -> Unit = {}
    
    // تم حذف onLeftScrollFn و onRightScrollFn واستبدالهم بالدوال أعلاه

    var hint = ""
        set(value) {
            field = value
            popupKeys = if (value.isNotEmpty()) value.split(" ") else emptyList()
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
    private var _backgroundImg: Drawable? = null

    val longPressTimeout = 200L
    var isHoldKey = false
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
    
    // متغيرات اللمس
    var touchStartX = 0f
    var touchStartY = 0f // إضافة Y لدعم السحب العمودي
    var offset = 0
    
    open var mtextSize = 18f
        set(value) {
            field = value
            textPaint.textSize = spToPx(value)
            invalidate()
        }

    open var hintSize = 10f
        set(value) {
            field = value
            topRightPaint.textSize = spToPx(value)
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
        setWillNotDraw(false)
    }

    private fun initAttributes(attrs: AttributeSet?) {
        if (attrs == null) return
        val ta = context.obtainStyledAttributes(attrs, R.styleable.keyAttrs)
        try {
            text = ta.getString(R.styleable.keyAttrs_text) ?: ""
            hint = ta.getString(R.styleable.keyAttrs_popupKeys) ?: ""
        } finally {
            ta.recycle()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    open fun onKeyPress() {
        if (Key.capslock.value != 2) Key.capslock.value = 0
        if (Key.ctrl.value != 2) Key.ctrl.value = 0
        if (Key.alt.value != 2) Key.alt.value = 0
        if (Key.shift.value != 2) Key.shift.value = 0
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
        
        // رسم الخلفية
        canvas.drawColor(Color.TRANSPARENT)
        val rectPaint = Paint().apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(inset, inset, width.toFloat() - inset, height.toFloat() - inset, rectPaint)
        
        // رسم الصورة إذا وجدت
        _backgroundImg?.let {
            val imgcx = it.intrinsicWidth / 2
            val imgcy = it.intrinsicHeight / 2
            it.setBounds(cx - imgcx, cy - imgcy, cx + imgcx, cy + imgcy) 
            it.draw(canvas)
            return@onDraw
        }
        
        // رسم النص
        canvas.drawText(text, centerX, centerY, textPaint)
        
        // رسم التلميح
        val fm = topRightPaint.fontMetrics
        val y = inset - fm.ascent
        val topTextX = width.toFloat() - 2 * inset
        canvas.drawText(hint.split(" ").take(2).joinToString(""), topTextX, y, topRightPaint) 
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
        touchStartY = e.y // حفظ نقطة البداية العمودية
        offset = 0
        return true
    }

    open fun actionUp(e: MotionEvent): Boolean {
        isHoldKey = false
        longPressHandler.removeCallbacks(longPressRunnable)
        val pressDuration = e.eventTime - e.downTime
        
        // إعادة تعيين لون الخلفية
        if (!isSpecialKey) setBackgroundColor(0xFF2D2D2D.toInt())

        // تحديد عتبة السحب (الحساسية)
        val threshold = btnWidth / 2.5 // تقليل العتبة قليلاً لتسهيل السحب

        val dx = e.x - touchStartX
        val dy = e.y - touchStartY
        
        // التحقق من السحب (Swipe Check)
        if (pressDuration < longPressTimeout) {
            
            // تحديد الاتجاه الأقوى (أفقي أم عمودي؟)
            if (abs(dx) > abs(dy)) {
                // --- معالجة السحب الأفقي ---
                if (abs(dx) > threshold) {
                    val direction = if (dx > 0) 1 else -1 // 1: يمين، -1: يسار
                    post {
                        onHorizontalSwipeFn(direction)
                        setBackgroundColor(0xFF2D2D2D.toInt())
                    }
                    // تم تنفيذ سحب، نخرج ولا ننفذ Click
                    return true 
                }
            } else {
                // --- معالجة السحب العمودي ---
                if (abs(dy) > threshold) {
                    val direction = if (dy > 0) 1 else -1 // 1: أسفل، -1: أعلى
                    post {
                        onVerticalSwipeFn(direction)
                        setBackgroundColor(0xFF2D2D2D.toInt())
                    }
                    // تم تنفيذ سحب، نخرج ولا ننفذ Click
                    return true
                }
            }
        }

        // التحقق من أن الإصبع ما زال فوق الزر للنقر العادي
        if (!isActionUp(e.rawX.toInt(), e.rawY.toInt())) return false
        
        // تنفيذ النقر العادي إذا لم يتحقق أي شرط سحب
        if (pressDuration < longPressTimeout) {
            onClick()
        }
        
        return true
    }

    open fun actionMove(e: MotionEvent): Boolean {
        // نحدث الـ offset فقط للأغراض التي قد تحتاجها لاحقاً
        // لكن المنطق الرئيسي يعتمد الآن على dx/dy في actionUp
        offset = (e.x - touchStartX).toInt()
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
        return dp * context.resources.displayMetrics.density
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

    // دوال مساعدة للإرسال
    fun sendText() {
        OwnboardIME.ime.sendKeyPress(text)
    }

    fun sendKeyCode() {
        OwnboardIME.ime.sendKeyPress(keyCode)
    }

    fun sendKeyPress(param: String) {
        OwnboardIME.ime.sendKeyPress(param) 
    }

    fun sendKeyPress(param: Int) {
        OwnboardIME.ime.sendKeyPress(param)
    }
}