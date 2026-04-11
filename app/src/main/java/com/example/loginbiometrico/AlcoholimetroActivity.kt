package com.example.loginbiometrico

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.loginbiometrico.ble.AlcoholemiaBleController
import com.example.loginbiometrico.ble.BacSnapshot
import com.example.loginbiometrico.ble.ScanDeviceUi
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class AlcoholimetroActivity : AppCompatActivity() {

    private lateinit var bleController: AlcoholemiaBleController
    private val decimalFormat = DecimalFormat("0.000")
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    
    private val requiredPermissions = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    )
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val hasPermissions = result.values.all { it }
        if (!hasPermissions) {
            Toast.makeText(this, "Se requieren permisos de Bluetooth", Toast.LENGTH_LONG).show()
        }
    }
    
    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Revisar estado al volver
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alcoholimetro)
        
        initializeViews()
        setupBluetooth()
        setupEventListeners()
    }
    
    private fun initializeViews() {
        bleController = AlcoholemiaBleController(applicationContext)
        observeBleState()
    }
    
    private fun setupBluetooth() {
        val hasPermissions = requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
        
        if (!hasPermissions) {
            permissionLauncher.launch(requiredPermissions)
        }
        
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
        }
    }
    
    private fun observeBleState() {
        lifecycleScope.launch {
            bleController.uiState.collect { state ->
                updateUI(state)
            }
        }
    }
    
    private fun updateUI(state: ScanDeviceUi) {
        // Update UI based on BLE state
        findViewById<android.widget.TextView>(R.id.tvStatus)?.text = state.status
        findViewById<android.widget.TextView>(R.id.tvBacValue)?.text = 
            if (state.currentBac != null) "${decimalFormat.format(state.currentBac)}% BAC" else "---"
        findViewById<android.widget.TextView>(R.id.tvLastReading)?.text = 
            if (state.lastReading != null) "Última lectura: ${timeFormat.format(Date(state.lastReading.timestamp))}" else "Sin lecturas"
        
        // Update progress indicators
        findViewById<android.widget.ProgressBar>(R.id.progressBar)?.visibility = 
            if (state.isScanning) android.view.View.VISIBLE else android.view.View.GONE
    }
    
    private fun setupEventListeners() {
        findViewById<android.widget.Button>(R.id.btnScan)?.setOnClickListener {
            startScan()
        }
        
        findViewById<android.widget.Button>(R.id.btnStop)?.setOnClickListener {
            stopScan()
        }
        
        findViewById<android.widget.Button>(R.id.btnConnect)?.setOnClickListener {
            connectToDevice()
        }
        
        findViewById<android.widget.Button>(R.id.btnDisconnect)?.setOnClickListener {
            disconnectDevice()
        }
        
        findViewById<android.widget.Button>(R.id.btnStartTest)?.setOnClickListener {
            startAlcoholTest()
        }
        
        findViewById<android.widget.Button>(R.id.btnSaveResult)?.setOnClickListener {
            saveTestResult()
        }
        
        findViewById<android.widget.Button>(R.id.btnBack)?.setOnClickListener {
            finish()
        }
    }
    
    private fun startScan() {
        if (checkPermissions()) {
            bleController.startScan()
            Toast.makeText(this, "Buscando dispositivos...", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun stopScan() {
        bleController.stopScan()
        Toast.makeText(this, "Búsqueda detenida", Toast.LENGTH_SHORT).show()
    }
    
    private fun connectToDevice() {
        lifecycleScope.launch {
            try {
                // Connect to first available device
                bleController.connect()
                Toast.makeText(this@AlcoholimetroActivity, "Conectando al dispositivo...", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@AlcoholimetroActivity, "Error al conectar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun disconnectDevice() {
        bleController.disconnect()
        Toast.makeText(this, "Dispositivo desconectado", Toast.LENGTH_SHORT).show()
    }
    
    private fun startAlcoholTest() {
        if (bleController.isConnected()) {
            lifecycleScope.launch {
                try {
                    bleController.startTest()
                    Toast.makeText(this@AlcoholimetroActivity, "Iniciando prueba de alcoholemia...", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this@AlcoholimetroActivity, "Error al iniciar prueba: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(this, "Conecte un dispositivo primero", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun saveTestResult() {
        val currentBac = bleController.getCurrentBac()
        if (currentBac != null) {
            // TODO: Save to API
            // POST /api/v1/alcohol-tests with test results
            println("API CALL: POST /api/v1/alcohol-tests")
            println("BAC Level: $currentBac")
            println("Timestamp: ${Date()}")
            println("User ID: ${getUserId()}")
            
            Toast.makeText(this, "Resultado guardado exitosamente", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No hay resultados para guardar", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun checkPermissions(): Boolean {
        return requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun getUserId(): Long {
        // TODO: Get from TokenManager
        return 1L // Placeholder
    }
}
