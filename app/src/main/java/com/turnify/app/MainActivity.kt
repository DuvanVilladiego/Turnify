package com.turnify.app

import PushNotificationService
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.turnify.app.fragments.alerts.SimpleTextButton

class MainActivity : AppCompatActivity() {

    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.hide()
        getNotificationPermission()
        PushNotificationService.initialize(this, permissionLauncher)
        setContentView(R.layout.main_layout)

        val btn: Button = findViewById(R.id.btnMainButton)
        btn.setOnClickListener {
            btn.isEnabled = false
            if (savedInstanceState == null)
            {
                val simplePopup = SimpleTextButton.Builder()
                    .isTitle("test")
                    .isMainButton("OK")
                    .build()

                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    add(R.id.fragmentContainerView, simplePopup)
                }
            }
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
