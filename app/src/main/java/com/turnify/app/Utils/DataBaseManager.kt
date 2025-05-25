package com.turnify.app.Utils

import android.content.Context
import com.turnify.app.helpers.DataHelper

object DatabaseManager {
    private var userInfo: DataHelper? = null

    fun init(context: Context) {
        if (userInfo == null) {
            userInfo = DataHelper(context.applicationContext)
        }
    }

    fun get(): DataHelper {
        return userInfo ?: throw IllegalStateException("DatabaseManager no ha sido inicializado. Llama a init() primero.")
    }
}
