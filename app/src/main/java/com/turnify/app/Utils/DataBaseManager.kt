package com.turnify.app.Utils

import android.content.Context
import com.turnify.app.helpers.DataHelper

object DatabaseManager {
    lateinit var userInfo: DataHelper

    fun init(context: Context) {
        userInfo = DataHelper(context.applicationContext)
    }

    fun get(): DataHelper {
        return userInfo
    }
}
