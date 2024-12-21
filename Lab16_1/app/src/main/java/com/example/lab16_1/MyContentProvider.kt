package com.example.lab16_1

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log

class MyContentProvider : ContentProvider() {
    private lateinit var dbrw: SQLiteDatabase

    override fun onCreate(): Boolean {
        val context = context ?: return false
        // 取得資料庫實體
        dbrw = MyDBHelper(context).writableDatabase
        return true
    }
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        if (values == null) return null
        try {
            // 將資料新增於資料庫並回傳此筆紀錄的Id
            val rowId = dbrw.insert("myTable", null, values)
            if (rowId > 0) {
                // 回傳此筆紀錄的Uri
                return Uri.parse("content://com.example.lab16/myTable/$rowId")
            } else {
                throw SQLException("Failed to insert row into $uri")
            }
        } catch (e: Exception) {
            Log.e("MyContentProvider", "Insert failed: $e")
            return null
        }
    }
    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        if (values == null || selection == null) return 0
        try {
            // 使用參數化查詢防止SQL注入
            val updatedRows = dbrw.update(
                "myTable", values, "book = ?", arrayOf(selection)
            )
            return updatedRows
        } catch (e: Exception) {
            Log.e("MyContentProvider", "Update failed: $e")
            return 0
        }
    }
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        if (selection == null) return 0
        try {
            // 使用參數化查詢防止SQL注入
            val deletedRows = dbrw.delete("myTable", "book = ?", arrayOf(selection))
            return deletedRows
        } catch (e: Exception) {
            Log.e("MyContentProvider", "Delete failed: $e")
            return 0
        }
    }
    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        try {
            // 根據書名查詢，如果沒有書名則查詢所有書籍
            val cursor = if (selection == null) {
                dbrw.query("myTable", projection, null, null, null, null, sortOrder)
            } else {
                dbrw.query(
                    "myTable", projection, "book = ?", arrayOf(selection), null, null, sortOrder
                )
            }
            cursor.setNotificationUri(context?.contentResolver, uri)
            return cursor
        } catch (e: Exception) {
            Log.e("MyContentProvider", "Query failed: $e")
            return null
        }
    }
    override fun getType(uri: Uri): String? {
        return when (uri.pathSegments[0]) {
            "myTable" -> "vnd.android.cursor.dir/vnd.com.example.lab16.myTable"
            else -> null
        }
    }
}
