package com.turnify.app

import LoginRequest
import PushNotificationService
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.turnify.app.services.HttpService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        getNotificationPermission()
        PushNotificationService.initialize(this, permissionLauncher)
        val loginBody = LoginRequest(
            email = "tes@test.com",
            password = "test"
        )
        lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) {
                HttpService.fetch(
                    "https://d2e3-181-51-32-120.ngrok-free.app/api",
                    "/authentication/login",
                    loginBody,
                    "POST"
                )
            }

            Log.d("Respuesta", response ?: "Sin respuesta")
        }
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
