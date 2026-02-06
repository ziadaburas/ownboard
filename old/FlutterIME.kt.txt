package com.example.ime

import android.inputmethodservice.InputMethodService
import com.example.ime.keyData.*
import android.view.LayoutInflater
import android.view.View
import android.view.KeyEvent
import android.widget.TextView
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.example.ime.R
import com.example.ime.views.*
import com.example.ime.apps.AppsDB
import com.example.ime.clipboard.ClipboardItem
import io.flutter.plugin.common.MethodChannel
// import android.inputmethodservice.InputMethodService
// import android.view.LayoutInflater
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
// import io.flutter.plugin.common.MethodChannel
import android.content.Context
import android.util.Log

import android.graphics.Color

import android.view.ViewGroup
import android.widget.PopupWindow
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.ime.clipboard.ClipboardDbHelper
import android.text.TextUtils

import android.view.ViewGroup.LayoutParams
import android.view.inputmethod.InputConnection

import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.EditorInfo
import android.text.InputType

import android.widget.ScrollView
import android.widget.ImageView
import android.content.ClipboardManager
import android.content.ClipData
import android.os.Bundle
import android.app.AlertDialog
 import android.view.WindowManager
import android.view.Gravity
import kotlinx.coroutines.*
import com.example.ime.utils.KeyboardLayoutBuilder

class FlutterIME : InputMethodService() {
    companion object {
        lateinit var ime:FlutterIME
    }
    private var isUppercase = false
    private val letterButtons = mutableListOf<Button>()
    private lateinit var methodChannel: MethodChannel
    private lateinit var flutterEngine: FlutterEngine
    lateinit var inflater: LayoutInflater
    private lateinit var keyboardBuilder: KeyboardLayoutBuilder
    

    var keyboardHeight=1
    var currentLang="en"
    init{
        ime = this
    }

