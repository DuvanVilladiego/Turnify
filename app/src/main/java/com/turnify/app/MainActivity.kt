package com.turnify.app

import PushNotificationService
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.turnify.app.Utils.DatabaseManager
import com.turnify.app.activities.LoginActivity

class MainActivity : AppCompatActivity() {

    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var loginLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        DatabaseManager.init(this)
        getNotificationPermission()
        supportActionBar?.hide()
        PushNotificationService.initialize(this, permissionLauncher)
        loginLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            ejecutarAlVolverDelLogin()
        }
        val loginIntent = Intent(this, LoginActivity::class.java)
        loginLauncher.launch(loginIntent)
    }

    private fun ejecutarAlVolverDelLogin() {
        val token = DatabaseManager.get().obtenerToken()
        Log.d("TOKENAUTH", token)
    }

    private fun getNotificationPermission()
    {
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            PushNotificationService.handlePermissionResult(this, isGranted)
        }
    }
}