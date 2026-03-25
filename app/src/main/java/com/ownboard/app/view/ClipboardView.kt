package com.ownboard.app.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ownboard.app.OwnboardIME
import com.ownboard.app.db.ClipboardDbHelper
import com.ownboard.app.ui.ClipboardManageActivity
import com.ownboard.app.utils.SettingsManager
import com.ownboard.app.R
import kotlinx.coroutines.* // استيراد مكتبة Coroutines

class ClipboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    // متغير التحكم بالإغلاق
    var closeClipboardAfterPaste: Boolean = true
        get() = SettingsManager.getBoolean("closeClipboardAfterPaste", true)

    // نطاق العمليات الخلفية (Coroutine Scope)
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private lateinit var mainLayout: LinearLayout
    private lateinit var topRow: LinearLayout
    private lateinit var centerContainer: FrameLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ClipboardAdapter
    
    // عناصر الديالوج
    private lateinit var dialogLayout: LinearLayout // تم تغيير الاسم لتوضيح أنها الحاوية
    private lateinit var textViewDialog: TextView
    private lateinit var buttonsContainer: LinearLayout

    // الأزرار العلوية
    // الأزرار العلوية الجديدة
    lateinit var closeBtn: Button
    lateinit var selectAllBtn: Button 
    lateinit var copyBtn: Button      // زر النسخ
    lateinit var pasteBtn: Button     // زر اللصق
    lateinit var settingsBtn: Button
    var cutBtn: Button
    // أزرار الديالوج
    lateinit var closeDialogBtn: Button
    lateinit var copyDialogBtn: Button
    lateinit var deleteDialogBtn: Button
    lateinit var pinDialogBtn: Button

    val clipboardDB = ClipboardDbHelper(context)

    init {
        visibility = View.GONE
        setBackgroundColor(Color.parseColor("#222222"))

        // 1. التخطيط الرئيسي
        mainLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }
        addView(mainLayout)

        // 2. الشريط العلوي
        topRow = LinearLayout(context).apply {
            setBackgroundColor(0xFF121212.toInt())
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(40f).toInt()
            )
        }

        // 3. حاوية الوسط
        centerContainer = FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        // 4. القائمة
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
        centerContainer.addView(recyclerView)

        // 5. الديالوج
        textViewDialog = TextView(context).apply {
            text = ""
            setTextColor(Color.WHITE)
            textSize = 16f
            setBackgroundColor(0xFF2D2D2D.toInt())
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        }

        buttonsContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(50f).toInt())
        }

        dialogLayout = LinearLayout(context).apply {
            isClickable = true
            setBackgroundColor(0xFF222222.toInt())
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            addView(textViewDialog)
            addView(buttonsContainer)
        }
        centerContainer.addView(dialogLayout)

        // بناء أزرار الديالوج
        val dialogBtnParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
            setMargins(2, 2, 2, 2)
        }

        closeDialogBtn = createDialogButton("اغلاق", dialogBtnParams) { hideDialog() }
        
        copyDialogBtn = createDialogButton("نسخ", dialogBtnParams) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("copied", textViewDialog.text.toString())
            clipboard.setPrimaryClip(clip)
            hideDialog()
        }
        
        deleteDialogBtn = createDialogButton("حذف", dialogBtnParams) {
            val textToDelete = textViewDialog.text.toString()
            scope.launch {
                withContext(Dispatchers.IO) {
                    clipboardDB.deleteText(textToDelete)
                }
                refresh() // تحديث القائمة بعد الحذف
                hideDialog()
            }
        }
        
        pinDialogBtn = createDialogButton("تثبيت", dialogBtnParams) {
            // سيتم تعيين المستمع (Listener) عند فتح الديالوج لأن الحالة تتغير
        }

        buttonsContainer.addView(closeDialogBtn)
        buttonsContainer.addView(copyDialogBtn)
        buttonsContainer.addView(pinDialogBtn)
        buttonsContainer.addView(deleteDialogBtn)

        // 6. أزرار الشريط العلوي

        // 6. أزرار الشريط العلوي
        val btnParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)

        closeBtn = createTopButton("\u2716", btnParams) {
            if (dialogLayout.visibility == View.VISIBLE) {
                hideDialog()
            } else {
                OwnboardIME.ime.toggleClipboard()
            }
        }

        // زر تحديد الكل
        selectAllBtn = createTopButton("", btnParams) {
            OwnboardIME.ime.performContextMenuAction(android.R.id.selectAll)
        }.apply {
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_select_all, 0, 0, 0)
            gravity = Gravity.CENTER
        }

        // زر النسخ
        copyBtn = createTopButton("", btnParams) {
            OwnboardIME.ime.performContextMenuAction(android.R.id.copy)
        }.apply {
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_copy, 0, 0, 0)
            gravity = Gravity.CENTER
            
        }

        pasteBtn = createTopButton("", btnParams) { // نص فارغ
            OwnboardIME.ime.performContextMenuAction(android.R.id.paste)
        }.apply {
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_paste, 0, 0, 0)
            gravity = Gravity.CENTER
        }

        cutBtn = createTopButton("", btnParams) { // نص فارغ
            OwnboardIME.ime.performContextMenuAction(android.R.id.cut)
        }.apply {
            setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_cut, 0, 0, 0)
            gravity = Gravity.CENTER
        }

        settingsBtn = createTopButton("\u2699", btnParams) {
            try {
                val intent = Intent(context, ClipboardManageActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                OwnboardIME.ime.toggleClipboard()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // إضافة الأزرار إلى الشريط العلوي بالترتيب (من اليمين لليسار أو العكس حسب رغبتك)
        topRow.addView(settingsBtn)
        topRow.addView(pasteBtn)     // أضفنا زر اللصق
        topRow.addView(copyBtn)      // أضفنا زر النسخ
        topRow.addView(cutBtn) // أضفنا زر تحديد الكل
        topRow.addView(selectAllBtn) // أضفنا زر تحديد الكل
        topRow.addView(closeBtn)

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

    // --- الوظائف المحسنة (Async) ---

    fun refresh() {
        scope.launch {
            // 1. جلب البيانات في الخلفية
            val items = withContext(Dispatchers.IO) {
                clipboardDB.getClipboardItems()
            }
            // 2. تحديث الواجهة في الخيط الرئيسي
            adapter.updateList(items)
        }
    }

    fun addClip(text: String) {
        if (text.isBlank()) return

        scope.launch {
            // 1. الحفظ في الخلفية
            withContext(Dispatchers.IO) {
                clipboardDB.addClip(text)
            }
            // 2. تحديث القائمة إذا كانت ظاهرة
            if (visibility == View.VISIBLE) {
                refresh()
            }
        }
    }

    private fun showDialog(text: String, isPinned: Boolean) {
        textViewDialog.text = text
        pinDialogBtn.text = if (isPinned) "الغاء التثبيت" else "تثبيت"
        
        // تحديث وظيفة زر التثبيت لتكون غير متزامنة
        pinDialogBtn.setOnClickListener {
            scope.launch {
                withContext(Dispatchers.IO) {
                    clipboardDB.setPinned(text, !isPinned)
                }
                refresh()
                hideDialog()
            }
        }
        dialogLayout.visibility = View.VISIBLE
    }

    fun hideDialog() {
        dialogLayout.visibility = View.GONE
    }

    private fun onPaste(text: String) {
        OwnboardIME.ime.pasteFromHistory(text)
        if (closeClipboardAfterPaste) {
            OwnboardIME.ime.toggleClipboard()
        }
    }

    override fun setVisibility(v: Int) {
        super.setVisibility(v)
        if (v == View.VISIBLE) {
            refresh()
            hideDialog()
        }
    }

    // تنظيف العمليات الخلفية عند تدمير الـ View
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        scope.cancel()
    }

    fun dpToPx(dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }
}