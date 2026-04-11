package com.example.loginbiometrico.ble

data class ScanDeviceUi(
    val status: String = "Listo",
    val isScanning: Boolean = false,
    val devices: List<BleDevice> = emptyList(),
    val connectionState: Int = 0,
    val currentBac: Float? = null,
    val lastReading: BacSnapshot? = null,
    val readings: List<BacSnapshot> = emptyList()
)

data class BleDevice(
    val name: String,
    val address: String,
    val rssi: Int
)

data class BacSnapshot(
    val timestamp: Long,
    val bac: Float,
    val temperature: Float
)
