package com.ownboard.app.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class ClipboardItem(
    val id: Long,
    val text: String,
    val isPinned: Boolean,
    val timestamp: Long
)

class ClipboardDbHelper(context: Context) : SQLiteOpenHelper(context, "clipboard.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE clipboard (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "text TEXT UNIQUE, " +
                    "is_pinned INTEGER DEFAULT 0, " +
                    "timestamp INTEGER)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS clipboard")
        onCreate(db)
    }

    fun addClip(text: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("text", text)
            put("timestamp", System.currentTimeMillis())
        }
        val id = db.insertWithOnConflict("clipboard", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        if (id == -1L) { // إذا كان موجوداً، نحدث الوقت فقط
             db.update("clipboard", values, "text = ?", arrayOf(text))
        }
    }

    fun deleteText(text: String) {
        writableDatabase.delete("clipboard", "text = ?", arrayOf(text))
    }

    fun setPinned(text: String, isPinned: Boolean) {
        val values = ContentValues().apply {
            put("is_pinned", if (isPinned) 1 else 0)
        }
        writableDatabase.update("clipboard", values, "text = ?", arrayOf(text))
    }

    fun getClipboardItems(): List<ClipboardItem> {
        val list = ArrayList<ClipboardItem>()
        val db = readableDatabase
        // الترتيب: المثبت أولاً، ثم الأحدث
        val cursor = db.rawQuery(
            "SELECT * FROM clipboard ORDER BY is_pinned DESC, timestamp DESC LIMIT 100", 
            null
        )
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val txt = cursor.getString(cursor.getColumnIndexOrThrow("text"))
                val pinned = cursor.getInt(cursor.getColumnIndexOrThrow("is_pinned")) == 1
                val time = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"))
                list.add(ClipboardItem(id, txt, pinned, time))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }
}