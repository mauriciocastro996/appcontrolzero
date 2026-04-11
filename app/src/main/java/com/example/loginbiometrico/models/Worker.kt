package com.example.loginbiometrico.models

data class Worker(
    val id: String,
    val name: String,
    val status: WorkerStatus,
    val bacLevel: Float? = null
)

enum class WorkerStatus {
    PENDING,
    VALIDATED
}
