
package com.example.loginbiometrico

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.loginbiometrico.adapters.WorkerAdapter
import com.example.loginbiometrico.models.Supervisor
import com.example.loginbiometrico.models.Worker
import com.example.loginbiometrico.models.WorkerStatus
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class HomeActivity : AppCompatActivity() {

    private lateinit var tvSupervisorName: TextView
    private lateinit var tvSupervisorPosition: TextView
    private lateinit var tvSupervisorId: TextView
    private lateinit var etSearchWorker: EditText
    private lateinit var rvWorkerList: RecyclerView
    private lateinit var btnValidate: Button
    private lateinit var btnTask: Button
    private lateinit var btnCerrarSesion: MaterialButton

    private lateinit var workerAdapter: WorkerAdapter
    private var allWorkers: MutableList<Worker> = mutableListOf()
    private var filteredWorkers: MutableList<Worker> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        initializeViews()
        setupRecyclerView()
        loadMockData()
        setupEventListeners()
        updateValidateButton()
    }

    private fun initializeViews() {
        tvSupervisorName = findViewById(R.id.tvSupervisorName)
        tvSupervisorPosition = findViewById(R.id.tvSupervisorPosition)
        tvSupervisorId = findViewById(R.id.tvSupervisorId)
        etSearchWorker = findViewById(R.id.etSearchWorker)
        rvWorkerList = findViewById(R.id.rvWorkerList)
        btnValidate = findViewById(R.id.btnValidate)
        btnTask = findViewById(R.id.btnTask)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)
    }

    private fun setupRecyclerView() {
        workerAdapter = WorkerAdapter(
            workers = filteredWorkers,
            onValidateClick = { worker -> validateWorker(worker) },
            onDeleteClick = { worker -> deleteOrRevertWorker(worker) }
        )

        rvWorkerList.layoutManager = LinearLayoutManager(this)
        rvWorkerList.adapter = workerAdapter
    }

    private fun loadMockData() {
        // Mock supervisor data - TODO: Replace with GET /api/encargado/info
        val supervisor = Supervisor(
            id = "SUP-2024-001",
            name = "Juan Carlos Pérez",
            position = "Supervisor de Turno"
        )

        tvSupervisorName.text = supervisor.name
        tvSupervisorPosition.text = "Cargo: ${supervisor.position}"
        tvSupervisorId.text = "ID: ${supervisor.id}"

        // Mock worker data - TODO: Replace with GET /api/personal/asignado
        allWorkers = mutableListOf(
            Worker("1", "Pedro Rodriguez", WorkerStatus.PENDING, 0.02f),
            Worker("2", "Ana García", WorkerStatus.PENDING),
            Worker("3", "Luis Martínez", WorkerStatus.PENDING),
            Worker("4", "María López", WorkerStatus.PENDING, 0.01f),
            Worker("5", "Carlos Sánchez", WorkerStatus.PENDING)
        )

        filteredWorkers.addAll(allWorkers)
        workerAdapter.updateWorkers(filteredWorkers)
    }

    private fun setupEventListeners() {
        // Cerrar sesión
        btnCerrarSesion.setOnClickListener {
            val intent = Intent(this@HomeActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Search functionality
        etSearchWorker.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterWorkers(s.toString())
            }
        })

        // Validate button
        btnValidate.setOnClickListener {
            if (hasPendingWorkers()) {
                validateAllPendingWorkers()
            } else {
                navigateToAlcoholimetro()
            }
        }

        // Task button
        btnTask.setOnClickListener {
            showTaskCreationDialog()
        }
    }

    private fun filterWorkers(query: String) {
        filteredWorkers.clear()

        if (query.isBlank()) {
            filteredWorkers.addAll(allWorkers)
        } else {
            filteredWorkers.addAll(
                allWorkers.filter {
                    it.name.contains(query, ignoreCase = true)
                }
            )
        }

        workerAdapter.updateWorkers(filteredWorkers)
    }

    private fun validateWorker(worker: Worker) {
        val index = allWorkers.indexOfFirst { it.id == worker.id }
        if (index != -1) {
            allWorkers[index] = worker.copy(
                status = WorkerStatus.VALIDATED,
                bacLevel = (1..5).random() / 100f
            )
            filterWorkers(etSearchWorker.text.toString())
            updateValidateButton()

            // TODO: Call PUT /api/personal/{id}/validar
            println("API CALL: PUT /api/personal/${worker.id}/validar")
        }
    }

    private fun deleteOrRevertWorker(worker: Worker) {
        val index = allWorkers.indexOfFirst { it.id == worker.id }
        if (index != -1) {
            if (worker.status == WorkerStatus.PENDING) {
                allWorkers.removeAt(index)
                Toast.makeText(this, "Trabajador eliminado", Toast.LENGTH_SHORT).show()
                // TODO: Call PUT /api/personal/{id}/reemplazar
                println("API CALL: PUT /api/personal/${worker.id}/reemplazar")
            } else {
                allWorkers[index] = worker.copy(status = WorkerStatus.PENDING, bacLevel = null)
                Toast.makeText(this, "Validación revertida", Toast.LENGTH_SHORT).show()
            }
            filterWorkers(etSearchWorker.text.toString())
            updateValidateButton()
        }
    }

    private fun validateAllPendingWorkers() {
        val pendingWorkers = allWorkers.filter { it.status == WorkerStatus.PENDING }
        pendingWorkers.forEach { worker ->
            validateWorker(worker)
        }
        Toast.makeText(this, "Todos los trabajadores validados", Toast.LENGTH_SHORT).show()
    }

    private fun hasPendingWorkers(): Boolean {
        return allWorkers.any { it.status == WorkerStatus.PENDING }
    }

    private fun updateValidateButton() {
        val hasPending = hasPendingWorkers()
        btnValidate.isEnabled = hasPending
        btnValidate.text = if (hasPending) "Validar" else "Realizar pruebas"
    }

    private fun navigateToAlcoholimetro() {
        Toast.makeText(this, "Iniciando sesión de pruebas...", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, AlcoholimetroActivity::class.java)
        startActivity(intent)
    }

    private fun showTaskCreationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_task_creation, null)
        val etTaskName = dialogView.findViewById<TextInputEditText>(R.id.etTaskName)
        val etTaskLocation = dialogView.findViewById<TextInputEditText>(R.id.etTaskLocation)
        val etTaskDescription = dialogView.findViewById<TextInputEditText>(R.id.etTaskDescription)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val taskName = etTaskName.text.toString()
            val taskLocation = etTaskLocation.text.toString()
            val taskDescription = etTaskDescription.text.toString()

            if (taskName.isNotBlank() && taskLocation.isNotBlank() && taskDescription.isNotBlank()) {
                // TODO: Call POST /api/tareas/crear
                println("API CALL: POST /api/tareas/crear")
                println("Task Name: $taskName")
                println("Location: $taskLocation")
                println("Description: $taskDescription")

                Toast.makeText(this, "Tarea creada exitosamente", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
}