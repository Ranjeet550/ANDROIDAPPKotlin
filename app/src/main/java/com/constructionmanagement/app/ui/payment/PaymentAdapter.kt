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
import com.constructionmanagement.app.data.model.Payment
import com.constructionmanagement.app.data.model.Worker
import com.constructionmanagement.app.util.CurrencyFormatter
import com.google.android.material.button.MaterialButton
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class PaymentAdapter(
    private val onPaymentClicked: (Long) -> Unit,
    private val lifecycleOwner: LifecycleOwner,
    private val workerLiveData: LiveData<List<Worker>>
) : ListAdapter<Payment, PaymentAdapter.PaymentViewHolder>(PaymentDiffCallback()) {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment, parent, false)
        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val payment = getItem(position)
        holder.bind(payment, position + 1) // position + 1 for 1-based indexing

        // Apply alternating row backgrounds for striped table effect
        if (position % 2 == 0) {
            holder.itemView.setBackgroundResource(R.drawable.table_row_even)
        } else {
            holder.itemView.setBackgroundResource(R.drawable.table_row_odd)
        }

        holder.itemView.setOnClickListener {
            onPaymentClicked(payment.paymentId)
        }
    }

    class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textSerialNumber: TextView = itemView.findViewById(R.id.text_serial_number)
        private val imageWorker: CircleImageView = itemView.findViewById(R.id.image_worker)
        private val textWorkerName: TextView = itemView.findViewById(R.id.text_worker_name)
        private val textPaymentDate: TextView = itemView.findViewById(R.id.text_payment_date)
        private val textPaymentDetails: TextView = itemView.findViewById(R.id.text_payment_details)
        private val textPaymentAmount: TextView = itemView.findViewById(R.id.text_payment_amount)
        private val buttonViewPayment: MaterialButton = itemView.findViewById(R.id.button_view_payment)

        fun bind(payment: Payment, serialNumber: Int) {
            // Set serial number
            textSerialNumber.text = serialNumber.toString()
            // Get worker from the cache
            val worker = workerMap[payment.workerId]

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

            textPaymentDate.text = payment.paymentDate
            textPaymentDetails.text = "Site ID: ${payment.siteId} | ${payment.description}"
            textPaymentAmount.text = CurrencyFormatter.formatRupees(payment.amount)

            // Set click listener for the action button
            buttonViewPayment.setOnClickListener {
                itemView.performClick()
            }
        }
    }

    class PaymentDiffCallback : DiffUtil.ItemCallback<Payment>() {
        override fun areItemsTheSame(oldItem: Payment, newItem: Payment): Boolean {
            return oldItem.paymentId == newItem.paymentId
        }

        override fun areContentsTheSame(oldItem: Payment, newItem: Payment): Boolean {
            return oldItem == newItem
        }
    }
}
