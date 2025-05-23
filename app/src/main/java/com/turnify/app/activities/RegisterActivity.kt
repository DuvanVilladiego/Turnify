package com.turnify.app.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.turnify.app.R
import com.turnify.app.Utils.Constants
import com.turnify.app.Utils.DatabaseManager
import com.turnify.app.dataObjects.RegisterRequest
import com.turnify.app.services.HttpService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {

    private lateinit var registerEmail: EditText
    private lateinit var registerName: EditText
    private lateinit var registerLastName: EditText
    private lateinit var registerPassword: EditText
    private lateinit var registerPhone: EditText
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)
        enableEdgeToEdge()
        supportActionBar?.hide()

        initializeViews()

        registerButton.setOnClickListener {
            if (!areFieldsValid()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val pushToken = DatabaseManager.get().obtenerPushToken()
            Log.d("TOKEN", pushToken)

            val registerBody = RegisterRequest(
                email = registerEmail.text.trim().toString(),
                name = registerName.text.trim().toString(),
                lastName = registerLastName.text.trim().toString(),
                password = registerPassword.text.trim().toString(),
                phone = registerPhone.text.trim().toString(),
                type = 1,
                phonetoken = pushToken
            )

            lifecycleScope.launch {
                val response = withContext(Dispatchers.IO) {
                    runCatching {
                        HttpService.fetch(
                            Constants.BASE_URL,
                            Constants.ENDPOINTS.REGISTER,
                            registerBody,
                            Constants.METHODS.POST
                        )
                    }.getOrNull()
                }

                handleResponse(response)
            }
        }
    }

    private fun initializeViews() {
        registerEmail = findViewById(R.id.RegisterEmail)
        registerName = findViewById(R.id.RegisterName)
        registerLastName = findViewById(R.id.RegisterLastName)
        registerPassword = findViewById(R.id.RegisterPassword)
        registerPhone = findViewById(R.id.RegisterPhone)
        registerButton = findViewById(R.id.btn_register)
    }

    private fun areFieldsValid(): Boolean {
        return listOf(
            registerEmail.text,
            registerName.text,
            registerLastName.text,
            registerPassword.text,
            registerPhone.text
        ).all { it.toString().isNotBlank() }
    }

    private fun handleResponse(response: String?) {
        if (response == null) {
            Toast.makeText(this, "Error en el servidor. Intenta más tarde.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val json = JSONObject(response)
            if (json.optString("status") != "true") {
                val message = json.optString("message", "Error desconocido")
                Toast.makeText(this, "Error al registrar: $message", Toast.LENGTH_LONG).show()
                return
            }

            val data = json.getJSONObject("data")
            val token = data.getString("token")
            val refreshToken = data.getString("refreshToken")

            DatabaseManager.get().insertarTokens(token, refreshToken)

            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Error procesando respuesta del servidor", Toast.LENGTH_SHORT).show()
            Log.e("RegisterActivity", "JSON parsing error", e)
        }
    }
}
