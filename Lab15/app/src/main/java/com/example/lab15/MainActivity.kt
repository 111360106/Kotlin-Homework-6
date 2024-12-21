package com.example.lab15

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
            val book = edBook.text.toString()
            val price = edPrice.text.toString()

            if (book.isBlank() || price.isBlank()) {
                showToast("欄位請勿留空")
                return@setOnClickListener
            }
            try {
                dbrw.execSQL(
                    "INSERT INTO myTable(book, price) VALUES(?, ?)",
                    arrayOf(book, price.toIntOrNull() ?: throw IllegalArgumentException("價格必須為數字"))
                )
                showToast("新增: $book, 價格: $price")
                cleanEditText()
            } catch (e: Exception) {
                showToast("新增失敗: ${e.localizedMessage}")
            }
        }
        findViewById<Button>(R.id.btnUpdate).setOnClickListener {
            val book = edBook.text.toString()
            val price = edPrice.text.toString()

            if (book.isBlank() || price.isBlank()) {
                showToast("欄位請勿留空")
                return@setOnClickListener
            }
            try {
                dbrw.execSQL(
                    "UPDATE myTable SET price = ? WHERE book = ?",
                    arrayOf(price.toIntOrNull() ?: throw IllegalArgumentException("價格必須為數字"), book)
                )
                showToast("更新: $book, 價格: $price")
                cleanEditText()
            } catch (e: Exception) {
                showToast("更新失敗: ${e.localizedMessage}")
            }
        }
        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            val book = edBook.text.toString()

            if (book.isBlank()) {
                showToast("書名請勿留空")
                return@setOnClickListener
            }
            try {
                dbrw.execSQL(
                    "DELETE FROM myTable WHERE book = ?",
                    arrayOf(book)
                )
                showToast("刪除: $book")
                cleanEditText()
            } catch (e: Exception) {
                showToast("刪除失敗: ${e.localizedMessage}")
            }
        }
        findViewById<Button>(R.id.btnQuery).setOnClickListener {
            val book = edBook.text.toString()
            val queryString = if (book.isBlank()) {
                "SELECT * FROM myTable"
            } else {
                "SELECT * FROM myTable WHERE book = ?"
            }
            try {
                val c = if (book.isBlank()) {
                    dbrw.rawQuery(queryString, null)
                } else {
                    dbrw.rawQuery(queryString, arrayOf(book))
                }

                items.clear()
                showToast("共有 ${c.count} 筆資料")
                while (c.moveToNext()) {
                    items.add("書名: ${c.getString(0)}\t\t價格: ${c.getInt(1)}")
                }
                adapter.notifyDataSetChanged()
                c.close()
            } catch (e: Exception) {
                showToast("查詢失敗: ${e.localizedMessage}")
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
