package com.turnify.app

import PushNotificationService
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.turnify.app.Utils.Constants
import com.turnify.app.Utils.DatabaseManager
import com.turnify.app.activities.LoginActivity
import com.turnify.app.activities.ScannerActivity
import com.turnify.app.fragments.alerts.SimpleTextButton
import com.turnify.app.services.HttpService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var loginLauncher: ActivityResultLauncher<Intent>
    private lateinit var scannerLauncher: ActivityResultLauncher<Intent>
    private lateinit var btnScanner: FloatingActionButton
    private lateinit var mainTitle : TextView
    private lateinit var mainNumber : TextView
    private lateinit var logo : ImageView

    private lateinit var id : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_layout)
        supportActionBar?.hide()

        DatabaseManager.init(this)

        initializePermissionLaunchers()
        PushNotificationService.initialize(this, notificationPermissionLauncher)

        checkCameraPermission()

        btnScanner = findViewById(R.id.btnScanear)
        mainTitle = findViewById(R.id.turnInfo)
        mainNumber = findViewById(R.id.turnoLabel)
        logo = findViewById(R.id.TurnifyLogo)

        scannerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            ejecutarAlVolverDeEscanear(it)
        }

        loginLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            ejecutarAlVolverDelLogin()
        }

        val data = intent?.data
        if (data != null) {
            val codigo = data.getQueryParameter("id")
            if (!codigo.isNullOrEmpty()) {
                subscribe(codigo)
            } else {
                Toast.makeText(this, "TURNO INVALIDO", Toast.LENGTH_SHORT).show()
            }
        }

        if (DatabaseManager.get().obtenerToken().isNullOrBlank()) {
            val loginIntent = Intent(this, LoginActivity::class.java)
            loginLauncher.launch(loginIntent)
        }

        if (!DatabaseManager.get().obtenerToken().isNullOrBlank()) {
            getShifts()
        }

        btnScanner.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                scanQr()
            } else {
                Toast.makeText(this, "Permiso de cámara requerido para escanear", Toast.LENGTH_SHORT).show()
                checkCameraPermission()
            }
        }

        supportFragmentManager.setFragmentResultListener("confirm_shift_result", this) { _, bundle ->
            val shouldConfirm = bundle.getBoolean("confirm", false)
            if (shouldConfirm) {
                confirmShift(id)
            }
        }

        logo.setOnClickListener {
            val fragment = SimpleTextButton.Builder()
                .isMainButton("CONFIRMAR") // Texto del botón
                .isTitle("CONFIRMAR TURNO") // Título del fragmento
                .isDescription("CONFIRMAR EL TURNO TE SACARÁ DE LA FILA") // Descripción
                .setButtonClickListener {
                    // Acción al presionar el botón CONFIRMAR
                    confirmShift(id) // <-- reemplaza esto con el ID dinámico
                }
                .build()

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }

        supportFragmentManager.addOnBackStackChangedListener {
            val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
            if (fragment == null) {
                getShifts()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val token = DatabaseManager.get().obtenerToken()
        if (!token.isNullOrBlank()) {
            getShifts()
        }
    }

    private fun scanQr() {
        val scanIntent = Intent(this, ScannerActivity::class.java)
        scannerLauncher.launch(scanIntent)
    }

    private fun ejecutarAlVolverDeEscanear(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            val datoEscaneado = result.data?.getStringExtra("COODENUMBER")
            if (!datoEscaneado.isNullOrEmpty()) {
                subscribe(datoEscaneado)
            } else {
                Toast.makeText(this, "TURNO INVALIDO", Toast.LENGTH_SHORT).show()
            }
        }
        getShifts()
    }

    private fun ejecutarAlVolverDelLogin() {
        val token = DatabaseManager.get().obtenerToken()
        Log.d("LOGIN", "Token recibido: $token")
        getShifts()
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

    private fun getShifts() {
        lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) {
                val headers = mapOf(
                    "Authorization" to "Bearer ${DatabaseManager.get().obtenerToken()}"
                )

                HttpService.fetch(
                    Constants.BASE_URL_SHIFTS,
                    Constants.ENDPOINTS.GET_ALL,
                    null,
                    Constants.METHODS.GET,
                    headers
                )
            }

            if (response.isNullOrBlank()) {
                Toast.makeText(this@MainActivity, "Respuesta vacía del servidor", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val json = JSONObject(response)
            val status = json.optString("status")

            if (status != "true") {
                Toast.makeText(this@MainActivity, "Error al obtener los turnos", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val dataArray = json.optJSONArray("data")
            if (dataArray != null) {
                fillShiftList(dataArray)
            }
        }
    }

    private fun fillShiftList(dataArray: JSONArray) {
        val container = findViewById<LinearLayout>(R.id.cardContainer)
        container.removeAllViews()

        for (i in 0 until dataArray.length()) {
            val shift = dataArray.getJSONObject(i)

            val cardView = layoutInflater.inflate(R.layout.item_card_turnify, container, false)

            val turnCount = cardView.findViewById<TextView>(R.id.turnCountTurnify)
            val turnNum = cardView.findViewById<TextView>(R.id.turnNumTurnify)
            val label = cardView.findViewById<TextView>(R.id.labelTurnify)

            val position = shift.optString("position")
            val number = shift.optString("number")
            val description = shift.optString("description")

            cardView.setOnClickListener {
                updateMain(position, number)
            }

            turnCount.text = "$position Turnos"
            turnNum.text = number
            label.text = "Turnify\n$description"

            container.addView(cardView)
        }
    }

    private fun updateMain(waiting: String, number: String) {
        mainTitle.text = "Estás a ${waiting} Turnos"
        mainNumber.text = "Turno ${number}"
    }

    private fun subscribe(code: String) {
        lifecycleScope.launch {
            id = getShift(code)
            if (id.isNullOrEmpty()) {
                Toast.makeText(this@MainActivity, "No se encontró el turno", Toast.LENGTH_SHORT).show()
            } else {
                addShift(id)
            }
            getShifts()
        }
    }

    private suspend fun getShift(code: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val headers = mapOf(
                    "Authorization" to "Bearer ${DatabaseManager.get().obtenerToken()}"
                )

                val response = HttpService.fetch(
                    Constants.BASE_URL_SHIFTS,
                    Constants.ENDPOINTS.GET_SHIFT + code,
                    null,
                    Constants.METHODS.GET,
                    headers
                )

                if (response.isNullOrBlank()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Respuesta vacía del servidor", Toast.LENGTH_SHORT).show()
                    }
                    return@withContext ""
                }

                val json = JSONObject(response)
                val status = json.optString("status")

                if (status != "true") {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Código inválido: $code", Toast.LENGTH_SHORT).show()
                    }
                    return@withContext ""
                }

                val data = json.optJSONObject("data")
                val id = data?.optString("id").orEmpty()

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Turno $code obtenido con éxito", Toast.LENGTH_SHORT).show()
                }

                return@withContext id
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error al obtener turno $code", Toast.LENGTH_SHORT).show()
                }
                return@withContext ""
            }
        }
    }

    private fun addShift(id: String) {
        lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) {
                val headers = mapOf(
                    "Authorization" to "Bearer ${DatabaseManager.get().obtenerToken()}"
                )

                HttpService.fetch(
                    Constants.BASE_URL_SHIFTS,
                    Constants.ENDPOINTS.SUBSCRIBE + id,
                    null,
                    Constants.METHODS.GET,
                    headers
                )
            }

            if (!response.isNullOrBlank()) {
                Toast.makeText(this@MainActivity, "Turno suscrito con éxito", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Error al suscribirse al turno", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmShift(id: String) {
        lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) {
                val headers = mapOf(
                    "Authorization" to "Bearer ${DatabaseManager.get().obtenerToken()}"
                )

                HttpService.fetch(
                    Constants.BASE_URL_SHIFTS,
                    Constants.ENDPOINTS.COMPLETE + id,
                    null,
                    Constants.METHODS.GET,
                    headers
                )
            }

            if (!response.isNullOrBlank()) {
                Toast.makeText(this@MainActivity, "Turno confirmado", Toast.LENGTH_SHORT).show()
                getShifts()
            } else {
                Toast.makeText(this@MainActivity, "Error al confirmar turno", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
