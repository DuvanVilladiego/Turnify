package com.turnify.app.activities

import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import androidx.camera.core.*
import com.turnify.app.R
import java.util.concurrent.Executors
import android.net.Uri
import android.transition.Visibility
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView

class ScanerActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private lateinit var btnBackQr: ImageButton
    private lateinit var btnAddCode: ImageButton
    private lateinit var btnAddCamara: ImageButton
    private lateinit var btnAddManualCode: Button
    private lateinit var txtScannerTitle: TextView
    private lateinit var txtCodenumber: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scaner_layout)
        supportActionBar?.hide()
        btnBackQr = findViewById(R.id.btnBackQr)
        btnAddCode = findViewById(R.id.btnAddCode)
        btnAddCamara = findViewById(R.id.btnAddCamara)
        btnAddManualCode = findViewById(R.id.btnPutCode)
        previewView = findViewById(R.id.previewView)
        txtScannerTitle = findViewById(R.id.txtScannerTitle)
        txtCodenumber = findViewById(R.id.txtCodenumber)

        startCamera()

        btnAddCode.setOnClickListener() {
            enableInputCode()
        }

        btnAddCamara.setOnClickListener() {
            enableInputCamera()
        }

        btnBackQr.setOnClickListener(){
            this.finish()
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }

            val barcodeScanner = BarcodeScanning.getClient(
                BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(
                        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE
                    )
                    .build()
            )

            val analyzer = ImageAnalysis.Builder().build().apply {
                setAnalyzer(cameraExecutor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        barcodeScanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                for (barcode in barcodes) {
                                    barcode.rawValue?.let { value ->
                                        val resultIntent = Intent().apply {
                                            val uri = Uri.parse(value)
                                            val codigo = uri.getQueryParameter("id")
                                            putExtra("COODENUMBER", codigo)
                                        }
                                        setResult(RESULT_OK, resultIntent)
                                        finish()
                                    }
                                }
                                imageProxy.close()
                            }
                            .addOnFailureListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, analyzer)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun enableInputCode() {
        //esconder camara inputs
        previewView.visibility = INVISIBLE
        btnAddCode.visibility = INVISIBLE

        btnAddManualCode.visibility = VISIBLE
        txtCodenumber.visibility = VISIBLE
        btnAddCamara.visibility = VISIBLE

        txtScannerTitle.text = "Ingresar Codigo"
    }

    private fun enableInputCamera() {
        //esconder code inputs
        btnAddManualCode.visibility = INVISIBLE
        txtCodenumber.visibility = INVISIBLE
        btnAddCamara.visibility = INVISIBLE

        previewView.visibility = VISIBLE
        btnAddCode.visibility = VISIBLE

        txtScannerTitle.text = "Escanear QR"
    }
}
