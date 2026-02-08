package com.ownboard.app.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppLanguageDbHelper(context: Context) : SQLiteOpenHelper(context, "app_settings.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        // جدول يحفظ اسم الحزمة (التطبيق) واللغة
        // package_name هو المفتاح الرئيسي لضمان عدم تكرار التطبيق
        db.execSQL("CREATE TABLE app_lang (package_name TEXT PRIMARY KEY, language TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS app_lang")
        onCreate(db)
    }

    // دالة لحفظ أو تحديث لغة التطبيق
    fun setAppLanguage(packageName: String, language: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("package_name", packageName)
            put("language", language)
        }
        // CONFLICT_REPLACE تعني: إذا كان التطبيق موجوداً سابقاً، قم بتحديث اللغة فقط
        db.insertWithOnConflict("app_lang", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    // دالة لجلب لغة التطبيق (الافتراضي ar)
    fun getAppLanguage(packageName: String): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT language FROM app_lang WHERE package_name = ?", arrayOf(packageName))
        var lang = "ar" // اللغة الافتراضية اذا لم يكن محفوظاً
        if (cursor.moveToFirst()) {
            lang = cursor.getString(0)
        }
        cursor.close()
        return lang
    }
}