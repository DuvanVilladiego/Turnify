package com.turnify.app.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.turnify.app.R
import com.turnify.app.dataObjects.RegisterRequest
import com.turnify.app.services.HttpService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.turnify.app.Utils.Constants
import com.turnify.app.Utils.DatabaseManager
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {
    lateinit var registerEmail: EditText
    lateinit var registerName: EditText
    lateinit var registerLastName: EditText
    lateinit var registerPassword: EditText
    lateinit var registerCountry: EditText
    lateinit var registerPhone: EditText
    lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)
        supportActionBar?.hide()

        registerEmail = findViewById(R.id.RegisterEmail)
        registerName = findViewById(R.id.RegisterName)
        registerLastName = findViewById(R.id.RegisterLastName)
        registerPassword = findViewById(R.id.RegisterPassword)
        registerPhone = findViewById(R.id.RegisterPhone)
        registerButton = findViewById(R.id.btn_register)

        registerButton.setOnClickListener {
            Log.d("TOKEN", DatabaseManager.get().obtenerPushToken())
            if (areFieldsValid()) {
                val registerBody = RegisterRequest(
                    email = registerEmail.text.toString().trim(),
                    name = registerName.text.toString().trim(),
                    lastName = registerLastName.text.toString().trim(),
                    password = registerPassword.text.toString().trim(),
                    phone = registerPhone.text.toString().trim(),
                    type = 1,
                    phonetoken = DatabaseManager.get().obtenerPushToken()
                )

                lifecycleScope.launch {
                    val response = withContext(Dispatchers.IO) {
                        HttpService.fetch(
                            Constants.BASE_URL,
                            Constants.ENDPOINTS.REGISTER,
                            registerBody,
                            Constants.METHODS.POST
                        )
                    }

                    if (response == null) {
                        Toast.makeText(this@RegisterActivity, "Error en el servidor", Toast.LENGTH_SHORT).show()
                    } else {
                        val json = JSONObject(response)
                        if (json.getString("status") != "true") {
                            Toast.makeText(this@RegisterActivity, "Error al registrar: ${json.getString("message")}", Toast.LENGTH_SHORT).show()
                        } else {
                            DatabaseManager.get().insertarTokens(json.getJSONObject("data").getString("token"), json.getJSONObject("data").getString("refreshToken"))
                            Toast.makeText(this@RegisterActivity, "Registro exitoso", Toast.LENGTH_SHORT).show()
                            setResult(RESULT_OK)
                            finish()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun areFieldsValid(): Boolean {
        return registerEmail.text.toString().isNotBlank() &&
                registerName.text.toString().isNotBlank() &&
                registerLastName.text.toString().isNotBlank() &&
                registerPassword.text.toString().isNotBlank() &&
                registerCountry.text.toString().isNotBlank() &&
                registerPhone.text.toString().isNotBlank()
    }
}