    lateinit var rootView: View
    val backTexts = listOf("<>","</>","/**/","\"\"","''","()","{}","[]")
    override fun onCreateInputView(): View {
       inflater = LayoutInflater.from(this)
       var lang = R.layout.keyboard_en
       if(currentLang=="ar") {
            lang = R.layout.keyboard_ar
        }
        var view = inflater.inflate(lang, null) as ViewGroup
        
        rootView= view
        keyboardBuilder = KeyboardLayoutBuilder(rootView.context)

        /*if(currentLang=="ar") {
            currentLang = "en"
            switchLang()
        }*/
        // lastCopy=""
        // clipboardView = view.findViewById(R.id.clipboard_container) as ViewGroup
        // val topBtn = clipboardView.findViewById<Button>(R.id.btnClipGoToTop)
        // val closeButton = view.findViewById<Button>(R.id.keyClip)
        // closeButton.setOnClickListener {
    //         var view1 = inflater.inflate(R.layout.keyboard_ar, null) 
    //         // var popup = inflater.inflate(R.layout.popup, null) 
    //         // rootView.removeAllViews()
    //         //  rootView.addView(popup)
    //         //  rootView.addView(view)
    //         // currentLang == "ar"
    //    // }
    //    setInputView(view1)
        //setInputView(rootView)
        // val newView =  inflater.inflate(R.layout.keyboard_ar, null)
        // val partToReplace = rootView.findViewById<View>(R.id.keyboard_root1)

        // val parent = partToReplace.parent as ViewGroup
        // val index = parent.indexOfChild(partToReplace)
        // parent.removeViewAt(index)
        // parent.addView(newView, index)
        // switchLang()
        // }


        // scrollContainer = clipboardView.findViewById(R.id.scroll_buttons_container)

        // // حدث التمرير لتحميل المزيد (تعديل ScrollView)
        // val scrollView = clipboardView.findViewById<ScrollView>(R.id.scroll_container)
        // topBtn.setOnClickListener {
        //     scrollView.fullScroll(View.FOCUS_UP)
        // }
    
        // scrollView.viewTreeObserver.addOnScrollChangedListener {
        //     val tview = scrollView.getChildAt(scrollView.childCount - 1)
        //     val diff = (tview.bottom - (scrollView.height + scrollView.scrollY))
        //     if (diff <= 200) { // قرب نهاية التمرير
        //         loadMoreTexts()
        //     }
        // }

        // val container = FrameLayout(this)
        // container.addView(keyboardView)
        // container.addView(clipboardView)

       
        // val numsIds = listOf(
        //     R.id.key1, R.id.key2, R.id.key3, R.id.key4, R.id.key5,
        //     R.id.key6, R.id.key7, R.id.key8, R.id.key9, R.id.key0,
        //      R.id.keySim,R.id.keyDot
        // )
       
        // numsIds.forEachIndexed { index, id ->
        //     LetterKey(this, view, id)
           
        // }

    //     RightKey(this,view).changeLang("en")
    //     EnterKey(this, view)        
    //     TabKey(this, view)

    //     SpaceKey(this, view,::changeLang)
    //     NumsKey(this, view,::changeLang)
    //     LeftKey(this,view)
    //     DownKey(this,view)
    //     UpKey(this,view)
    //     ClipboardKey(this,view,::showClipboard)
        
    //     SpecialKey(this, view, R.id.keyCtrl, KeyEvent.KEYCODE_CTRL_LEFT)
    //     SpecialKey(this, view, R.id.keyAlt, KeyEvent.KEYCODE_ALT_LEFT)
    //     SpecialKey(this, view, R.id.keyShift, KeyEvent.KEYCODE_SHIFT_LEFT)
        
    // dialogContainer = view.findViewById(R.id.customDialogContainer)

    // val btnYes = view.findViewById<Button>(R.id.btnClipMoreDelete)
    // val btnNo = view.findViewById<Button>(R.id.btnClipMoreCancel)

    // btnYes.setOnClickListener {
    //     // مثال: تنفيذ إجراء
    //     //showToast("تم الاختيار: نعم")
    //     hideDialog()
    // }

    // btnNo.setOnClickListener {
    //     // فقط إخفاء
    //     hideDialog()
    // }
     
        
        // for (id in ids) {
        //    LetterKey(this, view, id)
        // }
        // زر المسافة
        // view.findViewById<Button>(R.id.keyNums).setOnClickListener {
        //      //changeLangToAr(view)
        //     //  val newKeyboard = LayoutInflater.from(this).inflate(R.layout.ar, null)
        //     // setInputView(newKeyboard)
        //     val newView =  LayoutInflater.from(this).inflate(R.layout.ar, null)
        // val partToReplace = view.findViewById<View>(R.id.symbols)

        // // استبدل الجزء القديم بالجديد
        // val parent = partToReplace.parent as ViewGroup
        // val index = parent.indexOfChild(partToReplace)
        // parent.removeViewAt(index)
        // parent.addView(newView, index)
        // for (id in arIds) {
        //     // استبدال زر الحرف العربي
        //    LetterKey(this, view, id)
        // }
        // DeleteKey(this, view)
        //     }
            
    //.service=this
     //view.findViewById<LetterKeyLayout>(R.id.keyNums).popupContainer=view.findViewById<LinearLayout>(R.id.altPopupContainer)

            // val jsonString = assets.open("en.json").bufferedReader().use { it.readText() }
            // val keys1 = loadKeysFromJson(jsonString)      
            // for(id in keys1){
            //     val keyId = resources.getIdentifier(id.keyId, "id", view.context.packageName)
            //     val btn = view.findViewById<Button>(keyId)
            //     var parent = btn.parent as ViewGroup
            //     btn.text = id.text
            //     parent.visibility = View.VISIBLE
               
            //     (parent.getChildAt(1) as? TextView)?.text = id.popupKeys.joinToString("")
            // }
      
    //      view.findViewById<Button>(R.id.keyEmog).setOnClickListener {
    //         var emg =mutableListOf("\uD83E\uDD20")// mutableListOf("😀", "😁", "😂", "🤣", "😃", "😄", "😅", "😆", "😉", "😊")
    //         var i=1
    //         var a = 0x1F600
    //         while(i<200){
    //             var ch = CharArray(2)
    //             ch[0]= Char(0xD83E)
    //             ch[1]= Char(0xDD20+i)
                
    //             // emg.add(Char(i+a).toString())
    //              //"\u$i"
    //            emg.add(String(ch))
    //             i++
                
    //         }
    //         val emojis = listOf("😀", "😁", "😂", "🤣", "😃", "😄", "😅", "😆", "😉", "😊")
    //         addEmojisInRows(rootView, emg)
 
    //      }
    //       view.findViewById<Button>(R.id.btn_close_emog).setOnClickListener {

    //            val emojiPanel = rootView.findViewById<LinearLayout>(R.id.emoji_panel)
   
    // emojiPanel.visibility=View.GONE 
 
    //      }

        return view
    }




