package com.turnify.app.helpers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DataHelper(context: Context) : SQLiteOpenHelper(context, "userInfo.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE auth (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                token TEXT,
                refreshToken TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS auth")
        onCreate(db)
    }

    fun insertarTokens(token: String, refreshToken: String): Boolean {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put("token", token)
            put("refreshToken", refreshToken)
        }
        val resultado = db.insert("auth", null, valores)
        return resultado != -1L
    }

    fun obtenerToken(): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT token FROM auth", null)
        var token = ""
        if (cursor.moveToFirst()) {
            token = cursor.getString(0)
        }
        cursor.close()
        return token
    }

    fun obtenerRefreshToken(): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT refreshToken FROM auth", null)
        var refreshToken = ""
        if (cursor.moveToFirst()) {
            refreshToken = cursor.getString(0)
        }
        cursor.close()
        return refreshToken
    }
}