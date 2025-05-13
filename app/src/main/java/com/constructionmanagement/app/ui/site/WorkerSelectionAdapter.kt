package com.constructionmanagement.app.ui.site

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.constructionmanagement.app.R
import com.constructionmanagement.app.data.model.Worker
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class WorkerSelectionAdapter(
    private val onWorkerSelected: (Worker, Boolean) -> Unit
) : ListAdapter<WorkerSelectionAdapter.WorkerSelectionItem, WorkerSelectionAdapter.WorkerSelectionViewHolder>(
    WorkerSelectionDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkerSelectionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_worker_selection, parent, false)
        return WorkerSelectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkerSelectionViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onWorkerSelected)
    }

    fun getSelectedWorkers(): List<Worker> {
        return currentList.filter { it.isSelected }.map { it.worker }
    }

    fun updateSelection(worker: Worker, isSelected: Boolean) {
        val currentList = currentList.toMutableList()
        val index = currentList.indexOfFirst { it.worker.id == worker.id }
        if (index != -1) {
            currentList[index] = currentList[index].copy(isSelected = isSelected)
            submitList(currentList)
        }
    }

    class WorkerSelectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageWorker: CircleImageView = itemView.findViewById(R.id.image_worker)
        private val textWorkerName: TextView = itemView.findViewById(R.id.text_worker_name)
        private val textWorkerRole: TextView = itemView.findViewById(R.id.text_worker_role)
        private val checkboxSelect: CheckBox = itemView.findViewById(R.id.checkbox_select)

        fun bind(item: WorkerSelectionItem, onWorkerSelected: (Worker, Boolean) -> Unit) {
            val worker = item.worker
            textWorkerName.text = worker.name
            textWorkerRole.text = worker.role
            checkboxSelect.isChecked = item.isSelected

            // Load worker image if available
            if (worker.profileImagePath != null) {
                Glide.with(itemView.context)
                    .load(File(worker.profileImagePath))
                    .placeholder(R.drawable.ic_person)
                    .circleCrop()
                    .into(imageWorker)
            } else {
                imageWorker.setImageResource(R.drawable.ic_person)
            }

            // Set checkbox change listener
            checkboxSelect.setOnCheckedChangeListener { _, isChecked ->
                onWorkerSelected(worker, isChecked)
            }

            // Make the whole item clickable to toggle selection
            itemView.setOnClickListener {
                checkboxSelect.isChecked = !checkboxSelect.isChecked
            }
        }
    }

    data class WorkerSelectionItem(
        val worker: Worker,
        val isSelected: Boolean = false
    )

    private class WorkerSelectionDiffCallback : DiffUtil.ItemCallback<WorkerSelectionItem>() {
        override fun areItemsTheSame(oldItem: WorkerSelectionItem, newItem: WorkerSelectionItem): Boolean {
            return oldItem.worker.id == newItem.worker.id
        }

        override fun areContentsTheSame(oldItem: WorkerSelectionItem, newItem: WorkerSelectionItem): Boolean {
            return oldItem == newItem
        }
    }
}
