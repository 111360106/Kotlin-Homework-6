package com.example.lab16_1

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
// 自訂建構子並繼承 SQLiteOpenHelper 類別
class MyDBHelper(
    context: Context,
    name: String = DB_NAME,
    factory: SQLiteDatabase.CursorFactory? = null,
    version: Int = VERSION
) : SQLiteOpenHelper(context, name, factory, version) {

    companion object {
        private const val DB_NAME = "myDatabase" // 資料庫名稱
        private const val VERSION = 2 // 資料庫版本（已升級）
    }
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE myTable(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " + // 新增 id 欄位作為主鍵
                    "book TEXT NOT NULL, " +
                    "price INTEGER NOT NULL)"
        )
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // 如果升級到版本 2，進行資料表結構的升級
            db.execSQL("ALTER TABLE myTable ADD COLUMN id INTEGER PRIMARY KEY AUTOINCREMENT")
        }
    }
}
