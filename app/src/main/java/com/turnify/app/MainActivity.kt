package com.turnify.app

import PushNotificationService
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.turnify.app.Utils.DatabaseManager
import com.turnify.app.activities.LoginActivity
import com.turnify.app.activities.ScanerActivity

class MainActivity : AppCompatActivity() {

    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var loginLauncher: ActivityResultLauncher<Intent>
    private lateinit var escanerLauncher: ActivityResultLauncher<Intent>
    private lateinit var btnEscanear: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_layout)
        supportActionBar?.hide()

        // Inicializar servicios
        DatabaseManager.init(this)

        initializePermissionLaunchers()
        PushNotificationService.initialize(this, notificationPermissionLauncher)

        // Verificar permisos al iniciar
        checkCameraPermission()

        // Inicializar vistas
        btnEscanear = findViewById(R.id.btnScanear)

        // Configurar launchers
        escanerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            ejecutarAlVolverDeEscanear(it)
        }

        loginLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            ejecutarAlVolverDelLogin()
        }

        // Lanzar login si no hay token
        if (DatabaseManager.get().obtenerToken().isNullOrBlank()) {
            val loginIntent = Intent(this, LoginActivity::class.java)
            loginLauncher.launch(loginIntent)
        }

        btnEscanear.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                scanQr()
            } else {
                Toast.makeText(this, "Permiso de cámara requerido para escanear", Toast.LENGTH_SHORT).show()
                checkCameraPermission()
            }
        }
    }

    private fun scanQr() {
        val scanIntent = Intent(this, ScanerActivity::class.java)
        escanerLauncher.launch(scanIntent)
    }

    private fun ejecutarAlVolverDeEscanear(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            val datoEscaneado = result.data?.getStringExtra("COODENUMBER")
            Toast.makeText(this, datoEscaneado.toString(), Toast.LENGTH_SHORT).show()
            Log.d("ESCANER", "Dato escaneado: $datoEscaneado")
        }
    }

    private fun ejecutarAlVolverDelLogin() {
        val token = DatabaseManager.get().obtenerToken()
        Log.d("LOGIN", "Token recibido: $token")
    }

    private fun initializePermissionLaunchers() {
        notificationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            PushNotificationService.handlePermissionResult(this, isGranted)
        }

        cameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, "Permiso de cámara concedido", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("PERMISSION", "Permiso de cámara ya concedido")
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) -> {
                Toast.makeText(this, "Se necesita el permiso de cámara para escanear códigos", Toast.LENGTH_LONG).show()
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}
