package com.ownboard.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class LayoutDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "keyboard_layouts.db"
        private const val DATABASE_VERSION = 2
        private const val TABLE_NAME = "layouts"
        private const val COL_LANG = "lang"
        private const val COL_JSON = "json_data"

        // ==========================================
        // البيانات الافتراضية (تم دمج الـ JSON الكامل هنا)
        // ==========================================

        // داخل LayoutDatabase.kt

        private const val DEFAULT_AR_JSON = """
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

        private const val DEFAULT_EN_JSON = """
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
      { "weight": 1.0, "text": "1", "hint": "!", "click": "sendText", "longPress": "showPopup", "textToSend": "1" },
      { "weight": 1.0, "text": "2", "hint": "@", "click": "sendText", "longPress": "showPopup", "textToSend": "2" },
      { "weight": 1.0, "text": "3", "hint": "#", "click": "sendText", "longPress": "showPopup", "textToSend": "3" },
      { "weight": 1.0, "text": "4", "hint": "$", "click": "sendText", "longPress": "showPopup", "textToSend": "4" },
      { "weight": 1.0, "text": "5", "hint": "%", "click": "sendText", "longPress": "showPopup", "textToSend": "5" },
      { "weight": 1.0, "text": "6", "hint": "^", "click": "sendText", "longPress": "showPopup", "textToSend": "6" },
      { "weight": 1.0, "text": "7", "hint": "&", "click": "sendText", "longPress": "showPopup", "textToSend": "7" },
      { "weight": 1.0, "text": "8", "hint": "*", "click": "sendText", "longPress": "showPopup", "textToSend": "8" },
      { "weight": 1.0, "text": "9", "hint": "(", "click": "sendText", "longPress": "showPopup", "textToSend": "9" },
      { "weight": 1.0, "text": "0", "hint": ")", "click": "sendText", "longPress": "showPopup", "textToSend": "0" }
    ]
  },
  "row3": {
    "height": 55.0,
    "keys": [
      { "weight": 1.0, "text": "q", "hint": "( ) ()", "click": "sendText", "longPress": "showPopup", "textToSend": "q" },
      { "weight": 1.0, "text": "w", "hint": "{ } {}", "click": "sendText", "longPress": "showPopup", "textToSend": "w" },
      { "weight": 1.0, "text": "e", "hint": "[ ] []", "click": "sendText", "longPress": "showPopup", "textToSend": "e" },
      { "weight": 1.0, "text": "r", "hint": "& &&", "click": "sendText", "longPress": "showPopup", "textToSend": "r" },
      { "weight": 1.0, "text": "t", "hint": "| ||", "click": "sendText", "longPress": "showPopup", "textToSend": "t" },
      { "weight": 1.0, "text": "y", "hint": "= == =>", "click": "sendText", "longPress": "showPopup", "textToSend": "y" },
      { "weight": 1.0, "text": "u", "hint": "+ ++ +=", "click": "sendText", "longPress": "showPopup", "textToSend": "u", "leftScroll": "sendText", "rightScroll": "sendText", "textToSendLeftScroll": "++", "textToSendRightScroll": "+=" },
      { "weight": 1.0, "text": "i", "hint": "- ->", "click": "sendText", "longPress": "showPopup", "textToSend": "i" },
      { "weight": 1.0, "text": "o", "hint": "$", "click": "sendText", "longPress": "showPopup", "textToSend": "o" },
      { "weight": 1.0, "text": "p", "hint": "#", "click": "sendText", "longPress": "showPopup", "textToSend": "p" }
    ]
  },
  "row4": {
    "height": 55.0,
    "keys": [
      { "weight": 1.0, "text": "a", "hint": "@ • @gmail.com", "click": "sendText", "longPress": "showPopup", "textToSend": "a" },
      { "weight": 1.0, "text": "s", "hint": "! !=", "click": "sendText", "longPress": "showPopup", "textToSend": "s" },
      { "weight": 1.0, "text": "d", "hint": "~", "click": "sendText", "longPress": "showPopup", "textToSend": "d" },
      { "weight": 1.0, "text": "f", "hint": "?", "click": "sendText", "longPress": "showPopup", "textToSend": "f" },
      { "weight": 1.0, "text": "g", "hint": "* **", "click": "sendText", "longPress": "showPopup", "textToSend": "g" },
      { "weight": 1.0, "text": "h", "hint": "%", "click": "sendText", "longPress": "showPopup", "textToSend": "h" },
      { "weight": 1.0, "text": "j", "hint": "_ __", "click": "sendText", "longPress": "showPopup", "textToSend": "j" },
      { "weight": 1.0, "text": "k", "hint": ":", "click": "sendText", "longPress": "showPopup", "textToSend": "k" },
      { "weight": 1.0, "text": "l", "hint": ";", "click": "sendText", "longPress": "showPopup", "textToSend": "l" }
    ]
  },
  "row5": {
    "height": 55.0,
    "keys": [
      { "weight": 1.5, "text": "⇧", "click": "sendSpecial", "codeToSendClick": 115 },
      { "weight": 1.0, "text": "z", "hint": "' ''", "click": "sendText", "longPress": "showPopup", "textToSend": "z" },
      { "weight": 1.0, "text": "x", "hint": "\" \"\"", "click": "sendText", "longPress": "showPopup", "textToSend": "x" },
      { "weight": 1.0, "text": "c", "hint": "`", "click": "sendText", "longPress": "showPopup", "textToSend": "c" },
      { "weight": 1.0, "text": "v", "hint": "< <= <>", "click": "sendText", "longPress": "showPopup", "textToSend": "v" },
      { "weight": 1.0, "text": "b", "hint": "> >= </>", "click": "sendText", "longPress": "showPopup", "textToSend": "b" },
      { "weight": 1.0, "text": "n", "hint": "/ // /**/", "click": "sendText", "longPress": "showPopup", "textToSend": "n" },
      { "weight": 1.0, "text": "m", "hint": "\\", "click": "sendText", "longPress": "showPopup", "textToSend": "m" },
      { "weight": 1.5, "text": "⌫", "click": "delete", "longPress": "loop" }
    ]
  },
  "row6": {
    "height": 60.0,
    "keys": [
      { "weight": 1.5, "text": "123", "click": "switchSymbols" },
      { "weight": 1.0, "text": "ar", "click": "openEmoji" },
      { "weight": 1.0, "text": ",", "click": "sendText", "textToSend": "," },
      { "weight": 3.0, "text": "English", "hint": "العربية", "click": "sendText", "textToSend": " ", "leftScroll": "switchLang", "rightScroll": "switchLang" },
      { "weight": 1.0, "text": ".", "click": "sendText", "textToSend": "." },
      { "weight": 1.0, "text": "📋", "click": "openClipboard" },
      { "weight": 1.5, "text": "⏎", "click": "sendCode", "codeToSendClick": 66 }
    ]
  }
}
    """
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = "CREATE TABLE $TABLE_NAME ($COL_LANG TEXT PRIMARY KEY, $COL_JSON TEXT)"
        db.execSQL(createTable)

        insertDefaultLayout(db, "ar", DEFAULT_AR_JSON)
        insertDefaultLayout(db, "en", DEFAULT_EN_JSON)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    private fun insertDefaultLayout(db: SQLiteDatabase, lang: String, json: String) {
        val values = ContentValues().apply {
            put(COL_LANG, lang)
            put(COL_JSON, json)
        }
        db.insert(TABLE_NAME, null, values)
    }

    fun getLayoutByLang(lang: String): String {
        val db = this.readableDatabase
        val cursor = db.query(TABLE_NAME, arrayOf(COL_JSON), "$COL_LANG = ?", arrayOf(lang), null, null, null)
        var result = ""
        if (cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndexOrThrow(COL_JSON))
        }
        cursor.close()
        return result
    }
    
    fun updateLayout(lang: String, json: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_LANG, lang)
            put(COL_JSON, json)
        }
        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }
}