   private var lastPackageName: String? = null
     val appsdb : AppsDB by lazy {AppsDB(this)}
    override fun onStartInputView(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(attribute, restarting)
        if(!::rootView.isInitialized || attribute == null) return 
        
        // attribute.te
        lastPackageName = attribute.packageName

       // CoroutineScope(Dispatchers.IO).launch {
            // context from IME
            // val appLang = apps[attribute.packageName]
            val appLang = appsdb.getLang(attribute.packageName)

         //   withContext(Dispatchers.Main) {
                if(appLang != null && appLang != currentLang && ::rootView.isInitialized)
                switchLang() // changeLang(appLang)
                    // Key.currentLang = appLang
                    // attribute.let { info ->
                    //     val inputType = info.inputType
                    //     if ((inputType and InputType.TYPE_CLASS_NUMBER) == InputType.TYPE_CLASS_NUMBER) {
                    //         changeLang("numeric")
                    //     }else{
                    //         Key.isNumeric=false
                    //         changeLang(Key.currentLang)
                    //     }
                    // }
                // }
            // }       
    }
    override fun onFinishInput() {
        super.onFinishInput()
        // var appsdb = AppsDB(this)
        //currentInputConnection.commitText(lastPackageName, 1)
         saveLang()
       
    }
    private fun saveLang(){
        if(lastPackageName != null){
            val pk =lastPackageName.toString()
            //apps[pk]= currentLang
            appsdb.insertOrUpdate(pk,currentLang)
        }
    }
    private lateinit var clipboardDbHelper: ClipboardDbHelper
    private lateinit var clipboardView: ViewGroup

    private lateinit var scrollContainer: LinearLayout
    private lateinit var closeButton: Button

