package com.ownboard.app.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class RecentEmojiDB(context: Context) : SQLiteOpenHelper(context, "recent_emojis.db", null, 3) {

    companion object {
        const val TABLE_NAME = "recents"
        const val COL_EMOJI = "emoji"
        const val COL_COUNT = "usage_count"
        const val COL_LAST_USED = "last_used"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $TABLE_NAME ($COL_EMOJI TEXT PRIMARY KEY, $COL_COUNT INTEGER DEFAULT 1, $COL_LAST_USED INTEGER)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addEmoji(emoji: String) {
        val db = this.writableDatabase
        val time = System.currentTimeMillis()
        val values = ContentValues().apply {
            put(COL_EMOJI, emoji)
            put(COL_COUNT, 1)
            put(COL_LAST_USED, time)
        }
        db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    // === دالة الحذف الجديدة ===
    fun deleteEmoji(emoji: String) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "$COL_EMOJI = ?", arrayOf(emoji))
    }

    fun getRecentEmojis(limit: Int = 60): List<String> {
        val list = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COL_EMOJI FROM $TABLE_NAME ORDER BY $COL_LAST_USED DESC LIMIT $limit", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }
}