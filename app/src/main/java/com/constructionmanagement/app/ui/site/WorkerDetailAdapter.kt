package com.constructionmanagement.app.ui.site

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.constructionmanagement.app.R
import com.constructionmanagement.app.data.model.Worker
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class WorkerDetailAdapter : ListAdapter<Worker, WorkerDetailAdapter.WorkerDetailViewHolder>(WorkerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkerDetailViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_worker_detail, parent, false)
        return WorkerDetailViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkerDetailViewHolder, position: Int) {
        val worker = getItem(position)
        holder.bind(worker)
    }

    class WorkerDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageWorker: CircleImageView = itemView.findViewById(R.id.image_worker)
        private val textWorkerName: TextView = itemView.findViewById(R.id.text_worker_name)
        private val textWorkerRole: TextView = itemView.findViewById(R.id.text_worker_role)

        fun bind(worker: Worker) {
            textWorkerName.text = worker.name
            textWorkerRole.text = worker.role

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
        }
    }

    private class WorkerDiffCallback : DiffUtil.ItemCallback<Worker>() {
        override fun areItemsTheSame(oldItem: Worker, newItem: Worker): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Worker, newItem: Worker): Boolean {
            return oldItem == newItem
        }
    }
}