    private val PAGE_SIZE = 20
    private var currentOffset = 0
    private var allTexts: List<String> = emptyList()
    var clipboardItems :List<ClipboardItem> = emptyList()
    val clipboardListeners = mutableListOf<ClipboardManager.OnPrimaryClipChangedListener>()
    var clipboardListener: ClipboardManager.OnPrimaryClipChangedListener? = null
    var lastCopy:String=""
    var clipboardFlag = true
    var isChangeClipboard=true
    override fun onCreate() {
        super.onCreate()
        lastCopy=""
        clipboardDbHelper = ClipboardDbHelper(this)// as ClipboardManager
        val clipboard =  this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0 ) {
            val copiedText =clip.getItemAt(0).coerceToText(this).toString()
            if (copiedText.isNotBlank() && copiedText != lastCopy && clipboardFlag) {
                // حفظ النص المنسوخ في قاعدة البيانات
                lastCopy=copiedText
                isChangeClipboard=true
                clipboardDbHelper.insertText(copiedText)
            }
   
        }
}
       
       // clipboardListeners.forEach { listener ->
            clipboard.addPrimaryClipChangedListener( clipboardListener)
       // }
        
    
    }
    override fun onDestroy() {
        super.onDestroy()
        val clipboard =  this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        
        // clipboardListeners.forEach { listener ->
            clipboard.removePrimaryClipChangedListener( clipboardListener)
        //}
       // clipboardListeners.clear()
       // clipboard.removePrimaryClipChangedListener(clipboardListener?) // إزالة أي مستمع سابق
        //clipboardDbHelper.close() // إغلاق قاعدة البيانات عند تدمير الخدمة
    }
    private fun setButtonText(button: Button, text: String) {
        button.text = text
        button.maxLines = 1
        button.ellipsize = TextUtils.TruncateAt.END
        button.setOnClickListener {
            inputText(text)
        }

    }

    private fun inputText(text: String) {
        val ic: InputConnection? = currentInputConnection
        ic?.commitText(text, 1)
    }


   fun sendKeyPress(keyCode : Int) {
        if(keyCode == 0 || keyCode == 115)return
        val ic = currentInputConnection
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
    }
    
    fun sendKeyPress(keyCode : String) {
        val ic = currentInputConnection
        
        var keyCode1 = if ((Key.capslock.value ?: 1) != 0) keyCode.uppercase() else keyCode
        ic.commitText(keyCode1, 1)
        if((keyCode in backTexts)){
            
            val ext = ic.getExtractedText(ExtractedTextRequest(), 1)
            var mid = keyCode.length/2
            if(ext!=null){
                var back = ext.selectionStart-mid
                ic.setSelection(back,back)
            }else
            for(i in 1..mid)sendKeyPress(KeyEvent.KEYCODE_DPAD_LEFT)
            
        }
        
    }
    
    fun sendKeyDown(keyCode : Int) {
         if(keyCode == 0 || keyCode == 115)return
        val ic = currentInputConnection
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
    }

    fun sendKeyUp(keyCode : Int) {
         if(keyCode == 0 || keyCode == 115)return
        val ic = currentInputConnection
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
    }
    fun delete(){
        val ic = currentInputConnection
        val selected = getSelectedText()
        if(selected != "")ic.commitText("", 1)
        else ic.deleteSurroundingText(1, 0)
    }

    fun getSelectedText():String{
        val ic = currentInputConnection
        val selectedText = ic.getSelectedText(InputConnection.GET_TEXT_WITH_STYLES)
        if (selectedText != null && selectedText.isNotEmpty()) return selectedText.toString()
        return ""
    }
    private fun setKeyboardKeysProgrammatically(isArabic: Boolean) {
        val oldKeysView = rootView.findViewById<View>(R.id.keyboard_keys)
        
        if (oldKeysView != null) {
            val parent = oldKeysView.parent as? ViewGroup
            val index = parent?.indexOfChild(oldKeysView) ?: -1
            
            if (parent != null && index != -1) {
                // إنشاء الكيبورد الجديد
                val newKeysView = if (isArabic) {
                    keyboardBuilder.buildArabicKeyboard()
                } else {
                    keyboardBuilder.buildEnglishKeyboard()
                }
                
                // تعيين ID للعنصر الجديد
                newKeysView.id = R.id.keyboard_keys
                
                // استبدال العنصر القديم بالجديد
                parent.removeViewAt(index)
                parent.addView(newKeysView, index)
            }
        }
    }
    fun switchLang() {
        if (currentLang == "ar") {
            currentLang = "en"
            setKeyboardKeysProgrammatically(false) // false = English
        } else {
            currentLang = "ar"
            setKeyboardKeysProgrammatically(true) // true = Arabic
        }
        com.example.ime.views.Key.isSymbols.value = false
        saveLang()
    }

    fun switchLang1() {
        if (currentLang == "ar") {
            currentLang = "en"
            setKeyboardKeys(R.layout.k_en)
        } else {
            currentLang = "ar"
            setKeyboardKeys(R.layout.k_ar)
        }
        com.example.ime.views.Key.isSymbols.value = false
        saveLang()
    }
    fun switchSymbols(isSymbols: Boolean) {
        // val layoutId = when {
            //     isSymbols -> R.layout.keyboard_numeric
            //     currentLang == "ar" -> R.layout.keyboard_ar
            //     else -> R.layout.keyboard_en
            // }
            val keysLayoutId = when {
                isSymbols -> R.layout.k_nums
                currentLang == "ar" -> R.layout.k_ar
            else -> R.layout.k_en
        }
        setKeyboardKeys(keysLayoutId)
        Key.isSymbols.value = isSymbols
        // loadKeysFromAssetAndPopulate()
    }
    // fun setKeyboardKeys(layoutId: Int) {
    //     val newKeysView = LayoutInflater.from(rootView.context).inflate(layoutId, null)
    //     val oldKeysView = rootView.findViewById<View>(R.id.keyboard_keys)
    //     if (oldKeysView != null) {
    //         val parent = oldKeysView.parent as? ViewGroup
    //         val index = parent?.indexOfChild(oldKeysView) ?: -1
    //         if (parent != null && index != -1) {
    //             parent.removeViewAt(index)
    //             parent.addView(newKeysView, index)
    //         }
    //     }
    // }
    fun setKeyboardKeys(layoutId: Int) {
    val oldKeysView = rootView.findViewById<View>(R.id.keyboard_keys)
    if (oldKeysView != null) {
        val parent = oldKeysView.parent as? ViewGroup
        val index = parent?.indexOfChild(oldKeysView) ?: -1
        if (parent != null && index != -1) {
            // مرر الـ parent هنا مع attachToRoot = false
            val newKeysView = LayoutInflater.from(rootView.context)
                .inflate(layoutId, parent, false)

            parent.removeViewAt(index)
            parent.addView(newKeysView, index)
        }
    }
}

