package com.turnify.app.helpers

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DataHelper(context: Context) : SQLiteOpenHelper(context, "userInfo.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE auth (
                id INTEGER PRIMARY KEY,
                token TEXT,
                refreshToken TEXT,
                pushToken TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS auth")
        onCreate(db)
    }

    fun insertarPushToken(token: String): Boolean {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put("id", 1)
            put("pushToken", token)
        }
        // Replace para evitar conflicto de clave primaria
        val resultado = db.replace("auth", null, valores)
        return resultado != -1L
    }

    fun insertarTokens(token: String, refreshToken: String): Boolean {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put("id", 1)
            put("token", token)
            put("refreshToken", refreshToken)
        }
        val resultado = db.replace("auth", null, valores)
        return resultado != -1L
    }

    fun obtenerPushToken(): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT pushToken FROM auth WHERE id = 1", null)
        var token = ""
        if (cursor.moveToFirst()) {
            token = cursor.getString(0)
        }
        cursor.close()
        return token
    }

    fun obtenerToken(): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT token FROM auth WHERE id = 1", null)
        var token = ""
        if (cursor.moveToFirst()) {
            token = cursor.getString(0)
        }
        cursor.close()
        return token
    }

    fun obtenerRefreshToken(): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT refreshToken FROM auth WHERE id = 1", null)
        var refreshToken = ""
        if (cursor.moveToFirst()) {
            refreshToken = cursor.getString(0)
        }
        cursor.close()
        return refreshToken
    }
}
