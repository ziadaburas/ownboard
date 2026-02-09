package com.ownboard.app.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class LayoutDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "keyboard_layouts.db"
        private const val DATABASE_VERSION = 6 // تحديث الإصدار
        private const val TABLE_NAME = "layouts"
        private const val COL_LANG = "lang"
        private const val COL_JSON = "json_data"

        // ==========================================
        // تخطيط الرموز (Symbols) - مطابق للصورة
        // ==========================================
        private const val DEFAULT_SYMBOLS_JSON = """
    {
      "row1": {
        "height": 45.0,
        "keys": [
          { "weight": 1.0, "text": "←", "click": "sendCode", "longPress": "loop", "params": { "code": 21 } },
          { "weight": 1.0, "text": "↑", "hint": "Home", "click": "sendCode", "longPress": "sendCode", "verticalSwipe": "sendCode", "params": { "code": 19, "lpCode": 122, "vCode": 122 } },
          { "weight": 1.0, "text": "⇥", "click": "sendCode", "params": { "code": 61 } },
          { "weight": 1.0, "text": "Ctrl", "click": "sendSpecial", "params": { "code": 113 } }, 
          { "weight": 1.0, "text": "Alt", "click": "sendSpecial", "params": { "code": 57 } },
          { "weight": 1.0, "text": "Shift", "click": "sendSpecial", "params": { "code": 59 } },
          { "weight": 1.0, "text": "↓", "hint": "End", "click": "sendCode", "longPress": "sendCode", "verticalSwipe": "sendCode", "params": { "code": 20, "lpCode": 123, "vCode": 123 } },
          { "weight": 1.0, "text": "→", "click": "sendCode", "longPress": "loop", "params": { "code": 22 } }
        ]
      },
      "row2": {
        "height": 55.0,
        "keys": [
          { "weight": 1.0, "text": "+", "hint": "+++ =", "click": "sendText", "longPress": "showPopup", "params": { "text": "+", "popup": "+++ =" } },
          { "weight": 1.0, "text": "1", "click": "sendText", "params": { "text": "1" } },
          { "weight": 1.0, "text": "2", "click": "sendText", "params": { "text": "2" } },
          { "weight": 1.0, "text": "3", "click": "sendText", "params": { "text": "3" } },
          { "weight": 1.0, "text": "/", "hint": "()", "click": "sendText", "longPress": "showPopup", "params": { "text": "/", "popup": "()" } }
        ]
      },
      "row3": {
        "height": 55.0,
        "keys": [
          { "weight": 1.0, "text": "-", "hint": "_", "click": "sendText", "longPress": "showPopup", "params": { "text": "-", "popup": "_" } },
          { "weight": 1.0, "text": "4", "click": "sendText", "params": { "text": "4" } },
          { "weight": 1.0, "text": "5", "click": "sendText", "params": { "text": "5" } },
          { "weight": 1.0, "text": "6", "click": "sendText", "params": { "text": "6" } },
          { "weight": 1.0, "text": "\\", "hint": "{}", "click": "sendText", "longPress": "showPopup", "params": { "text": "\\", "popup": "{}" } }
        ]
      },
      "row4": {
        "height": 55.0,
        "keys": [
          { "weight": 1.0, "text": "!", "hint": "@", "click": "sendText", "longPress": "showPopup", "params": { "text": "!", "popup": "@" } },
          { "weight": 1.0, "text": "7", "click": "sendText", "params": { "text": "7" } },
          { "weight": 1.0, "text": "8", "click": "sendText", "params": { "text": "8" } },
          { "weight": 1.0, "text": "9", "click": "sendText", "params": { "text": "9" } },
          { "weight": 1.0, "text": "%", "hint": "[]", "click": "sendText", "longPress": "showPopup", "params": { "text": "%", "popup": "[]" } }
        ]
      },
      "row5": {
        "height": 55.0,
        "keys": [
          { "weight": 1.0, "text": "$", "hint": "?", "click": "sendText", "longPress": "showPopup", "params": { "text": "$", "popup": "?" } },
          { "weight": 1.0, "text": "*", "click": "sendText", "params": { "text": "*" } },
          { "weight": 1.0, "text": "0", "hint": "+", "click": "sendText", "longPress": "showPopup", "params": { "text": "0", "popup": "+" } },
          { "weight": 1.0, "text": "#", "click": "sendText", "params": { "text": "#" } },
          { "weight": 1.0, "text": "⌫", "click": "delete", "longPress": "loop", "horizontalSwipe": "delete", "params": {} }
        ]
      },
      "row6": {
        "height": 60.0,
        "keys": [
          { "weight": 1.5, "text": "abc", "click": "switchSymbols", "params": {} },
          { "weight": 1.0, "text": "😁", "click": "openEmoji", "params": {} },
          { "weight": 1.0, "text": ",", "click": "sendText", "params": { "text": "," } },
          { "weight": 3.0, "text": "العربية", "hint": "English", "click": "sendText", "horizontalSwipe": "switchLang", "params": { "text": " " } },
          { "weight": 1.0, "text": ".", "click": "sendText", "params": { "text": "." } },
          { "weight": 1.0, "text": "📋", "click": "openClipboard", "params": {} },
          { "weight": 1.5, "text": "⏎", "click": "sendCode", "params": { "code": 66 } }
        ]
      }
    }
    """

        // ==========================================
        // العربية (AR) - تم توحيد الـ Popups مع الإنجليزية
        // ==========================================
        private const val DEFAULT_AR_JSON = """
    {
      "row1": {
        "height": 45.0,
        "keys": [
          { "weight": 1.0, "text": "←", "click": "sendCode", "longPress": "loop", "params": { "code": 21 } },
          { "weight": 1.0, "text": "↑", "hint": "Home", "click": "sendCode", "longPress": "sendCode", "verticalSwipe": "sendCode", "params": { "code": 19, "lpCode": 122, "vCode": 122 } },
          { "weight": 1.0, "text": "⇥", "click": "sendCode", "params": { "code": 61 } },
          { "weight": 1.0, "text": "Ctrl", "click": "sendSpecial", "params": { "code": 113 } }, 
          { "weight": 1.0, "text": "Alt", "click": "sendSpecial", "params": { "code": 57 } },
          { "weight": 1.0, "text": "Shift", "click": "sendSpecial", "params": { "code": 59 } },
          { "weight": 1.0, "text": "↓", "hint": "End", "click": "sendCode", "longPress": "sendCode", "verticalSwipe": "sendCode", "params": { "code": 20, "lpCode": 123, "vCode": 123 } },
          { "weight": 1.0, "text": "→", "click": "sendCode", "longPress": "loop", "params": { "code": 22 } }
        ]
      },
      "row2": {
        "height": 55.0,
        "keys": [
          { "weight": 1.0, "text": "1", "hint": "!", "click": "sendText", "longPress": "showPopup", "verticalSwipe": "sendText", "params": { "text": "1", "vText": "!", "popup": "!" } },
          { "weight": 1.0, "text": "2", "hint": "@", "click": "sendText", "longPress": "showPopup", "params": { "text": "2", "popup": "@" } },
          { "weight": 1.0, "text": "3", "hint": "#", "click": "sendText", "longPress": "showPopup", "params": { "text": "3", "popup": "#" } },
          { "weight": 1.0, "text": "4", "hint": "$", "click": "sendText", "longPress": "showPopup", "params": { "text": "4", "popup": "$" } },
          { "weight": 1.0, "text": "5", "hint": "%", "click": "sendText", "longPress": "showPopup", "params": { "text": "5", "popup": "%" } },
          { "weight": 1.0, "text": "6", "hint": "^", "click": "sendText", "longPress": "showPopup", "horizontalSwipe": "switchLang", "params": { "text": "6", "popup": "^" } },
          { "weight": 1.0, "text": "7", "hint": "&", "click": "sendText", "longPress": "showPopup", "horizontalSwipe": "sendText", "params": { "text": "7", "hText": "cc", "popup": "&" } },
          { "weight": 1.0, "text": "8", "hint": "*", "click": "sendText", "longPress": "showPopup", "params": { "text": "8", "popup": "*" } },
          { "weight": 1.0, "text": "9", "hint": "(", "click": "sendText", "longPress": "showPopup", "params": { "text": "9", "popup": "(" } },
          { "weight": 1.0, "text": "0", "hint": ")", "click": "sendText", "longPress": "showPopup", "params": { "text": "0", "popup": ")" } }
        ]
      },
      "row3": {
        "height": 55.0,
        "keys": [
          { "weight": 1.0, "text": "ض", "hint": "( )", "click": "sendText", "longPress": "showPopup", "params": { "text": "ض", "popup": "( ) ()" } },
          { "weight": 1.0, "text": "ص", "hint": "{ }", "click": "sendText", "longPress": "showPopup", "params": { "text": "ص", "popup": "{ } {}" } },
          { "weight": 1.0, "text": "ق", "hint": "[ ]", "click": "sendText", "longPress": "showPopup", "params": { "text": "ق", "popup": "[ ] []" } },
          { "weight": 1.0, "text": "ف", "hint": "&", "click": "sendText", "longPress": "showPopup", "params": { "text": "ف", "popup": "& &&" } },
          { "weight": 1.0, "text": "غ", "hint": "|", "click": "sendText", "longPress": "showPopup", "params": { "text": "غ", "popup": "| ||" } },
          { "weight": 1.0, "text": "ع", "hint": "=", "click": "sendText", "longPress": "showPopup", "params": { "text": "ع", "popup": "= == =>" } },
          { "weight": 1.0, "text": "ه", "hint": "+", "click": "sendText", "longPress": "showPopup", "params": { "text": "ه", "popup": "+ ++ +=" } },
          { "weight": 1.0, "text": "خ", "hint": "-", "click": "sendText", "longPress": "showPopup", "params": { "text": "خ", "popup": "- ->" } },
          { "weight": 1.0, "text": "ح", "hint": "$", "click": "sendText", "longPress": "showPopup", "params": { "text": "ح", "popup": "$" } },
          { "weight": 1.0, "text": "ج", "hint": "#", "click": "sendText", "longPress": "showPopup", "params": { "text": "ج", "popup": "#" } }
        ]
      },
      "row4": {
        "height": 55.0,
        "keys": [
          { "weight": 1.0, "text": "ش", "hint": "@", "click": "sendText", "longPress": "showPopup", "params": { "text": "ش", "popup": "@ • @gmail.com" } },
          { "weight": 1.0, "text": "س", "hint": "!", "click": "sendText", "longPress": "showPopup", "params": { "text": "س", "popup": "! !=" } },
          { "weight": 1.0, "text": "ي", "hint": "~", "click": "sendText", "longPress": "showPopup", "params": { "text": "ي", "popup": "~" } },
          { "weight": 1.0, "text": "ب", "hint": "?", "click": "sendText", "longPress": "showPopup", "params": { "text": "ب", "popup": "?" } },
          { "weight": 1.0, "text": "ل", "hint": "*", "click": "sendText", "longPress": "showPopup", "params": { "text": "ل", "popup": "* **" } },
          { "weight": 1.0, "text": "ا", "hint": "%", "click": "sendText", "longPress": "showPopup", "params": { "text": "ا", "popup": "%" } },
          { "weight": 1.0, "text": "ت", "hint": "_", "click": "sendText", "longPress": "showPopup", "params": { "text": "ت", "popup": "_ __" } },
          { "weight": 1.0, "text": "ن", "hint": ":", "click": "sendText", "longPress": "showPopup", "params": { "text": "ن", "popup": ":" } },
          { "weight": 1.0, "text": "م", "hint": ";", "click": "sendText", "longPress": "showPopup", "params": { "text": "م", "popup": ";" } },
          { "weight": 1.0, "text": "ك", "hint": "!", "click": "sendText", "longPress": "showPopup", "params": { "text": "ك", "popup": "!" } }
        ]
      },
      "row5": {
        "height": 55.0,
        "keys": [
          { "weight": 1.0, "text": "ظ", "hint": "'", "click": "sendText", "longPress": "showPopup", "params": { "text": "ظ", "popup": "' ''" } },
          { "weight": 1.0, "text": "ط", "hint": "\"", "click": "sendText", "longPress": "showPopup", "params": { "text": "ط", "popup": "\" \"\"" } },
          { "weight": 1.0, "text": "ذ", "hint": "`", "click": "sendText", "longPress": "showPopup", "params": { "text": "ذ", "popup": "`" } },
          { "weight": 1.0, "text": "د", "hint": "<", "click": "sendText", "longPress": "showPopup", "params": { "text": "د", "popup": "< <= <>" } },
          { "weight": 1.0, "text": "ز", "hint": ">", "click": "sendText", "longPress": "showPopup", "params": { "text": "ز", "popup": "> >= </>" } },
          { "weight": 1.0, "text": "ر", "hint": "/", "click": "sendText", "longPress": "showPopup", "params": { "text": "ر", "popup": "/ // /**/" } },
          { "weight": 1.0, "text": "و", "hint": "\\", "click": "sendText", "longPress": "showPopup", "params": { "text": "و", "popup": "\\" } },
          { "weight": 1.0, "text": "ة", "hint": "!", "click": "sendText", "longPress": "showPopup", "params": { "text": "ة", "popup": "!" } },
          { "weight": 1.0, "text": "ث", "hint": "!", "click": "sendText", "longPress": "showPopup", "params": { "text": "ث", "popup": "!" } },
          { "weight": 1.5, "text": "⌫", "click": "delete", "longPress": "loop", "horizontalSwipe": "delete", "params": {} }
        ]
      },
      "row6": {
        "height": 60.0,
        "keys": [
          { "weight": 1.5, "text": "123", "click": "switchSymbols", "params": {} },
          { "weight": 1.0, "text": "😁", "click": "openEmoji", "params": {} },
          { "weight": 1.0, "text": "،", "click": "sendText", "params": { "text": "،" } },
          { "weight": 3.0, "text": "العربية", "hint": "English", "click": "sendText", "horizontalSwipe": "switchLang", "params": { "text": " " } },
          { "weight": 1.0, "text": ".", "click": "sendText", "params": { "text": "." } },
          { "weight": 1.0, "text": "📋", "click": "openClipboard", "params": {} },
          { "weight": 1.5, "text": "⏎", "click": "sendCode", "params": { "code": 66 } }
        ]
      }
    }
    """

        private const val DEFAULT_EN_JSON = """
{
  "row1": {
    "height": 45.0,
    "keys": [
      { "weight": 1.0, "text": "←", "click": "sendCode", "longPress": "loop", "params": { "code": 21 } },
      { "weight": 1.0, "text": "↑", "hint": "Home", "click": "sendCode", "longPress": "sendCode", "verticalSwipe": "sendCode", "params": { "code": 19, "lpCode": 122, "vCode": 122 } },
      { "weight": 1.0, "text": "⇥", "click": "sendCode", "params": { "code": 61 } },
      { "weight": 1.0, "text": "Ctrl", "click": "sendSpecial", "params": { "code": 113 } },
      { "weight": 1.0, "text": "Alt", "click": "sendSpecial", "params": { "code": 57 } },
      { "weight": 1.0, "text": "Shift", "click": "sendSpecial", "params": { "code": 59 } },
      { "weight": 1.0, "text": "↓", "hint": "End", "click": "sendCode", "longPress": "sendCode", "verticalSwipe": "sendCode", "params": { "code": 20, "lpCode": 123, "vCode": 123 } },
      { "weight": 1.0, "text": "→", "click": "sendCode", "longPress": "loop", "params": { "code": 22 } }
    ]
  },
  "row2": {
    "height": 55.0,
    "keys": [
      { "weight": 1.0, "text": "1", "hint": "!", "click": "sendText", "longPress": "showPopup", "params": { "text": "1", "popup": "!" } },
      { "weight": 1.0, "text": "2", "hint": "@", "click": "sendText", "longPress": "showPopup", "params": { "text": "2", "popup": "@" } },
      { "weight": 1.0, "text": "3", "hint": "#", "click": "sendText", "longPress": "showPopup", "params": { "text": "3", "popup": "#" } },
      { "weight": 1.0, "text": "4", "hint": "$", "click": "sendText", "longPress": "showPopup", "params": { "text": "4", "popup": "$" } },
      { "weight": 1.0, "text": "5", "hint": "%", "click": "sendText", "longPress": "showPopup", "params": { "text": "5", "popup": "%" } },
      { "weight": 1.0, "text": "6", "hint": "^", "click": "sendText", "longPress": "showPopup", "params": { "text": "6", "popup": "^" } },
      { "weight": 1.0, "text": "7", "hint": "&", "click": "sendText", "longPress": "showPopup", "params": { "text": "7", "popup": "&" } },
      { "weight": 1.0, "text": "8", "hint": "*", "click": "sendText", "longPress": "showPopup", "params": { "text": "8", "popup": "*" } },
      { "weight": 1.0, "text": "9", "hint": "(", "click": "sendText", "longPress": "showPopup", "params": { "text": "9", "popup": "(" } },
      { "weight": 1.0, "text": "0", "hint": ")", "click": "sendText", "longPress": "showPopup", "params": { "text": "0", "popup": ")" } }
    ]
  },
  "row3": {
    "height": 55.0,
    "keys": [
      { "weight": 1.0, "text": "q", "hint": "( ) ()", "click": "sendText", "longPress": "showPopup", "params": { "text": "q", "popup": "( ) ()" } },
      { "weight": 1.0, "text": "w", "hint": "{ } {}", "click": "sendText", "longPress": "showPopup", "params": { "text": "w", "popup": "{ } {}" } },
      { "weight": 1.0, "text": "e", "hint": "[ ] []", "click": "sendText", "longPress": "showPopup", "params": { "text": "e", "popup": "[ ] []" } },
      { "weight": 1.0, "text": "r", "hint": "& &&", "click": "sendText", "longPress": "showPopup", "params": { "text": "r", "popup": "& &&" } },
      { "weight": 1.0, "text": "t", "hint": "| ||", "click": "sendText", "longPress": "showPopup", "params": { "text": "t", "popup": "| ||" } },
      { "weight": 1.0, "text": "y", "hint": "= == =>", "click": "sendText", "longPress": "showPopup", "params": { "text": "y", "popup": "= == =>" } },
      { "weight": 1.0, "text": "u", "hint": "+ ++ +=", "click": "sendText", "longPress": "showPopup", "horizontalSwipe": "sendText", "params": { "text": "u", "hText": "++", "popup": "+ ++ +=" } },
      { "weight": 1.0, "text": "i", "hint": "- ->", "click": "sendText", "longPress": "showPopup", "params": { "text": "i", "popup": "- ->" } },
      { "weight": 1.0, "text": "o", "hint": "$", "click": "sendText", "longPress": "showPopup", "params": { "text": "o", "popup": "$" } },
      { "weight": 1.0, "text": "p", "hint": "#", "click": "sendText", "longPress": "showPopup", "params": { "text": "p", "popup": "#" } }
    ]
  },
  "row4": {
    "height": 55.0,
    "keys": [
      { "weight": 1.0, "text": "a", "hint": "@ • @gmail.com", "click": "sendText", "longPress": "showPopup", "params": { "text": "a", "popup": "@ • @gmail.com" } },
      { "weight": 1.0, "text": "s", "hint": "! !=", "click": "sendText", "longPress": "showPopup", "params": { "text": "s", "popup": "! !=" } },
      { "weight": 1.0, "text": "d", "hint": "~", "click": "sendText", "longPress": "showPopup", "params": { "text": "d", "popup": "~" } },
      { "weight": 1.0, "text": "f", "hint": "?", "click": "sendText", "longPress": "showPopup", "params": { "text": "f", "popup": "?" } },
      { "weight": 1.0, "text": "g", "hint": "* **", "click": "sendText", "longPress": "showPopup", "params": { "text": "g", "popup": "* **" } },
      { "weight": 1.0, "text": "h", "hint": "%", "click": "sendText", "longPress": "showPopup", "params": { "text": "h", "popup": "%" } },
      { "weight": 1.0, "text": "j", "hint": "_ __", "click": "sendText", "longPress": "showPopup", "params": { "text": "j", "popup": "_ __" } },
      { "weight": 1.0, "text": "k", "hint": ":", "click": "sendText", "longPress": "showPopup", "params": { "text": "k", "popup": ":" } },
      { "weight": 1.0, "text": "l", "hint": ";", "click": "sendText", "longPress": "showPopup", "params": { "text": "l", "popup": ";" } }
    ]
  },
  "row5": {
    "height": 55.0,
    "keys": [
      { "weight": 1.5, "text": "⇧", "click": "sendSpecial", "verticalSwipe": "sendSpecial", "params": { "code": 115, "vCode": 115 } },
      { "weight": 1.0, "text": "z", "hint": "' ''", "click": "sendText", "longPress": "showPopup", "params": { "text": "z", "popup": "' ''" } },
      { "weight": 1.0, "text": "x", "hint": "\" \"\"", "click": "sendText", "longPress": "showPopup", "params": { "text": "x", "popup": "\" \"\"" } },
      { "weight": 1.0, "text": "c", "hint": "`", "click": "sendText", "longPress": "showPopup", "params": { "text": "c", "popup": "`" } },
      { "weight": 1.0, "text": "v", "hint": "< <= <>", "click": "sendText", "longPress": "showPopup", "params": { "text": "v", "popup": "< <= <>" } },
      { "weight": 1.0, "text": "b", "hint": "> >= </>", "click": "sendText", "longPress": "showPopup", "params": { "text": "b", "popup": "> >= </>" } },
      { "weight": 1.0, "text": "n", "hint": "/ // /**/", "click": "sendText", "longPress": "showPopup", "params": { "text": "n", "popup": "/ // /**/" } },
      { "weight": 1.0, "text": "m", "hint": "\\", "click": "sendText", "longPress": "showPopup", "params": { "text": "m", "popup": "\\" } },
      { "weight": 1.5, "text": "⌫", "click": "delete", "longPress": "loop", "horizontalSwipe": "delete", "params": {} }
    ]
  },
  "row6": {
    "height": 60.0,
    "keys": [
      { "weight": 1.5, "text": "123", "click": "switchSymbols", "params": {} },
      { "weight": 1.0, "text": "ar", "click": "openEmoji", "params": {} },
      { "weight": 1.0, "text": ",", "click": "sendText", "params": { "text": "," } },
      { "weight": 3.0, "text": "English", "hint": "العربية", "click": "sendText", "horizontalSwipe": "switchLang", "params": { "text": " " } },
      { "weight": 1.0, "text": ".", "click": "sendText", "params": { "text": "." } },
      { "weight": 1.0, "text": "📋", "click": "openClipboard", "params": {} },
      { "weight": 1.5, "text": "⏎", "click": "sendCode", "params": { "code": 66 } }
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
        insertDefaultLayout(db, "symbols", DEFAULT_SYMBOLS_JSON) // إضافة الرموز
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

    fun getAllLayouts(): List<Pair<String, String>> {
        val layouts = mutableListOf<Pair<String, String>>()
        val db = this.readableDatabase
        val cursor = db.query(TABLE_NAME, arrayOf(COL_LANG, COL_JSON), null, null, null, null, null)

        with(cursor) {
            while (moveToNext()) {
                val lang = getString(getColumnIndexOrThrow(COL_LANG))
                val jsonData = getString(getColumnIndexOrThrow(COL_JSON))
                layouts.add(Pair(lang, jsonData))
            }
        }
        cursor.close()
        return layouts
    }

    fun resetToDefaultLayouts() {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, null, null) 
        insertDefaultLayout(db, "ar", DEFAULT_AR_JSON)
        insertDefaultLayout(db, "en", DEFAULT_EN_JSON)
        insertDefaultLayout(db, "symbols", DEFAULT_SYMBOLS_JSON)
    }
    
    fun resetSingleLayoutToDefault(lang: String) {
        val defaultJson = when(lang) {
            "ar" -> DEFAULT_AR_JSON
            "en" -> DEFAULT_EN_JSON
            "symbols" -> DEFAULT_SYMBOLS_JSON
            else -> return 
        }
        updateLayout(lang, defaultJson)
    }
}