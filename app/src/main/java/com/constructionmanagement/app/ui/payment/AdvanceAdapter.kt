package com.constructionmanagement.app.ui.payment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.constructionmanagement.app.R
import com.constructionmanagement.app.data.model.Advance
import com.constructionmanagement.app.data.model.Worker
import com.constructionmanagement.app.util.CurrencyFormatter
import com.google.android.material.button.MaterialButton
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class AdvanceAdapter(
    private val onAdvanceClicked: (Long) -> Unit,
    private val lifecycleOwner: LifecycleOwner,
    private val workerLiveData: LiveData<List<Worker>>
) : ListAdapter<Advance, AdvanceAdapter.AdvanceViewHolder>(AdvanceDiffCallback()) {

    // Cache of workers for quick lookup - make it companion object so ViewHolder can access it
    companion object {
        var workerMap: Map<Long, Worker> = emptyMap()
    }

    init {
        // Observe workers and create a map for quick lookup by ID
        workerLiveData.observe(lifecycleOwner) { workers ->
            workerMap = workers.associateBy { it.id }
            notifyDataSetChanged() // Refresh the list to update worker names
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdvanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_advance, parent, false)
        return AdvanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdvanceViewHolder, position: Int) {
        val advance = getItem(position)
        holder.bind(advance, position + 1) // position + 1 for 1-based indexing

        // Apply alternating row backgrounds for striped table effect
        if (position % 2 == 0) {
            holder.itemView.setBackgroundResource(R.drawable.table_row_even)
        } else {
            holder.itemView.setBackgroundResource(R.drawable.table_row_odd)
        }

        holder.itemView.setOnClickListener {
            onAdvanceClicked(advance.advanceId)
        }
    }

    class AdvanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textSerialNumber: TextView = itemView.findViewById(R.id.text_serial_number)
        private val imageWorker: CircleImageView = itemView.findViewById(R.id.image_worker)
        private val textWorkerName: TextView = itemView.findViewById(R.id.text_worker_name)
        private val textAmount: TextView = itemView.findViewById(R.id.text_amount)
        private val textDate: TextView = itemView.findViewById(R.id.text_date)
        private val textReason: TextView = itemView.findViewById(R.id.text_reason)
        private val buttonStatus: MaterialButton = itemView.findViewById(R.id.button_advance_status)
        private val buttonViewAdvance: MaterialButton = itemView.findViewById(R.id.button_view_advance)

        fun bind(advance: Advance, serialNumber: Int) {
            // Set serial number
            textSerialNumber.text = serialNumber.toString()
            // Get worker from the cache
            val worker = workerMap[advance.workerId]

            // Set worker name (just the name, no "Worker ID:" prefix)
            textWorkerName.text = worker?.name ?: "Unknown"

            // Set worker image
            if (worker?.profileImagePath != null) {
                val imageFile = File(worker.profileImagePath)
                if (imageFile.exists()) {
                    Glide.with(itemView.context)
                        .load(imageFile as File) // Explicitly cast to File to resolve ambiguity
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .circleCrop()
                        .into(imageWorker)
                } else {
                    imageWorker.setImageResource(R.drawable.ic_person)
                }
            } else {
                imageWorker.setImageResource(R.drawable.ic_person)
            }

            textAmount.text = CurrencyFormatter.formatRupees(advance.amount)
            textDate.text = advance.advanceDate
            textReason.text = advance.reason

            // Set status button text and style
            if (advance.isRecovered) {
                buttonStatus.text = "Completed"
                buttonStatus.setBackgroundResource(R.drawable.button_status_completed_background)
            } else {
                buttonStatus.text = "Pending"
                buttonStatus.setBackgroundResource(R.drawable.button_status_pending_background)
            }

            // Status button is now just for display, not clickable
            buttonStatus.isClickable = false

            // Set click listener for the view button
            buttonViewAdvance.setOnClickListener {
                itemView.performClick()
            }
        }
    }

    class AdvanceDiffCallback : DiffUtil.ItemCallback<Advance>() {
        override fun areItemsTheSame(oldItem: Advance, newItem: Advance): Boolean {
            return oldItem.advanceId == newItem.advanceId
        }

        override fun areContentsTheSame(oldItem: Advance, newItem: Advance): Boolean {
            return oldItem == newItem
        }
    }
}