fun loadKeysFromAssetAndPopulate() {
    try {
        val jsonString = rootView.context.assets.open("en.json").bufferedReader().use { it.readText() }
        val gson = Gson()
        val type = object : TypeToken<List<KeyData>>() {}.type
        val jsonArray = org.json.JSONArray(jsonString)
        val container = rootView.findViewById<LinearLayout>(R.id.keyboard_keys)
        container.removeAllViews()
        for (i in 0 until jsonArray.length()) {
            val kStr = jsonArray.getString(i)
            val keys: List<KeyData> = gson.fromJson(kStr, type)
       
            var currentRow: LinearLayout? = null

            currentRow = LinearLayout(rootView.context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    -1,
                    0,1f
                )
            }
            container.addView(currentRow)
            for ((index, keyData) in keys.withIndex()) {
                when(keyData.type) {
                    "LoopKey" -> {
                        val normalView = LoopKey(rootView.context).apply {
                            text = keyData.text
                            keyCode = keyData.keyCode
                            hint = keyData.popupKeys
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                -1,
                                1f
                            )
                            clickFn = {functionMap[keyData.clickFn]?.invoke(keyData.clickParam) }
                        }
                        currentRow.addView(normalView)
                    }
                    "space" -> {
                        val spaceView = Space(rootView.context).apply {
                            text = keyData.text
                            hint = keyData.popupKeys
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                -1,
                                3f
                            )
                        }
                        currentRow.addView(spaceView)
                    }
                    "enter" -> {
                        val enterView = Enter(rootView.context).apply {
                            text = keyData.text
                            hint = keyData.popupKeys
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                -1,
                                1f
                            )
                        }
                        currentRow.addView(enterView)
                    }
                    "delete" -> {
                        val deleteView = Delete(rootView.context).apply {
                            text = keyData.text
                            hint = keyData.popupKeys
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                -1,
                                1f
                            )
                        }
                        currentRow.addView(deleteView)
                    }
                    "mode" -> {
                        val letterView = Letter1(rootView.context).apply {
                        text =keyData.text 
                        
                        hint = keyData.popupKeys
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            -1,
                            1.5f
                        )
                    }
                        currentRow.addView(letterView)
                    }
                else ->{
                    val letterView1 = Letter(rootView.context).apply {
                        text =keyData.text 
                        hint = keyData.popupKeys
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            -1,
                            1f
                        )
                    }
                    val letterView = Letter1(rootView.context).apply {
                        text =keyData.text 
                        popupBtns.add(letterView1)
                        hint = keyData.popupKeys
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            -1,
                            1f
                        )
                    }

                
                    currentRow.addView(letterView)
                }
            }
            }
        }
    } catch (e: Exception) {
        Log.e("FlutterIME", "Error loading keys from asset: ${e.message}")
    }
}
    
}
