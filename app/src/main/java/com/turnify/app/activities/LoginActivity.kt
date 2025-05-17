package com.turnify.app.activities

import LoginRequest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.turnify.app.R
import com.turnify.app.Utils.Constants
import com.turnify.app.Utils.DatabaseManager
import com.turnify.app.services.HttpService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    lateinit var usuario: EditText
    lateinit var contrasena: EditText
    lateinit var boton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login)
        supportActionBar?.hide()

        usuario = findViewById(R.id.editTextUsuario)
        contrasena = findViewById(R.id.editTextContraseña)
        boton = findViewById(R.id.btn_continue)

        boton.setOnClickListener {
            val userText = usuario.text.toString().trim()
            val passText = contrasena.text.toString().trim()

            if (userText.isEmpty() || passText.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa usuario y contraseña", Toast.LENGTH_SHORT).show()
            } else {
                val loginBody = LoginRequest(
                    email = userText,
                    password = passText
                )
                lifecycleScope.launch {
                    val response = withContext(Dispatchers.IO) {
                        HttpService.fetch(
                            Constants.BASE_URL,
                            Constants.ENDPOINTS.LOGIN,
                            loginBody,
                            Constants.METHODS.POST
                        )
                    }
                    if (response == null) {
                        Toast.makeText(this@LoginActivity, "Error en el servidor", Toast.LENGTH_SHORT).show()
                    }
                    if (response != null) {
                        val json = JSONObject(response)
                        if (json.getString("status") != "true") {
                            Toast.makeText(this@LoginActivity, "Error en el login", Toast.LENGTH_SHORT).show()
                        }
                        if (json.getString("status") == "true") {
                            DatabaseManager.get().insertarTokens(json.getJSONObject("data").getString("token"), json.getJSONObject("data").getString("refreshToken"))
                            Toast.makeText(this@LoginActivity, "Login Exitoso", Toast.LENGTH_SHORT).show()
                            setResult(RESULT_OK)
                            finish()
                        }
                    }
                }
            }
        }
    }
}
