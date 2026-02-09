package com.ownboard.app.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.IOException

class LayoutDatabase(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "keyboard_layouts.db"
        // قمنا برفع الإصدار لمسح البيانات القديمة المتعارضة
        private const val DATABASE_VERSION = 10
        private const val TABLE_NAME = "layouts"
        private const val COL_LANG = "lang"
        private const val COL_JSON = "json_data"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = "CREATE TABLE $TABLE_NAME ($COL_LANG TEXT PRIMARY KEY, $COL_JSON TEXT)"
        db.execSQL(createTable)

        // تحميل الافتراضيات من الملفات
        insertDefaultLayout(db, "ar", loadJSONFromAsset("ar.json"))
        insertDefaultLayout(db, "en", loadJSONFromAsset("en.json"))
        insertDefaultLayout(db, "symbols", loadJSONFromAsset("symbols.json"))
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // دالة لقراءة الملفات من Assets
    private fun loadJSONFromAsset(filename: String): String {
        return try {
            val inputStream = context.assets.open("layouts/$filename")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            "[]" // إرجاع مصفوفة فارغة لتجنب الانهيار
        }
    }

    private fun insertDefaultLayout(db: SQLiteDatabase, lang: String, json: String) {
        if (json.isEmpty()) return
        val values = ContentValues().apply {
            put(COL_LANG, lang)
            put(COL_JSON, json)
        }
        db.insert(TABLE_NAME, null, values)
    }

    fun getLayoutByLang(lang: String): String {
        val db = this.readableDatabase
        val cursor = db.query(TABLE_NAME, arrayOf(COL_JSON), "$COL_LANG = ?", arrayOf(lang), null, null, null)
        var result = "[]" // افتراضي مصفوفة فارغة
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

    // الدالة التي كانت تسبب خطأ Unresolved reference
    fun getAllLayouts(): List<Pair<String, String>> {
        val layouts = mutableListOf<Pair<String, String>>()
        val db = this.readableDatabase
        val cursor = db.query(TABLE_NAME, arrayOf(COL_LANG, COL_JSON), null, null, null, null, null)

        if (cursor.moveToFirst()) {
            do {
                val lang = cursor.getString(cursor.getColumnIndexOrThrow(COL_LANG))
                val json = cursor.getString(cursor.getColumnIndexOrThrow(COL_JSON))
                layouts.add(Pair(lang, json))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return layouts
    }

    fun resetToDefaultLayouts() {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, null, null) 
        onCreate(db) 
    }
    
    // الدالة التي كانت تسبب خطأ Unresolved reference
    fun resetSingleLayoutToDefault(lang: String) {
        val fileName = when(lang) {
            "ar" -> "ar.json"
            "en" -> "en.json"
            "symbols" -> "symbols.json"
            else -> return 
        }
        val defaultJson = loadJSONFromAsset(fileName)
        updateLayout(lang, defaultJson)
    }
}