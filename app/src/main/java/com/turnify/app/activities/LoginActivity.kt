package com.turnify.app.activities

import LoginRequest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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

    private lateinit var usuario: EditText
    private lateinit var contrasena: EditText
    private lateinit var boton: Button
    private lateinit var goToRegisterButton: Button
    private lateinit var registerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login)
        supportActionBar?.hide()

        usuario = findViewById(R.id.editTextUsuario)
        contrasena = findViewById(R.id.editTextContraseña)
        boton = findViewById(R.id.btn_continue)
        goToRegisterButton = findViewById(R.id.btn_pageregister)

        registerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            ejecutarAlVolver(result)
        }

        boton.setOnClickListener {
            val userText = usuario.text.toString().trim()
            val passText = contrasena.text.toString().trim()

            if (userText.isEmpty() || passText.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa usuario y contraseña", Toast.LENGTH_SHORT).show()
            } else {
                val loginBody = LoginRequest(email = userText, password = passText, phonetoken = DatabaseManager.get().obtenerPushToken())

                lifecycleScope.launch {
                    val response = withContext(Dispatchers.IO) {
                        HttpService.fetch(
                            Constants.BASE_URL_AUTH,
                            Constants.ENDPOINTS.LOGIN,
                            loginBody,
                            Constants.METHODS.POST
                        )
                    }

                    if (response == null) {
                        Toast.makeText(this@LoginActivity, "Error en el servidor", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    val json = JSONObject(response)
                    val status = json.optString("status")

                    if (status != "true") {
                        Toast.makeText(this@LoginActivity, "Error en el login", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    val data = json.getJSONObject("data")
                    val token = data.optString("token")
                    val refreshToken = data.optString("refreshToken")

                    DatabaseManager.get().insertarTokens(token, refreshToken)
                    Toast.makeText(this@LoginActivity, "Login Exitoso", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }

        goToRegisterButton.setOnClickListener {
            val registerIntent = Intent(this, RegisterActivity::class.java)
            registerLauncher.launch(registerIntent)
        }
    }

    private fun ejecutarAlVolver(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            finish()
        } else {
            Toast.makeText(this, "Registro cancelado", Toast.LENGTH_SHORT).show()
        }
    }
}
