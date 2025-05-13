package com.constructionmanagement.app.ui.worker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.constructionmanagement.app.R
import com.constructionmanagement.app.data.model.Worker
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import java.io.File

class WorkerAdapter(private val onWorkerClicked: (Long) -> Unit) :
    ListAdapter<Worker, WorkerAdapter.WorkerViewHolder>(WorkerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_worker, parent, false)
        return WorkerViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkerViewHolder, position: Int) {
        val worker = getItem(position)
        holder.bind(worker, onWorkerClicked)
    }

    class WorkerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textWorkerSN: TextView = itemView.findViewById(R.id.text_worker_sn)
        private val imageWorker: ImageView = itemView.findViewById(R.id.image_worker)
        private val textWorkerName: TextView = itemView.findViewById(R.id.text_worker_name)
        private val textWorkerMobile: TextView = itemView.findViewById(R.id.text_worker_mobile)
        private val textWorkerRole: TextView = itemView.findViewById(R.id.text_worker_skill)
        private val chipWorkerStatus: Chip = itemView.findViewById(R.id.chip_worker_status)
        private val chipWorkerSite: Chip = itemView.findViewById(R.id.chip_worker_site)
        private val buttonViewWorker: MaterialButton = itemView.findViewById(R.id.button_view_worker)

        fun bind(worker: Worker, onWorkerClicked: (Long) -> Unit) {
            // Set serial number (position + 1)
            textWorkerSN.text = (adapterPosition + 1).toString()
            textWorkerName.text = worker.name
            textWorkerMobile.text = worker.phoneNumber
            textWorkerRole.text = worker.role

            // Set worker status chip
            chipWorkerStatus.text = if (worker.isActive) "Active" else "Inactive"
            chipWorkerStatus.setChipBackgroundColorResource(
                if (worker.isActive) R.color.success else R.color.secondary_text
            )

            // Set profile image
            if (worker.profileImagePath != null) {
                val imageFile = File(worker.profileImagePath)
                if (imageFile.exists()) {
                    Glide.with(itemView.context)
                        .load(imageFile)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .centerCrop()
                        .into(imageWorker)
                } else {
                    imageWorker.setImageResource(R.drawable.ic_launcher_foreground)
                }
            } else {
                imageWorker.setImageResource(R.drawable.ic_launcher_foreground)
            }

            // Set view button click listener
            buttonViewWorker.setOnClickListener {
                onWorkerClicked(worker.id)
            }
        }
    }

    class WorkerDiffCallback : DiffUtil.ItemCallback<Worker>() {
        override fun areItemsTheSame(oldItem: Worker, newItem: Worker): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Worker, newItem: Worker): Boolean {
            return oldItem == newItem
        }
    }
}
