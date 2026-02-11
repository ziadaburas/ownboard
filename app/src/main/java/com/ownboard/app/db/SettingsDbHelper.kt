package com.ownboard.app.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SettingsDbHelper(context: Context) : SQLiteOpenHelper(context, "Settings.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        // تم تغيير اسم العمود إلى jsonData
        db.execSQL("CREATE TABLE settings (id INTEGER PRIMARY KEY, jsonData TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS settings")
        onCreate(db)
    }

    fun saveSettingsJson(jsonString: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", 1)
            put("jsonData", jsonString) // camelCase
        }
        db.insertWithOnConflict("settings", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
    }

    fun getSettingsJson(): String? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT jsonData FROM settings WHERE id = 1", null)
        var result: String? = null
        if (cursor.moveToFirst()) {
            result = cursor.getString(0)
        }
        cursor.close()
        db.close()
        return result
    }
}