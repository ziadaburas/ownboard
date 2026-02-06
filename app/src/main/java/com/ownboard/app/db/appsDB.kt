package com.example.ime.apps

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.database.Cursor

class AppsDB(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "apps.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_NAME = "apps"
        const val COLUMN_ID = "app"
        const val COLUMN_TEXT = "lang"
    }
   
    override fun onCreate(db: SQLiteDatabase) {
    val createTable = """
        CREATE TABLE $TABLE_NAME (
            $COLUMN_ID TEXT PRIMARY KEY,
            $COLUMN_TEXT TEXT NOT NULL
        );
    """.trimIndent()
    db.execSQL(createTable)
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }
fun insertOrUpdate(app: String, lang: String) {
    val db = this.writableDatabase
    val values = ContentValues().apply {
        put(COLUMN_ID, app)
        put(COLUMN_TEXT, lang)
    }

    // إدراج مع استبدال في حال التعارض (أي إذا كان التطبيق موجود)
    db.insertWithOnConflict(
        TABLE_NAME,
        null,
        values,
        SQLiteDatabase.CONFLICT_REPLACE
    )

    db.close()
}
fun getLang(app: String): String? {
    val db = this.readableDatabase
    val cursor = db.query(
        TABLE_NAME,
        arrayOf(COLUMN_TEXT),
        "$COLUMN_ID = ?",
        arrayOf(app),
        null, null, null
    )

    var lang: String? = null
    if (cursor.moveToFirst()) {
        lang = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEXT))
    }

    cursor.close()
    db.close()
    return lang
}

    
}