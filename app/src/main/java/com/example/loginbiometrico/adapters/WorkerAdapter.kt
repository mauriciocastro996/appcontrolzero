package com.example.loginbiometrico.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.loginbiometrico.R
import com.example.loginbiometrico.models.Worker
import com.example.loginbiometrico.models.WorkerStatus

class WorkerAdapter(
    private var workers: List<Worker>,
    private val onValidateClick: (Worker) -> Unit,
    private val onDeleteClick: (Worker) -> Unit
) : RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_worker_card, parent, false)
        return WorkerViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkerViewHolder, position: Int) {
        val worker = workers[position]
        holder.bind(worker)
    }

    override fun getItemCount(): Int = workers.size

    fun updateWorkers(newWorkers: List<Worker>) {
        workers = newWorkers.toMutableList()
        notifyDataSetChanged()
    }

    inner class WorkerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvWorkerName: TextView = itemView.findViewById(R.id.tvWorkerName)
        private val tvWorkerStatus: TextView = itemView.findViewById(R.id.tvWorkerStatus)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        private val btnValidate: ImageButton = itemView.findViewById(R.id.btnValidate)

        fun bind(worker: Worker) {
            tvWorkerName.text = worker.name
            
            when (worker.status) {
                WorkerStatus.PENDING -> {
                    tvWorkerStatus.text = "Estado: Pendiente"
                    tvWorkerStatus.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
                    btnValidate.alpha = 1.0f
                    btnValidate.isEnabled = true
                    btnDelete.alpha = 1.0f
                    btnDelete.isEnabled = true
                }
                WorkerStatus.VALIDATED -> {
                    tvWorkerStatus.text = "Validado ${worker.bacLevel?.let { "%BAC" } ?: ""}"
                    tvWorkerStatus.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
                    btnValidate.alpha = 0.5f
                    btnValidate.isEnabled = false
                    btnDelete.alpha = 1.0f
                    btnDelete.isEnabled = true
                }
            }

            btnValidate.setOnClickListener {
                if (worker.status == WorkerStatus.PENDING) {
                    onValidateClick(worker)
                }
            }

            btnDelete.setOnClickListener {
                onDeleteClick(worker)
            }
        }
    }
}
