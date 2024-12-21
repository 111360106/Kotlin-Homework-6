package com.example.lab16_1

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private var items: ArrayList<String> = ArrayList()
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var dbrw: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // 取得資料庫實體
        dbrw = MyDBHelper(this).writableDatabase
        // 宣告Adapter並連結ListView
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        findViewById<ListView>(R.id.listView).adapter = adapter

        // 設定監聽器
        setListener()
    }
    override fun onDestroy() {
        super.onDestroy()
        dbrw.close() // 關閉資料庫
    }
    // 設定監聽器
    private fun setListener() {
        val edBook = findViewById<EditText>(R.id.edBook)
        val edPrice = findViewById<EditText>(R.id.edPrice)

        findViewById<Button>(R.id.btnInsert).setOnClickListener {
            if (edBook.text.isEmpty() || edPrice.text.isEmpty()) {
                showToast("欄位請勿留空")
            } else {
                try {
                    // 使用參數化查詢防止SQL注入
                    val insertQuery = "INSERT INTO myTable(book, price) VALUES(?, ?)"
                    dbrw.execSQL(insertQuery, arrayOf(edBook.text.toString(), edPrice.text.toString()))
                    showToast("新增:${edBook.text}, 價格:${edPrice.text}")
                    cleanEditText()
                } catch (e: Exception) {
                    showToast("新增失敗:$e")
                }
            }
        }
        findViewById<Button>(R.id.btnUpdate).setOnClickListener {
            if (edBook.text.isEmpty() || edPrice.text.isEmpty()) {
                showToast("欄位請勿留空")
            } else {
                try {
                    val updateQuery = "UPDATE myTable SET price = ? WHERE book LIKE ?"
                    dbrw.execSQL(updateQuery, arrayOf(edPrice.text.toString(), edBook.text.toString()))
                    showToast("更新:${edBook.text}, 價格:${edPrice.text}")
                    cleanEditText()
                } catch (e: Exception) {
                    showToast("更新失敗:$e")
                }
            }
        }
        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            if (edBook.text.isEmpty()) {
                showToast("書名請勿留空")
            } else {
                try {
                    val deleteQuery = "DELETE FROM myTable WHERE book LIKE ?"
                    dbrw.execSQL(deleteQuery, arrayOf(edBook.text.toString()))
                    showToast("刪除:${edBook.text}")
                    cleanEditText()
                } catch (e: Exception) {
                    showToast("刪除失敗:$e")
                }
            }
        }
        findViewById<Button>(R.id.btnQuery).setOnClickListener {
            val queryString = if (edBook.text.isEmpty()) {
                "SELECT * FROM myTable"
            } else {
                "SELECT * FROM myTable WHERE book LIKE ?"
            }
            val cursor = dbrw.rawQuery(queryString, arrayOf(edBook.text.toString()))
            cursor.use {
                it.moveToFirst()
                items.clear()
                showToast("共有${it.count}筆資料")

                if (it.count > 0) {
                    do {
                        val bookName = it.getString(0)
                        val price = it.getInt(1)
                        items.add("書名:$bookName\t\t\t\t價格:$price")
                    } while (it.moveToNext())
                }
                adapter.notifyDataSetChanged()
            }
        }
    }
    // 建立showToast方法顯示Toast訊息
    private fun showToast(text: String) =
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    // 清空輸入的書名與價格
    private fun cleanEditText() {
        findViewById<EditText>(R.id.edBook).setText("")
        findViewById<EditText>(R.id.edPrice).setText("")
    }
}
