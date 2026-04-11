package com.example.loginbiometrico.ble

import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class AlcoholemiaBleController(private val context: Context) {
    
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    
    private val bluetoothLeScanner: BluetoothLeScanner? by lazy {
        bluetoothAdapter?.bluetoothLeScanner
    }
    
    private val handler = Handler(Looper.getMainLooper())
    private val scanCallback = BleScanCallback()
    private val gattCallback = BleGattCallback()
    
    private val _uiState = MutableStateFlow(ScanDeviceUi())
    val uiState: StateFlow<ScanDeviceUi> = _uiState
    
    private val discoveredDevices = ConcurrentHashMap<String, BluetoothDevice>()
    private var connectedDevice: BluetoothDevice? = null
    private var bluetoothGatt: BluetoothGatt? = null
    
    companion object {
        private const val SCAN_PERIOD = 10000L
        private const val STATE_DISCONNECTED = 0
        private const val STATE_CONNECTING = 1
        private const val STATE_CONNECTED = 2
        
        private val ALCOHOLIMETRO_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB")
        private val BAC_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB")
    }
    
    fun startScan() {
        if (!hasBluetoothLe()) {
            _uiState.value = _uiState.value.copy(status = "BLE no disponible")
            return
        }
        if (!hasPermissions()) {
            _uiState.value = _uiState.value.copy(status = "Se requieren permisos de Bluetooth")
            return
        }
        discoveredDevices.clear()
        _uiState.value = _uiState.value.copy(
            status = "Escaneando dispositivos...",
            isScanning = true,
            devices = emptyList()
        )
        bluetoothLeScanner?.startScan(scanCallback)
        handler.postDelayed({ stopScan() }, SCAN_PERIOD)
    }
    
    fun stopScan() {
        bluetoothLeScanner?.stopScan(scanCallback)
        _uiState.value = _uiState.value.copy(status = "Escaneo detenido", isScanning = false)
    }
    
    fun connect() {
        val device = discoveredDevices.values.firstOrNull()
        if (device != null) {
            _uiState.value = _uiState.value.copy(status = "Conectando...")
            device.connectGatt(context, false, gattCallback)
        } else {
            _uiState.value = _uiState.value.copy(status = "No hay dispositivos disponibles")
        }
    }
    
    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        connectedDevice = null
        _uiState.value = _uiState.value.copy(
            status = "Desconectado",
            connectionState = STATE_DISCONNECTED
        )
    }
    
    fun startTest() {
        if (bluetoothGatt != null) {
            _uiState.value = _uiState.value.copy(status = "Iniciando prueba...")
            simulateAlcoholTest()
        } else {
            _uiState.value = _uiState.value.copy(status = "No conectado a dispositivo")
        }
    }
    
    fun isConnected(): Boolean = _uiState.value.connectionState == STATE_CONNECTED
    
    fun getCurrentBac(): Float? = _uiState.value.currentBac
    
    private fun simulateAlcoholTest() {
        _uiState.value = _uiState.value.copy(status = "Midiendo...")
        handler.postDelayed({
            val simulatedBac = Random.nextFloat() * (0.05f - 0.001f) + 0.001f
            val snapshot = BacSnapshot(
                timestamp = System.currentTimeMillis(),
                bac = simulatedBac,
                temperature = Random.nextFloat() * (37.5f - 36.0f) + 36.0f
            )
            _uiState.value = _uiState.value.copy(
                status = "Prueba completada",
                currentBac = simulatedBac,
                lastReading = snapshot,
                readings = _uiState.value.readings + snapshot
            )
        }, 3000)
    }
    
    private fun hasBluetoothLe(): Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    
    private fun hasPermissions(): Boolean =
        context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_SCAN) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED &&
        context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
    
    private inner class BleScanCallback : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            discoveredDevices[device.address] = device
            val deviceList = discoveredDevices.values.map {
                BleDevice(
                    name = it.name ?: "Dispositivo desconocido",
                    address = it.address,
                    rssi = result.rssi
                )
            }
            _uiState.value = _uiState.value.copy(
                status = "Dispositivos encontrados: ${deviceList.size}",
                devices = deviceList
            )
        }
        override fun onScanFailed(errorCode: Int) {
            _uiState.value = _uiState.value.copy(
                status = "Error en escaneo: $errorCode",
                isScanning = false
            )
        }
    }
    
    private inner class BleGattCallback : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    _uiState.value = _uiState.value.copy(
                        status = "Conectado a ${gatt.device.name}",
                        connectionState = STATE_CONNECTED
                    )
                    connectedDevice = gatt.device
                    bluetoothGatt = gatt
                    handler.postDelayed({ gatt.discoverServices() }, 1000)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    _uiState.value = _uiState.value.copy(
                        status = "Desconectado",
                        connectionState = STATE_DISCONNECTED,
                        currentBac = null
                    )
                    connectedDevice = null
                    bluetoothGatt = null
                }
            }
        }
        
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                _uiState.value = _uiState.value.copy(status = "Servicios descubiertos")
                gatt.services.forEach { service ->
                    if (service.uuid == ALCOHOLIMETRO_SERVICE_UUID) {
                        service.characteristics.forEach { characteristic ->
                            if (characteristic.uuid == BAC_CHARACTERISTIC_UUID) {
                                gatt.setCharacteristicNotification(characteristic, true)
                                _uiState.value = _uiState.value.copy(status = "Listo para medir")
                            }
                        }
                    }
                }
            }
        }
        
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            if (characteristic.uuid == BAC_CHARACTERISTIC_UUID) {
                val bacValue = parseBacValue(characteristic.value)
                val snapshot = BacSnapshot(
                    timestamp = System.currentTimeMillis(),
                    bac = bacValue,
                    temperature = 36.5f
                )
                _uiState.value = _uiState.value.copy(
                    currentBac = bacValue,
                    lastReading = snapshot,
                    readings = _uiState.value.readings + snapshot
                )
            }
        }
    }
    
    private fun parseBacValue(data: ByteArray?): Float {
        return if (data != null && data.isNotEmpty()) {
            (data[0].toInt() and 0xFF) / 1000.0f
        } else {
            0.0f
        }
    }
}
