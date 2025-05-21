package com.turnify.app

import PushNotificationService
import android.Manifest
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

    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var loginLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        DatabaseManager.init(this)

        // Registrar los permisos
        registerPermissionLaunchers()

        // Solicitar los permisos
        requestNotificationPermission()
        requestCameraPermission()

        supportActionBar?.hide()
        PushNotificationService.initialize(this, notificationPermissionLauncher)

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

    private fun registerPermissionLaunchers() {
        notificationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            PushNotificationService.handlePermissionResult(this, isGranted)
        }

        cameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                Log.d("PERMISSION", "Permiso de cámara concedido.")
            } else {
                Log.d("PERMISSION", "Permiso de cámara denegado.")
            }
        }
    }

    private fun requestNotificationPermission() {
        notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun requestCameraPermission() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }
}
