package com.ownboard.app.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ownboard.app.OwnboardIME
import com.ownboard.app.db.ClipboardDbHelper

class ClipboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    // متغير التحكم بالإغلاق بعد اللصق (يمكن تغييره من الإعدادات)
    var closeClipboardAfterPaste: Boolean = true

    // المتغيرات
    private lateinit var mainLayout: LinearLayout
    private lateinit var topRow: LinearLayout
    private lateinit var centerContainer: FrameLayout // حاوية تجمع القائمة والديالوج
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ClipboardAdapter
    private lateinit var dialog: LinearLayout
    private lateinit var textViewDialog: TextView

    // الأزرار العلوية
    lateinit var closeBtn: Button
    lateinit var goToTopBtn: Button
    lateinit var goToDownBtn: Button
    lateinit var goToSettingsBtn: Button

    // أزرار الديالوج
    lateinit var closeDialogBtn: Button
    lateinit var copyDialogBtn: Button
    lateinit var deleteDialogBtn: Button
    lateinit var pinDialogBtn: Button

    val clipboardDB = ClipboardDbHelper(context)

    init {
        visibility = View.GONE
        setBackgroundColor(Color.parseColor("#121212"))

        // 1. إنشاء التخطيط الرئيسي (عمودي)
        mainLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }
        addView(mainLayout)

        // 2. تصميم الشريط العلوي والأزرار
        topRow = LinearLayout(context).apply {
            setBackgroundColor(0xFF121212.toInt())
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(40f).toInt() // ارتفاع ثابت للشريط العلوي
            )
        }

        // 3. إنشاء حاوية الوسط (تحتوي على القائمة والديالوج فوق بعضهما)
        centerContainer = FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f // تأخذ باقي المساحة
            )
        }

        // 4. تهيئة القائمة (RecyclerView)
        recyclerView = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        adapter = ClipboardAdapter(
            onItemClick = { text -> onPaste(text) },
            onItemLongClick = { item -> showDialog(item.text, item.isPinned) }
        )
        recyclerView.adapter = adapter

        // إضافة القائمة لحاوية الوسط
        centerContainer.addView(recyclerView)

        // 5. تصميم الديالوج (داخل حاوية الوسط ليظهر فوق القائمة فقط)
        textViewDialog = TextView(context).apply {
            text = ""
            setTextColor(Color.WHITE)
            textSize = 16f
            setBackgroundColor(0xFF2D2D2D.toInt())
            gravity = Gravity.CENTER
            // النص يأخذ المساحة المتبقية داخل الديالوج
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        }

        val bottomRowDialog = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(50f).toInt())
        }

        val dialogBtnParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
            setMargins(2, 2, 2, 2)
        }

        closeDialogBtn = createDialogButton("اغلاق", dialogBtnParams) { hideDialog() }
        
        copyDialogBtn = createDialogButton("نسخ", dialogBtnParams) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("copied", textViewDialog.text.toString())
            clipboard.setPrimaryClip(clip)
            refresh()
            hideDialog()
        }
        
        deleteDialogBtn = createDialogButton("حذف", dialogBtnParams) {
            clipboardDB.deleteText(textViewDialog.text.toString())
            refresh()
            hideDialog()
        }
        
        pinDialogBtn = createDialogButton("تثبيت", dialogBtnParams) {
            // الوظيفة تتحدد عند العرض
        }

        bottomRowDialog.addView(closeDialogBtn)
        bottomRowDialog.addView(copyDialogBtn)
        bottomRowDialog.addView(pinDialogBtn)
        bottomRowDialog.addView(deleteDialogBtn)

        dialog = LinearLayout(context).apply {
            isClickable = true // لمنع النقر على القائمة خلفه
            setBackgroundColor(0xFF222222.toInt())
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )

            addView(textViewDialog)
            addView(bottomRowDialog)
        }

        // إضافة الديالوج لحاوية الوسط (فوق القائمة)
        centerContainer.addView(dialog)

        // 6. إعداد أزرار الشريط العلوي
        val btnParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)

        closeBtn = createTopButton("\u2716", btnParams) {
            // المنطق المطلوب: إذا الديالوج مفتوح أغلقه، وإلا أغلق الحافظة
            if (dialog.visibility == View.VISIBLE) {
                hideDialog()
            } 
            OwnboardIME.ime.toggleClipboard()
            
        }

        goToTopBtn = createTopButton("\u25b2", btnParams) {
            recyclerView.smoothScrollToPosition(0)
        }
        goToDownBtn = createTopButton("\u25bc", btnParams) {
            if (adapter.itemCount > 0) recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
        }
        goToSettingsBtn = createTopButton("\u2699", btnParams) {
            // هنا يمكنك فتح الإعدادات أو فعل شيء آخر، حالياً تغلق الحافظة كما في كودك الأصلي
             OwnboardIME.ime.toggleClipboard()
        }

        topRow.addView(goToSettingsBtn)
        topRow.addView(goToDownBtn)
        topRow.addView(goToTopBtn)
        topRow.addView(closeBtn)

        // تجميع التخطيط الرئيسي
        mainLayout.addView(topRow)
        mainLayout.addView(centerContainer)
    }

    private fun createTopButton(txt: String, params: LinearLayout.LayoutParams, onClick: () -> Unit): Button {
        return Button(context).apply {
            setTextColor(Color.WHITE)
            background = null
            text = txt
            textSize = 18f
            layoutParams = params
            setOnClickListener { onClick() }
        }
    }

    private fun createDialogButton(txt: String, params: LinearLayout.LayoutParams, onClick: () -> Unit): Button {
        return Button(context).apply {
            text = txt
            setTextColor(Color.WHITE)
            setBackgroundColor(0xFF444444.toInt())
            layoutParams = params
            setOnClickListener { onClick() }
        }
    }

    fun refresh() {
        val items = clipboardDB.getClipboardItems()
        adapter.updateList(items)
    }

    private fun showDialog(text: String, isPinned: Boolean) {
        textViewDialog.text = text
        pinDialogBtn.text = if (isPinned) "الغاء التثبيت" else "تثبيت"
        pinDialogBtn.setOnClickListener {
            clipboardDB.setPinned(text, !isPinned)
            refresh()
            hideDialog()
        }
        dialog.visibility = View.VISIBLE
    }

    fun hideDialog() {
        dialog.visibility = View.GONE
    }

    private fun onPaste(text: String) {
        OwnboardIME.ime.sendKeyPress(text)
        // التحقق من المتغير الجديد للإغلاق
        if (closeClipboardAfterPaste) {
            OwnboardIME.ime.toggleClipboard()
        }
    }

    override fun setVisibility(v: Int) {
        super.setVisibility(v)
        if (v == View.VISIBLE) {
            refresh()
            // تأكد من إخفاء الديالوج عند فتح الحافظة من جديد
            hideDialog()
        }
    }

    fun addClip(text: String) {
        if (text.isBlank()) return

        clipboardDB.addClip(text)

        if (visibility == View.VISIBLE) {
            refresh()
        }
    }

    fun dpToPx(dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }
}